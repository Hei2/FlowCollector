/*
 * This file is part of jsFlow.
 *
 * Copyright (c) 2009 DE-CIX Management GmbH <http://www.de-cix.net> - All rights
 * reserved.
 * 
 * Author: Thomas King <thomas.king@de-cix.net>
 *
 * This software is licensed under the GNU Public License (GPL) license. A copy of 
 * the license agreement is included in this distribution.
 */
package jsflow;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import jsflow.header.CounterRecordHeader;
import jsflow.header.EthernetInterfaceCounterHeader;
import jsflow.header.ExpandedCounterSampleHeader;
import jsflow.header.ExpandedFlowSampleHeader;
import jsflow.header.FlowRecordHeader;
import jsflow.header.FlowSampleHeader;
import jsflow.header.GenericFlowSampleHeader;
import jsflow.header.GenericInterfaceCounterHeader;
import jsflow.header.HeaderException;
import jsflow.header.HeaderParseException;
import jsflow.header.IPHeader;
import jsflow.header.RawPacketHeader;
import jsflow.header.SampleDataHeader;
import jsflow.header.SflowHeader;
import jsflow.header.TransportHeader;
import jsflow.util.UtilityException;

/**
 * Main class to run the SFlow Collector.
 *
 * @author wk
 *
 */
public class SFlowCollector extends Thread {

    public final int FLOW_SAMPLE = 1;
    public final int COUNTER_SAMPLE = 2;
    public final int EXPANDED_FLOW_SAMPLE = 3;
    public final int EXPANDED_COUNTER_SAMPLE = 4;

    private boolean displayOutput = false;

    public boolean isDisplayOutput() {
        return displayOutput;
    }

    public void setDisplayOutput(boolean displayOutput) {
        this.displayOutput = displayOutput;
    }
    private int port;
    private HashSet<Agent> agents;

    public SFlowCollector() {
        this.port = 6343;
        agents = new HashSet<>();
    }

    public SFlowCollector(int port) {
        this.port = port;
        agents = new HashSet<>();
    }

    /**
     * Run sFlowCollector on a thread.
     */
    public void run() {
        try (DatagramSocket ds = new DatagramSocket(port)) {
            long initialTime = System.currentTimeMillis();
            while (true) {
                try {
                    // Receive data and parse
                    byte[] data = new byte[2500];
                    DatagramPacket dp = new DatagramPacket(data, data.length);
                    ds.receive(dp);
                    SflowHeader sfh = SflowHeader.parse(dp.getData());

                    // Append data to their respective agents & subAgents
                    boolean found = false;
                    Agent agent = new Agent(sfh.getAddressAgent().toString());
                    for(Agent a : agents){
                        if(a.equals(agent)){
                            agent = a;
                            found = true;
                            break;
                        }
                    }
                    if(!found) agents.add(agent);

                    for (SampleDataHeader sdh : sfh.getSampleDataHeaders()) {
                        GenericFlowSampleHeader fsh;

                        // Check type of sample data.
                        if (sdh.getSampleDataFormat() == SampleDataHeader.FLOWSAMPLE) {
                            fsh = sdh.getFlowSampleHeader();
                        } else if (sdh.getSampleDataFormat() == SampleDataHeader.EXPANDEDFLOWSAMPLE) {
                            fsh = sdh.getExpandedFlowSampleHeader();
                        } else {
                            fsh = null; // Counter records are not parsed currently.
                        }

                        if (fsh != null) {
                            // Create a FlowSamplePacket representation that stores only needed information.
                            FlowSamplePacket fsp = new FlowSamplePacket();
                            fsp.setSampleSeqNo(fsh.getSequenceNumber());
                            fsp.setSamplePool(fsh.getSamplePool());
                            fsp.setSource(((Long) fsh.getSourceIDType()).toString());

                            Vector<FlowRecordHeader> flowRecords = fsh.getFlowRecords();

                            for (FlowRecordHeader frh : flowRecords) {
                                // Each Flow Sample has 4 Flow Records, 1, 1001, 1002, 1003
                                // We are only parsing 1, the Raw Packet Header 
                                // Other counter data may be useful in the future
                                if (frh.getFlowDataFormat() == frh.RAW_PACKET_HEADER) {
                                    RawPacketHeader rph = frh.getRawPacketHeader();
                                    fsp.setPacketSize(rph.getFrameLength());

                                    IPHeader iph = rph.getMacHeader().getIph();
                                    fsp.setDestIP(iph.getDestAddress());
                                    fsp.setSrcIP(iph.getSrcAddress());
                                    fsp.setProtocol(iph.getProtocol());

                                    TransportHeader tph = iph.getTph();
                                    fsp.setSourcePort(tph.getSrcPort());
                                    fsp.setDestPort(tph.getDestPort());
                                }
                            }

                            SubAgent sa = agent.get(Long.toString(fsh.getSourceIDType()));
                            if (sa == null) {
                                sa = new SubAgent(Long.toString(fsh.getSourceIDType()), agent.getIpAddress());
                                agent.add(sa);
                            }
                            sa.addPacket(fsp);
                        }
                    }

                    // Currently hard-coded to insert / print statistics every 60 seconds
                    if (System.currentTimeMillis() - initialTime >= 60000) {
                        for (Agent a : agents) {
                            for (SubAgent sa : agent.getSubAgents()) {
                                try {
                                    sa.analyze();
                                    sa.insertFlows();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (displayOutput) {
                                a.printFlows();
                                a.printTop();
                            }
                        }
                        // Reinitialize variables
                        initialTime = System.currentTimeMillis();
                        agents.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (HeaderParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (HeaderException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UtilityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
