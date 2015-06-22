/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package classifier;

/**
 *
 * @author gxia
 */
import birchtree.*;
import flow.DatabaseProperties;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.sizeof.SizeOf;

public class Classifier {
    
    public static HashSet<Integer> cluster(String StartSecsStr,String EndSecsStr) {
        //skeptical host list- return value
        HashSet<Integer> potentialP2PHost = new HashSet<Integer>();
        
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
                    Statement stmt = conn.createStatement();   ){
                System.out.println("birchTree cluster begin");
                createP2PTable();            
                //retrieve local host ip list
                HashSet<Integer> ipAddresses = RetrieveLocalhost(StartSecsStr,EndSecsStr); 
//                HashSet<Integer> ipAddresses = new HashSet<Integer>();
//                ipAddresses.add(-2038342695);
               
                //Perform clustering on every internal host.
                String retrieve = "";
                double[] x = new double[2];
                long id = 0;
                int dstaddr = 0;
                for (int ip : ipAddresses) {
                    //retrieve flows from database for this particular host 
                    retrieve = "SELECT a.id,dPkts,dOctets,dstaddr FROM PACKET_V5 a left join PACKET_V5_HEADER b on a.header_id=b.id "
                            +"WHERE b.unix_secs between "+StartSecsStr+" and "+EndSecsStr+" and srcaddr="+ip; 
//                    retrieve = "SELECT a.id,dPkts,dOctets,srcaddr FROM PACKET_V5 a left join PACKET_V5_HEADER b on a.header_id=b.id "
//                            +"WHERE srcaddr not in (select ip from DNS_LOOKUP_Nf) and b.unix_secs between "+StartSecsStr+" and "+EndSecsStr+" and srcaddr="+ip; 

                    //only outbound ip flows are considered at first phase.
                    ResultSet rset1Host = stmt.executeQuery(retrieve);

                    //Instantiate a new birch tree instance       
                    int maxNodeEntries = 40; //maximum entries per node;
                    double distThreshold = 1; //initial distance threshold 
                    int distFunction = CFTree.D0_DIST;//type of distance;
                    boolean applyMergingRefinement = true;

                    CFTree birchTree = new CFTree(maxNodeEntries,distThreshold,distFunction,applyMergingRefinement);
                    // no need to rebuild by MemoryLimit, because LeafEntriesLimit will trigger rebuild if too many leaf entries created
                    //birchTree.setMemoryLimit(200*1024*1024); //200MB
                    //birchTree.setAutomaticRebuild(true);
                    //birchTree.setPeriodicMemLimitCheck(100000); //check memory every 100000 insertion.
                   
                    while (rset1Host.next()) {
                        id = rset1Host.getLong(1);  //column id
                        x[0] = rset1Host.getInt(2); //column dPkts
                        x[1] = rset1Host.getInt(3); //column dOctets
                        dstaddr = rset1Host.getInt(4); //column dstaddr

                        boolean inserted = birchTree.insertEntry(x, id, dstaddr);
                        if(!inserted) {
                            System.err.println("ERROR: NOT INSERTED!");
                            //return false;
                        }
                    }
//                    ArrayList<Long> P2PflowidList = birchTree.getP2PtrafficID(5);
//                    if(P2PflowidList.size() > 0){
//                        for(Long e:P2PflowidList){
//                            retrieve = "REPLACE INTO P2P VALUES (" + e + ",NOW())" ;
//                            stmt.executeUpdate(retrieve);
//                        }
//
//                    }
                    if(birchTree.getP2PClusterNum(15)>1 ){
                        potentialP2PHost.add(ip);
                    }
                }
                System.out.println("birchTree cluster complete");

            } catch (SQLException ex) {
                if (ex.getSQLState().equals("08S01")) {
                    System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
                } else {
                    System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString());
                    ex.printStackTrace();
                }                
                System.out.println("\nFailed to perform the clustring.\nRestart the program.");
            }            
        } catch (ClassNotFoundException ex) {
                Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return potentialP2PHost;
    }
        
//flow sets filter
public static boolean flowFilter(int ip) {   
    //stitch outgoing and incoming traffic for the host
    
    //
    
    return true;
}    

public static void P2PIdentify(String StartSecsStr,String EndSecsStr) {   
    //stitch outgoing and incoming traffic for the host
    try{
        HashSet<Integer> potentialP2PHost = cluster(StartSecsStr,EndSecsStr);
    } catch(Exception ex){
        ex.printStackTrace();
    }
    //return Fingermatch(StartSecsStr,EndSecsStr,potentialP2PHost);
    return;
} 

/**
 * generate finger cluster for local host and match known P2P protocol fingerprint
 * label identified flow id in a separate table for future display
 */
    public static boolean Fingermatch(String StartSecsStr,String EndSecsStr,HashSet<Integer> p2pHostList){
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
                    Statement stmt = conn.createStatement();   ){
                System.out.println("Fingermatch begin");
                createFingermatchTable();            

                //Perform clustering on every internal host.
                String retrieve = "";
                String tableclean="DROP TABLE IF EXISTS matchedPrint";
                long matchedPrint = 0;
                final int MATCH_TH = 3;
                for (int ip : p2pHostList) {
                    //clean temperate tables                    
                    stmt.executeUpdate(tableclean);                    
                    //match fingerprint for local host. only outbound traffic are considerred at this phase
                    retrieve = "create table matchedPrint select dPkts,dOctets,count(*) as cnt,count(distinct dstaddr) as ipcnt from PACKET_V5 a right join PACKET_V5_HEADER c on a.header_id=c.id where c.unix_secs between "+StartSecsStr+" and "+EndSecsStr
                    + " and dstport not in(53,137) and srcport not in (1900) and srcaddr="+ip+" group by dPkts,dOctets having cnt>10 and ipcnt>2 and (dPkts,dOctets) in (select dPkts,dOctets from btfprint)";
                    stmt.executeUpdate(retrieve);
                    
                    //query out number of matched fingerprint
                    retrieve = "select count(*) from matchedPrint";                    
                    ResultSet rset1Host = stmt.executeQuery(retrieve);

                    rset1Host.next();
                    matchedPrint = rset1Host.getLong(1);  //matched fingerprint  
                    if(matchedPrint> MATCH_TH){ //matched fingerprint greater than threshold
                        retrieve = "insert into table btappFlowIdlist select id from PACKET_V5 a right join PACKET_V5_HEADER c on a.header_id=c.id where c.unix_secs between "+StartSecsStr+" and "+EndSecsStr
                    + " and dstport not in(53,137) and srcport not in (1900) and srcaddr="+ip+" and dstaddr in(select distinct dstaddr from PACKET_V5 a right join PACKET_V5_HEADER c on a.header_id=c.id "
                                + "where c.unix_secs between "+StartSecsStr+" and "+EndSecsStr+" and srcaddr="+ip+" and (dPkts,dOctets) in (select dPkts,dOctets from matchedPrint))";                    
                        stmt.executeUpdate(retrieve);
                    }
                    
                }

                System.out.println("Fingermatch complete");
                return true;

            } catch (SQLException ex) {
                if (ex.getSQLState().equals("08S01")) {
                    System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
                } else {
                    System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString());
                    ex.printStackTrace();
                }

                System.out.println("\nFailed to perform the fingermatch.\nRestart the program.");
            }
            return false;
        } catch (ClassNotFoundException ex) {
                Logger.getLogger(Classifier.class.getName()).log(Level.SEVERE, null, ex);
                return false;
        } 
    }
        
        
    public static boolean createP2PTable() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");       
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
                Statement stmt = conn.createStatement(); ) {
                //Create the table.
                String create = "CREATE TABLE IF NOT EXISTS P2P (id bigint unsigned NOT NULL, "
                        + "timestamp DATETIME NOT NULL, "
                        + "PRIMARY KEY (id))";
                stmt.executeUpdate(create);
                return true;
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("08S01")) {
                    System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
                } else {
                    System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString()
                            + "\nFailed to connect to database with URL: " + DatabaseProperties.getDatabase());
                }

                System.out.println("\nFailed to create the database tables.\nRestart the program.");
                return false;
            } 
        } catch (ClassNotFoundException ex) {
                System.out.println(ex.getMessage());
                return false;
            }
    }
    
    public static boolean createFingermatchTable() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");     
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
                Statement stmt = conn.createStatement(); ) {
                //Create the table.
                String create = "CREATE TABLE IF NOT EXISTS FingerMatch (id bigint unsigned NOT NULL, "
                        + "timestamp DATETIME NOT NULL, "
                        + "PRIMARY KEY (id))";
                stmt.executeUpdate(create);
                return true;
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("08S01")) {
                    System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
                } else {
                    System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString()
                            + "\nFailed to connect to database with URL: " + DatabaseProperties.getDatabase());
                }
                System.out.println("\nFailed to create the database tables.\nRestart the program.");
                return false;
            } 
        } catch (ClassNotFoundException ex) {
                System.out.println(ex.getMessage());
                return false;
        }
    }
    
    
    
    public static HashSet<Integer> RetrieveLocalhost(String StartSecsStr,String EndSecsStr) {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");     
            try(Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
                Statement stmt = conn.createStatement(); ) {
                
                String retrieve = "";
                //obtain local/internal host ip address list
                //suppose this interface is connected to intranet, then source addresses are corresponding to local host. 
                retrieve = "SELECT DISTINCT srcaddr FROM PACKET_V5 a left join PACKET_V5_HEADER b on a.header_id=b.id WHERE b.unix_secs between "+StartSecsStr+" and "+EndSecsStr;
                ResultSet rset = stmt.executeQuery(retrieve);    
                HashSet<Integer> ipAddresses = new HashSet<>();            
                while (rset.next()) {
                    int ip = rset.getInt(1);
                    ipAddresses.add(ip);
                }
                return ipAddresses;
            } catch (SQLException ex) {
                if (ex.getSQLState().equals("08S01")) {
                    System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
                } else {
                    System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString()
                            + "\nFailed to connect to database with URL: " + DatabaseProperties.getDatabase());
                }
                System.out.println("\nFailed to retrieve list of local hostip address.");                
            } 
        } catch (ClassNotFoundException ex) {
                System.out.println(ex.getMessage());                
        }
        return null;
    }
}


