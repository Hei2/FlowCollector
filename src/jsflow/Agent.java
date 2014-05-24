package jsflow;

import flow.DatabaseProperties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Agent represents a router that sample packets are received from. Sample
 * packets should be added to this agent. The agent can then either print
 * information to standard output, or save data to the collector database.
 *
 * @author Wei Kang Lim
 *
 */
public class Agent {

    private String user = DatabaseProperties.getUser();
    private String password = DatabaseProperties.getPassword();
    private String database = DatabaseProperties.getDatabase();
    private String id;
    private ArrayList<FlowSamplePacket> packets;
    private HashSet<String> srcIPTable;
    private ArrayList<Flow> flows;
    private long totalPacketsSize;

    public Agent(String id) {
        this.setId(id);
        packets = new ArrayList<FlowSamplePacket>();
        srcIPTable = new HashSet<String>();
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
        totalPacketsSize += packet.getPacketSize();
        srcIPTable.add(packet.getSrcIP());

        for (FlowSamplePacket fsp : packets) {
            if (fsp.getSampleSeqNo() == packet.getSampleSeqNo()) { // this sample has already been stored.
                return;
            }
        }

        this.packets.add(packet);
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
        Collections.sort(packets);
        double samplingRatio = samplingRatio();
        double averagePacketSize = averagePacketSize();

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
        for (Flow f : flows) {
            stmt.executeUpdate("INSERT INTO FLOWS (ProtocolNumber, SourceAddress, DestinationAddress, SourcePort, DestinationPort, DateTimeInitiated, KiloBytesTransferred)"
                    + String.format(" VALUES(%d, INET6_ATON('%s'), INET6_ATON('%s'), %d, %d, %s, %.3f )",
                    f.getProtocol(), f.getSrcIP(), f.getDestIP(), f.getSrcPort(), f.getDestPort(), "NOW()", f.getKilobytesTransferred()
                    ));
        }
        stmt.close();
    }

    /**
     * Prints information about the packets stored in this agent.
     */
    public void printFlows() {
         DecimalFormat kilobyte_format = new DecimalFormat("0.###");        
         for(Flow f : flows){
            System.out.println(" " + f.getSrcIP() + "->" + f.getDestIP() + " " + " " + Math.round(f.getPacketsSent()) + " " + kilobyte_format.format(f.getKilobytesTransferred()) + " KB  ");
         }
    }
    
//    public void printTop(){
//        
//    }
}
