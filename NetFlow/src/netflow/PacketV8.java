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

//http://netflow.caligare.com/netflow_v8.htm

/**
 *
 * @author Keenan
 */
public class PacketV8 extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketV8(DatagramPacket received)
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
                byte aggregation = in.readByte();
                byte agg_version = in.readByte();
                in.skipBytes(4); //Ignoring reserved
                
                String insert = "INSERT INTO PACKET_V8_HEADER (count, sys_uptime, unix_secs, unix_nsecs, flow_sequence, engine_type, engine_id, aggregation, agg_version) " +
                        "VALUES (" + count + ", " + sys_uptime + ", " + unix_secs + ", " + unix_nsecs + ", " + flow_sequence + ", " + engine_type + ", " + engine_id + ", " +
                        aggregation + ", " + agg_version + ")";
                
                stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                
                if (rs != null && rs.next())
                {
                    long header_id = rs.getLong(1);
                    
                    //Store the rest of the packet as a BLOB and allow it to be
                    //parsed later.
                    byte[] packet = new byte[in.available()];
                    in.readFully(packet);
                    insert = "INSERT INTO PACKET_V8 (header_id, packet)" +
                            "VALUES (" + header_id + ", " + packet + ")";
                    stmt.executeUpdate(insert);
                }
                else
                {
                    System.out.println("Error: Unable to determine id of PACKET_V8_HEADER; associated flows dropped.");
                }
            }
            catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
                System.out.println("Error: An SQLException occurred in PacketV8.\n");
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: An IOException occurred in PacketV8.\n");
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: A ClassNotFoundException occurred in PacketV8.\n");
        }
    }
}