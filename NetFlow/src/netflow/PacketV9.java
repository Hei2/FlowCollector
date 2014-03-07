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

//NetFlow v9 RFC: http://www.ietf.org/rfc/rfc3954.txt
//Easier read for v9: http://netflow.caligare.com/netflow_v9.htm

/**
 *
 * @author Keenan
 */
public class PacketV9 extends Thread
{
    /*enum FieldTypes {
        IN_BYTES, IN_PKTS, FLOWS, PROTOCOL, SRC_TOS, TCP_FLAGS, L4_SRC_PORT,
        IPV4_SRC_ADDR, SRC_MASK, INPUT_SNMP, L4_DST_PORT, IPV4_DST_ADDR,
        DST_MASK, OUTPUT_SNMP, IPV4_NEXT_HOP, SRC_AS, DST_AS, BGP_IPV4_NEXT_HOP,
        MUL_DST_PKTS, MUL_DST_BYTES, LAST_SWITCHED
    }*/
    
    /*
     * Defined at: http://netflow.caligare.com/netflow_v9.htm
     * !!UNFINISHED!!
     */
    private static final int[][] FieldTypes = new int[][]
    {
        {1, 4}, {2, 4}, {3, 4}, {4, 1}, {5, 1}, {6, 1}, {7, 2}, {8, 4}, {9, 1},
        {10, 2}, {11, 2}, {12, 4}, {13, 1}, {14, 2}, {15, 4}, {16, 2}, {17, 2},
        {18, 4}, {19, 4}, {20, 4}
    };
    
    /*
     * IDEAS: Use a HashMap<Integer, String> connecting Value to Field Type.
     * Store the template as an ArrayList(?) of Values. When reading through a
     * Record, use the template to determine data types to store in another
     * ArrayList of data associated with a Value. When storing the data in the
     * database, use the Value in the 2nd ArrayList to find the Field Type in
     * the HashMap to construct an INSERT statement...
     * 
     * !IMPORTANT! Templates will need to be saved to the database.
     */
    
    DatagramPacket receivedPacket;
            
    public PacketV9(DatagramPacket received)
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
                int package_sequence = in.readInt();
                int source_id = in.readInt();
                
                String insert = "INSERT INTO PACKET_V9_HEADER (count, sys_uptime, unix_secs, package_sequence, source_id) " +
                        "VALUES (" + count + ", " + sys_uptime + ", " + unix_secs + ", " + package_sequence + ", " + source_id + ")";
                
                stmt.executeUpdate(insert, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                
                if (rs != null && rs.next())
                {
                    long header_id = rs.getLong(1);

                    //Store the rest of the packet as a BLOB and allow it to be
                    //parsed later.
                    byte[] packet = new byte[in.available()];
                    in.readFully(packet);
                    insert = "INSERT INTO PACKET_V9 (header_id, packet)" +
                            "VALUES (" + header_id + ", " + packet + ")";
                    stmt.executeUpdate(insert);
                }
                else
                {
                    System.out.println("Error: Unable to determine id of PACKET_V9_HEADER; associated flows dropped.");
                }
            }
            catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
                System.out.println("Error: An SQLException occurred in PacketV9.\n");
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: An IOException occurred in PacketV9.\n");
        }
        catch (ClassNotFoundException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: A ClassNotFoundException occurred in PacketV9.\n");
        }
    }
}