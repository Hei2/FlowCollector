package flow;

import netflow.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsflow.SFlowCollector;

//http://stackoverflow.com/questions/5160414/read-netflow-rflow-dd-wrt-packet-content
//http://stackoverflow.com/questions/10556829/sending-and-receiving-udp-packets-using-java
//http://dev.mysql.com/doc/refman/5.0/es/connector-j-reference-configuration-properties.html

/**
 * Main class to run the sflow program.
 * @author Wei Kang Lim, Keenan Salveson
 */
public class FlowCollector
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {        
        //Get the url, user, and password from the config file.
        File config = new File("config.txt");
        try{
            DatabaseProperties.load(config);
        } 
        catch (FileNotFoundException ex)
        {
            System.err.println("Configuration file for database not found.");
            System.err.println("Program will terminate since a database is needed for collector.");
            ex.printStackTrace();
            return;
        } catch (UninitializedException ex) {
            ex.printStackTrace();
        }
        
        try{            
            int sflow_port =  -1;
            int netflow_port = -1;
            boolean display = false;
            NetFlowCollector nc;
            SFlowCollector sc;
            // Take in arguments from command line
            for(int i = 0; i < args.length; i++){
                switch(args[i]){
                    case "-s": sflow_port = Integer.parseInt(args[++i]); break;
                    case "-n": netflow_port = Integer.parseInt(args[++i]); break;
                    case "-o": display = true; break;
                    default: System.err.println("Unrecognized option: " + args[i]); return;
                }
            }
            
            createTables();
        
            if(netflow_port == -1){
                nc = new NetFlowCollector();
            } else {
                nc = new NetFlowCollector(netflow_port);
            }
            
            if(sflow_port == -1){
                sc = new SFlowCollector();
            } else{
                sc = new SFlowCollector(sflow_port);
            }
            sc.setDisplayOutput(display);
            
            
            nc.start();
            sc.start();
            double initialTime = System.currentTimeMillis();
            
            while(true){
                if(System.currentTimeMillis() - initialTime >= 60000){ // DNS lookup every hour 3600000
                    try{
                        DNS.performDNSLookup();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    initialTime = System.currentTimeMillis(); // reset initial time
                }
              }
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.err.println("Error: Failed to connect to database: " + DatabaseProperties.getDatabase());
            } else {
                System.err.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString());
            }

            System.err.println("\nFailed to create the database tables.\nRestart the program.");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
     /**
     * This attempts to connect to the database and create the appropriate
     * tables if they do not exist.
     *
     */
    private static void createTables() throws ClassNotFoundException, SQLException {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + 
                    DatabaseProperties.getDatabase(), 
                    DatabaseProperties.getUser(), 
                    DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Begin creating tables.
            String create = "CREATE TABLE IF NOT EXISTS PACKET_V1_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "unix_nsecs INT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V1 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "srcaddr INT NOT NULL, "
                    + "dstaddr INT NOT NULL, "
                    + "nexthop INT NOT NULL, "
                    + "input SMALLINT NOT NULL, "
                    + "output SMALLINT NOT NULL, "
                    + "dPkts INT NOT NULL, "
                    + "dOctets INT NOT NULL, "
                    + "first INT NOT NULL, "
                    + "last INT NOT NULL, "
                    + "srcport SMALLINT NOT NULL, "
                    + "dstport SMALLINT NOT NULL, "
                    + "prot TINYINT NOT NULL, "
                    + "tos TINYINT NOT NULL, "
                    + "flags TINYINT NOT NULL, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY(header_id) REFERENCES PACKET_V1_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V5_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "unix_nsecs INT NOT NULL, "
                    + "flow_sequence INT NOT NULL, "
                    + "engine_type TINYINT NOT NULL, "
                    + "engine_id TINYINT NOT NULL, "
                    + "sampling_interval SMALLINT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V5 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "srcaddr INT NOT NULL, "
                    + "dstaddr INT NOT NULL, "
                    + "nexthop INT NOT NULL, "
                    + "input SMALLINT NOT NULL, "
                    + "output SMALLINT NOT NULL, "
                    + "dPkts INT NOT NULL, "
                    + "dOctets INT NOT NULL, "
                    + "first INT NOT NULL, "
                    + "last INT NOT NULL, "
                    + "srcport SMALLINT NOT NULL, "
                    + "dstport SMALLINT NOT NULL, "
                    + "tcp_flags TINYINT NOT NULL, "
                    + "prot TINYINT NOT NULL, "
                    + "tos TINYINT NOT NULL, "
                    + "src_as SMALLINT NOT NULL, "
                    + "dst_as SMALLINT NOT NULL, "
                    + "src_mask TINYINT NOT NULL, "
                    + "dst_mask TINYINT NOT NULL, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (header_id) REFERENCES PACKET_V5_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V6_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "unix_nsecs INT NOT NULL, "
                    + "flow_sequence INT NOT NULL, "
                    + "engine_type TINYINT NOT NULL, "
                    + "engine_id TINYINT NOT NULL, "
                    + "sampling_interval SMALLINT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V6 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "srcaddr INT NOT NULL, "
                    + "dstaddr INT NOT NULL,"
                    + "nexthop INT NOT NULL, "
                    + "input SMALLINT NOT NULL, "
                    + "output SMALLINT NOT NULL, "
                    + "dPkts INT NOT NULL, "
                    + "dOctets INT NOT NULL, "
                    + "first INT NOT NULL, "
                    + "last INT NOT NULL, "
                    + "srcport SMALLINT NOT NULL,"
                    + "dstport SMALLINT NOT NULL, "
                    + "tcp_flags TINYINT NOT NULL, "
                    + "prot TINYINT NOT NULL, "
                    + "tos TINYINT NOT NULL, "
                    + "src_as SMALLINT NOT NULL, "
                    + "dst_as SMALLINT NOT NULL, "
                    + "src_mask TINYINT NOT NULL, "
                    + "dst_mask TINYINT NOT NULL, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (header_id) REFERENCES PACKET_V6_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V7_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "unix_nsecs INT NOT NULL, "
                    + "flow_sequence INT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V7 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "srcaddr INT NOT NULL, "
                    + "dstaddr INT NOT NULL, "
                    + "nexthop INT NOT NULL, "
                    + "input SMALLINT NOT NULL, "
                    + "output SMALLINT NOT NULL, "
                    + "dPkts INT NOT NULL, "
                    + "dOctets INT NOT NULL, "
                    + "first INT NOT NULL, "
                    + "last INT NOT NULL, "
                    + "srcport SMALLINT NOT NULL, "
                    + "dstport SMALLINT NOT NULL, "
                    + "tcp_flags TINYINT NOT NULL, "
                    + "prot TINYINT NOT NULL, "
                    + "tos TINYINT NOT NULL, "
                    + "src_as SMALLINT NOT NULL, "
                    + "dst_as SMALLINT NOT NULL, "
                    + "src_mask TINYINT NOT NULL, "
                    + "dst_mask TINYINT NOT NULL, "
                    + "flags SMALLINT NOT NULL, "
                    + "router_sc INT NOT NULL, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (header_id) REFERENCES PACKET_V7_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V8_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "unix_nsecs INT NOT NULL, "
                    + "flow_sequence INT NOT NULL, "
                    + "engine_type TINYINT NOT NULL, "
                    + "engine_id TINYINT NOT NULL, "
                    + "aggregation TINYINT NOT NULL, "
                    + "agg_version TINYINT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V8 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "packet BLOB, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (header_id) REFERENCES PACKET_V8_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V9_HEADER (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "count SMALLINT NOT NULL, "
                    + "sys_uptime INT NOT NULL, "
                    + "unix_secs INT NOT NULL, "
                    + "package_sequence INT NOT NULL, "
                    + "source_id INT NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS PACKET_V9 (id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT, "
                    + "header_id BIGINT UNSIGNED NOT NULL, "
                    + "packet BLOB, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (header_id) REFERENCES PACKET_V9_HEADER(id) ON DELETE CASCADE)";
            stmt.addBatch(create);

            create = "CREATE TABLE IF NOT EXISTS FLOWS ( FlowID SERIAL, "
                    + "ProtocolNumber TINYINT UNSIGNED, "
                    + "SourceAddress VARCHAR(35) NOT NULL, "
                    + "DestinationAddress VARCHAR(35) NOT NULL, "
                    + "SourcePort SMALLINT UNSIGNED, "
                    + "DestinationPort SMALLINT UNSIGNED,  "
                    + "DateTimeInitiated DATETIME NOT NULL, "
                    + "KiloBytesTransferred DECIMAL(15,3) NOT NULL, "
                    + "PRIMARY KEY(FlowID))";
            stmt.addBatch(create);

             //Create the table.
            create = "CREATE TABLE IF NOT EXISTS DNS_LOOKUP (ip VARCHAR(35) NOT NULL, "
                    + "hostname TEXT, "
                    + "timestamp DATETIME NOT NULL, "
                    + "PRIMARY KEY (ip))";
            stmt.addBatch(create);
            
            stmt.executeBatch();
    }
}