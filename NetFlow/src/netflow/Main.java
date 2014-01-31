package netflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
//import java.util.Scanner;

//http://stackoverflow.com/questions/5160414/read-netflow-rflow-dd-wrt-packet-content
//http://stackoverflow.com/questions/10556829/sending-and-receiving-udp-packets-using-java
//http://www.vogella.com/tutorials/MySQLJava/article.html

/**
 *
 * @author Keenan
 */
public class Main
{
    private static String user, pass, url;
    
    public static String GetUser() { return user; }
    
    public static String GetPass() { return pass; }
    
    public static String GetUrl() { return url; }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        /*System.out.println("Enter an IP Address to search for: ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        scanner.close();
        System.out.println("\nThe name associated with that IP is:\n" + Functions.getHostName(ip) + "\n");*/
        
        //Do something to get the url, user, and password from the user.
        
        try
        {
            CreateDatabase();
        }
        catch (SQLException | ClassNotFoundException ex)
        {
            System.out.println("Failed to create the database tables.\nRestart the program.");
            System.out.println(ex.getMessage());
        }
        
        Capture capture = new Capture();
        capture.start();
    }
    
    private static void CreateDatabase()
    throws SQLException, ClassNotFoundException
    {
        //Load the driver.
        Class.forName("com.mysql.jdbc.Driver");
        
        //Create a connection.
        Connection conn = DriverManager.getConnection(url, user, pass);
        
        //Create a statement object to use.
        Statement stmt = conn.createStatement();
        
        //Begin creating tables.
        String create = "CREATE TABLE IF NOT EXISTS PACKET_V1_HEADER (id INT NOT NULL AUTO_INCREMENT, " +
                        "count SMALLINT NOT NULL, " +
                        "sys_uptime INT NOT NULL, " +
                        "unix_secs INT NOT NULL, " +
                        "unix_nsecs INT NOT NULL, " +
                        "PRIMARY KEY (id));";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V1 (id INT NOT NULL AUTO_INCREMENT, " +
                        "header_id INT NOT NULL, " +
                        "srcaddr INT NOT NULL, " +
                        "dstaddr INT NOT NULL, " +
                        "nexthop INT NOT NULL, " +
                        "input SMALLINT NOT NULL, " +
                        "output SMALLINT NOT NULL, " +
                        "dPkts INT NOT NULL, " +
                        "dOctets INT NOT NULL, " +
                        "first INT NOT NULL, " +
                        "last INT NOT NULL, " +
                        "srcport SMALLINT NOT NULL, " +
                        "dstport SMALLINT NOT NULL, " +
                        "prot TINYINT NOT NULL, " +
                        "tos TINYINT NOT NULL, " +
                        "flags TINYINT NOT NULL, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY(header_id) REFERENCES PACKET_V1_HEADER(id) ON DELETE CASCADE);";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V5_HEADER (id INT NOT NULL AUTO_INCREMENT, " +
                        "count SMALLINT NOT NULL, " +
                        "sys_uptime INT NOT NULL, " +
                        "unix_secs INT NOT NULL, " +
                        "unix_nsecs INT NOT NULL, " +
                        "flow_sequence INT NOT NULL, " +
                        "engine_type TINYINT NOT NULL, " +
                        "engine_id TINYINT NOT NULL, " +
                        "sampling_interval SMALLINT NOT NULL, " +
                        "PRIMARY KEY (id));";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V5 (id INT NOT NULL AUTO_INCREMENT, " +
                        "header_id INT NOT NULL, " +
                        "srcaddr INT NOT NULL, " +
                        "dstaddr INT NOT NULL, " +
                        "nexthop INT NOT NULL, " +
                        "input SMALLINT NOT NULL, " +
                        "output SMALLINT NOT NULL, " +
                        "dPkts INT NOT NULL, " +
                        "dOctets INT NOT NULL, " +
                        "first INT NOT NULL, " +
                        "last INT NOT NULL, " +
                        "srcport SMALLINT NOT NULL, " +
                        "dstport SMALLINT NOT NULL, " +
                        "tcp_flags TINYINT NOT NULL, " +
                        "prot TINYINT NOT NULL, " +
                        "tos TINYINT NOT NULL, " +
                        "src_as SMALLINT NOT NULL, " +
                        "dst_as SMALLINT NOT NULL, " +
                        "src_mask TINYINT NOT NULL, " +
                        "dst_mask TINYINT NOT NULL, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY (header_id) REFERENCES PACKET_V5_HEADER(id) ON DELETE CASCADE);";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V6_HEADER (id INT NOT NULL AUTO_INCREMENT, " +
                        "count SMALLINT NOT NULL, " +
                        "sys_uptime INT NOT NULL, " +
                        "unix_secs INT NOT NULL, " +
                        "unix_nsecs INT NOT NULL, " +
                        "flow_sequence INT NOT NULL, " +
                        "engine_type TINYINT NOT NULL, " +
                        "engine_id TINYINT NOT NULL, " +
                        "sampling_interval SMALLINT NOT NULL, " +
                        "PRIMARY KEY (id));";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V6 (id INT NOT NULL AUTO_INCREMENT, " +
                        "header_id INT NOT NULL, " +
                        "srcaddr INT NOT NULL, " +
                        "dstaddr INT NOT NULL," +
                        "nexthop INT NOT NULL, " +
                        "input SMALLINT NOT NULL, " +
                        "output SMALLINT NOT NULL, " +
                        "dPkts INT NOT NULL, " +
                        "dOctets INT NOT NULL, " +
                        "first INT NOT NULL, " +
                        "last INT NOT NULL, " +
                        "srcport SMALLINT NOT NULL," +
                        "dstport SMALLINT NOT NULL, " +
                        "tcp_flags TINYINT NOT NULL, " +
                        "prot TINYINT NOT NULL, " +
                        "tos TINYINT NOT NULL, " +
                        "src_as SMALLINT NOT NULL, " +
                        "dst_as SMALLINT NOT NULL, " +
                        "src_mask TINYINT NOT NULL, " +
                        "dst_mask TINYINT NOT NULL, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY (header_id) REFERENCES PACKET_V6_HEADER(id) ON DELETE CASCADE);";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V7_HEADER (id INT NOT NULL AUTO_INCREMENT, " +
                        "count SMALLINT NOT NULL, " +
                        "sys_uptime INT NOT NULL, " +
                        "unix_secs INT NOT NULL, " +
                        "unix_nsecs INT NOT NULL, " +
                        "flow_sequence INT NOT NULL, " +
                        "PRIMARY KEY (id));";
        stmt.executeUpdate(create);
        
        create = "CREATE TABLE IF NOT EXISTS PACKET_V7 (id INT NOT NULL AUTO_INCREMENT, " +
                        "header_id INT NOT NULL, " +
                        "srcaddr INT NOT NULL, " +
                        "dstaddr INT NOT NULL, " +
                        "nexthop INT NOT NULL, " +
                        "input SMALLINT NOT NULL, " +
                        "output SMALLINT NOT NULL, " +
                        "dPkts INT NOT NULL, " +
                        "dOctets INT NOT NULL, " +
                        "first INT NOT NULL, " +
                        "last INT NOT NULL, " +
                        "srcport SMALLINT NOT NULL, " +
                        "dstport SMALLINT NOT NULL, " +
                        "tcp_flags TINYINT NOT NULL, " +
                        "prot TINYINT NOT NULL, " +
                        "tos TINYINT NOT NULL, " +
                        "src_as SMALLINT NOT NULL, " +
                        "dst_as SMALLINT NOT NULL, " +
                        "src_mask TINYINT NOT NULL, " +
                        "dst_mask TINYINT NOT NULL, " +
                        "flags SMALLINT NOT NULL, " +
                        "router_sc INT NOT NULL, " +
                        "PRIMARY KEY (id), " +
                        "FOREIGN KEY (header_id) REFERENCES PACKET_V7_HEADER(id) ON DELETE CASCADE);";
        stmt.executeUpdate(create);
        
        /*create = "";
        stmt.executeUpdate(create);
        
        create = "";
        stmt.executeUpdate(create);
        
        create = "";
        stmt.executeUpdate(create);
        
        create = "";
        stmt.executeUpdate(create);*/
        
        stmt.close();
        conn.close();
    }
}