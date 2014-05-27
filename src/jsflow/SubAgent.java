package jsflow;

import flow.DatabaseProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Agent represents a router that sample packets are received from. Sample
 * packets should be added to this agent. The agent can then either print
 * information to standard output, or save data to the collector database.
 *
 * @author Wei Kang Lim
 *
 */
public class SubAgent {
    private String agentIP;
    private String user = DatabaseProperties.getUser();
    private String password = DatabaseProperties.getPassword();
    private String database = DatabaseProperties.getDatabase();
    private String id;
    private ArrayList<FlowSamplePacket> packets;
    private ArrayList<Flow> flows;
    private long totalPacketsSize;

    public SubAgent(String id, String agentIP) {
        this.setId(id);
        this.setAgentIP(agentIP);        
        packets = new ArrayList<FlowSamplePacket>();
        flows = new ArrayList<Flow>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTotalPacketsSize() {
        return totalPacketsSize;
    }

    public String getAgentIP() {
        return agentIP;
    }

    public void setAgentIP(String agentIP) {
        this.agentIP = agentIP;
    }
    
    /**
     * Returns the packets contained in this source.
     *
     * @return
     */
    public ArrayList<FlowSamplePacket> getPackets() {
        ArrayList<FlowSamplePacket> packets = new ArrayList<>();
        packets.addAll(this.packets);
        return packets;
    }

    /**
     * Adds a packet to the source.
     *
     * @param packet
     */
    public void addPacket(FlowSamplePacket packet) {
        for (FlowSamplePacket fsp : packets) {
            if (fsp.getSampleSeqNo() == packet.getSampleSeqNo()) { // this sample has already been stored.
                return;
            }
        }
        totalPacketsSize += packet.getPacketSize();
        this.packets.add(packet);
    }
    
    @Override
    public boolean equals(Object o){
        return o instanceof SubAgent? false : ((SubAgent) o ).getId().equals(this.getId());
    }

    /**
     * Returns effective sampling ratio of the packets contained in this source.
     *
     * @return
     */
    public double samplingRatio() {
        long initialSamplePool = packets.get(0).getSamplePool();
        long finalSamplePool = packets.get(packets.size() - 1).getSamplePool();
        return (finalSamplePool - initialSamplePool) / ((double) packets.size());
    }

    /**
     * Returns average packet size contained in this source.
     *
     * @return
     */
    public double averagePacketSize() {
        return totalPacketsSize / ((double) packets.size());
    }

    /**
     * Analyze the packets that were sampled form the agent. Aggregate the
     * packet information and save it as Flows.
     */
    public void analyze() {
        // Sort by SrcIP, then by DestIP. This groups packets with same Src and Dest together.
        double samplingRatio = samplingRatio();
        double averagePacketSize = averagePacketSize();
        Collections.sort(packets);

        // O(n) complexity, where n is the number of sampled packets.
        for (int i = 0; i < packets.size(); i++) {
            FlowSamplePacket curr = packets.get(i);
            int count = 1;
            Flow f = new Flow();
            f.readFrom(curr);
            if(i != (packets.size() - 1 )){
                FlowSamplePacket next = packets.get(i + 1);
                while (next.equals(curr)) {
                    count++;
                    i++;
                    if (i >= packets.size()) {
                        break;
                    }
                    next = packets.get(i);
                }
            }
            int packetsSent = (int) Math.round(count * samplingRatio);
            double kilobytes_transferred = packetsSent * averagePacketSize / 1024;
            f.setNoSamplePackets(count);
            f.setPacketsSent(packetsSent);
            f.setKilobytesTransferred(kilobytes_transferred);
            flows.add(f);
        }
    }

    /**
     * Inserts the information contained in this agent into the database.
     *
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public void insertFlows() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection con = DriverManager.getConnection("jdbc:mysql://" + database,
                user,
                password);
        Statement stmt = con.createStatement();
        DecimalFormat kilobyte_format = new DecimalFormat("0.###");        
        ResultSet rs = stmt.executeQuery("SELECT VERSION()");
        rs.first();
	String [] sql_v = rs.getString(1).split("\\.",0);
        double mysql_version = Integer.parseInt(sql_v[0]) + Integer.parseInt(sql_v[1])*0.1d;
        for (Flow f : flows) {
	    if(mysql_version >= 5.6){
            	stmt.executeUpdate("INSERT INTO FLOWS (ProtocolNumber, SourceAddress, DestinationAddress, SourcePort, DestinationPort, DateTimeInitiated, KiloBytesTransferred, AgentAddress)"
                    + String.format(" VALUES(%d, INET6_ATON('%s'), INET6_ATON('%s'), %d, %d, %s, %s, INET6_ATON('%s') )",
                    f.getProtocol(), f.getSrcIP(), f.getDestIP(), f.getSrcPort(), f.getDestPort(), "NOW()", kilobyte_format.format(f.getKilobytesTransferred()), this.getAgentIP()
                    ));
	    } else {
            	stmt.executeUpdate("INSERT INTO FLOWS (ProtocolNumber, SourceAddress, DestinationAddress, SourcePort, DestinationPort, DateTimeInitiated, KiloBytesTransferred, AgentAddress)"
                    + String.format(" VALUES(%d, '%s', '%s', %d, %d, %s, %s, '%s' )",
                    f.getProtocol(), f.getSrcIP(), f.getDestIP(), f.getSrcPort(), f.getDestPort(), "NOW()", kilobyte_format.format(f.getKilobytesTransferred()),  this.getAgentIP()
                    ));		
	    }
        }
        stmt.close();
    }

    /**
     * Prints information about the packets stored in this agent.
     */
    public void printFlows() {
         DecimalFormat kilobyte_format = new DecimalFormat("0.###");     
         System.out.println(this.getId());
         for(Flow f : flows){
            System.out.println(" " + f.getSrcIP() + "->" + f.getDestIP() + " " + " " + Math.round(f.getPacketsSent()) + " " + kilobyte_format.format(f.getKilobytesTransferred()) + " KB  ");
         }
    }
    
    /**
     * Print top 10 sources and destinations.
     */
    public ArrayList<TrafficNode> aggregateTraffic(){
        ArrayList<TrafficNode> nodes = new ArrayList<>();
        ArrayList<Flow> flows = new ArrayList<>(this.flows.size());
        for(int i = 0; i < this.flows.size(); i++){
            flows.add(this.flows.get(i));
        }
        
        // Accumulate all sent data for each node
        Collections.sort(flows, new FlowSrcIPComparator()); // sorted by IP
        for (int i = 0; i < flows.size(); i++) {
            Flow curr = flows.get(i);
            double totalPacketsSent = curr.getPacketsSent();
            double totalKilobytesSent = curr.getKilobytesTransferred();
            String ip = curr.getSrcIP();
            if(i != (flows.size() - 1 )){
                Flow next = flows.get(i + 1);
                while (next.getSrcIP().equals(ip)) {
                    totalPacketsSent += next.getPacketsSent();
                    totalKilobytesSent += next.getKilobytesTransferred();
                    i++;
                    if (i >= packets.size()) {
                        break;
                    }
                    next = flows.get(i);
                }
            }
            TrafficNode tn = new TrafficNode();
            tn.setTotalKiloBytesSent(totalKilobytesSent);
            tn.setTotalPacketsSent(totalPacketsSent);
            tn.setIpAddress(ip);
            nodes.add(tn);
        }
        
        // Accumulate all received data for each node
        Collections.sort(flows, new FlowDestIPComparator());
        for (int i = 0; i < flows.size(); i++) {
            Flow curr = flows.get(i);
            double totalPacketsReceived = curr.getPacketsSent();
            double totalKilobytesReceived = curr.getKilobytesTransferred();
            String ip = curr.getDestIP();
            if(i != (flows.size() - 1 )){
                Flow next = flows.get(i + 1);
                while (next.getDestIP().equals(ip)) {
                    totalPacketsReceived += next.getPacketsSent();
                    totalKilobytesReceived += next.getKilobytesTransferred();
                    i++;
                    if (i >= packets.size()) {
                        break;
                    }
                    next = flows.get(i);
                }
            }
        
            TrafficNode tn = new TrafficNode();
            tn.setTotalKiloBytesReceived(totalKilobytesReceived);
            tn.setTotalPacketsReceived(totalPacketsReceived);
            tn.setIpAddress(ip);
            
            boolean contain = false;
            for(int j = 0; j < nodes.size(); j++){
                if(nodes.get(j).equals(tn)){
                    nodes.get(j).setTotalKiloBytesReceived(totalKilobytesReceived);
                    nodes.get(j).setTotalPacketsReceived(totalPacketsReceived);
                    contain = true;
                    break;
                } 
            }
            if(!contain){
                nodes.add(tn);
            }
        }
        
        return nodes;
    }
}
