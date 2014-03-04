/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//http://netflow.caligare.com/netflow_v5.htm

/**
 *
 * @author Keenan
 */
public class PacketV5 extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketV5(DatagramPacket received)
    {
        receivedPacket = received;
    }
    
    @Override
    public void run()
    {
        //Do we need to recreate the DatagramPacket just to be sure this
        //packet is not the same object used by other threads?
        SavePacket(receivedPacket);
    }
    
    /*
     * Grabs all of the packet information and stores it in the database.
     */
    private void SavePacket(DatagramPacket receivedPacket)
    {
        //Offset of 2 since version is being skipped.
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 2, receivedPacket.getLength());
                DataInputStream in = new DataInputStream(byteIn);)
        {
            Class.forName("com.mysql.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + Main.GetUrl(), Main.GetUser(), Main.GetPass());
                Statement stmt = conn.createStatement();)
            {
                short count = in.readShort();
                int sys_uptime = in.readInt();
                int unix_secs = in.readInt();
                int unix_nsecs = in.readInt();
                int flow_sequence = in.readInt();
                byte engine_type = in.readByte();
                byte engine_id = in.readByte();
                short sampling_interval = in.readShort();
                
                String insert = "INSERT INTO PACKET_V5_HEADER (count, sys_uptime, unix_secs, unix_nsecs, flow_sequence, engine_type, engine_id, sampling_interval) " +
                        "VALUES (" + count + ", " + sys_uptime + ", " + unix_secs + ", " + unix_nsecs + ", " + flow_sequence + ", " + engine_type + ", " + engine_id + ", " + sampling_interval + ")";
                
                stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                
                if (rs != null && rs.next())
                {
                    long header_id = rs.getLong(1);

                    for (int i = 0; i < count; i++)
                    {
                        int srcaddr = in.readInt();
                        int dstaddr = in.readInt();
                        int nexthop = in.readInt();
                        short input = in.readShort();
                        short output = in.readShort();
                        int dPkts = in.readInt();
                        int dOctets = in.readInt();
                        int first = in.readInt();
                        int last = in.readInt();
                        short srcport = in.readShort();
                        short dstport = in.readShort();
                        in.skipBytes(1); //Skip pad1
                        byte tcp_flags = in.readByte();
                        byte prot = in.readByte();
                        byte tos = in.readByte();
                        short src_as = in.readShort();
                        short dst_as = in.readShort();
                        byte src_mask = in.readByte();
                        byte dst_mask = in.readByte();
                        in.skipBytes(2); //Ignoring pad2
                        
                        insert = "INSERT INTO PACKET_V5 (header_id, srcaddr, dstaddr, nexthop, input, output, dPkts, dOctets, first, last, srcport, dstport, tcp_flags, prot, tos, src_as, dst_as, src_mask, dst_mask) " +
                                "VALUES (" + header_id + ", " + srcaddr + ", " + dstaddr + ", " + nexthop + ", " + input + ", " + output + ", " + dPkts + ", " + dOctets +
                                ", " + first + ", " + last + ", " + srcport + ", " + dstport + ", " + tcp_flags + ", " + prot + ", " + tos + ", " + src_as + ", " + dst_as +
                                src_mask + ", " + dst_mask + ")";
                        stmt.executeUpdate(insert);
                    }
                }
                else
                {
                    System.out.println("Error: Unable to determine id of PACKET_V5_HEADER; associated flows dropped.");
                }
            }
            catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
                System.out.println("Error: An SQLException occurred in PacketV5.\n");
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: An IOException occurred in PacketV5.\n");
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: A ClassNotFoundException occurred in PacketV5.\n");
        }
    }
}