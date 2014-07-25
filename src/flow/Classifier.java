/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package flow;

/**
 *
 * @author gxia
 */
import birchtree.*;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

public class Classifier {
    
        public static boolean cluster(String StartSecsStr,String EndSecsStr) {
        try {
            createP2PTable();
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");
            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
            //Create a statement object to use.
            Statement stmt = conn.createStatement();
            //Retrieve the IP addresses in the PACKET_V5. 
            //Since Netflow v5 is unidirectional and only for ingress, for certain interface either source address or destination address
            //is always address of local host.
	    String retrieve = "";
            retrieve = "SELECT DISTINCT srcaddr FROM PACKET_V5 a left join PACKET_V5_HEADER b on a.header_id=b.id "+"WHERE b.unix_secs between "+StartSecsStr+" and "+EndSecsStr;
            ResultSet rset = stmt.executeQuery(retrieve);
            
            HashSet<Integer> ipAddresses = new HashSet<>();            
            while (rset.next()) {
                int ip = rset.getInt(1);
                ipAddresses.add(ip);
            }

            //Perform clustering on every internal host.
            double[] x = new double[2];
            long id = 0;
            int dstaddr = 0;
            for (int ip : ipAddresses) {
                retrieve = "SELECT a.id,dPkts,dOctets,dstaddr FROM PACKET_V5 a left join PACKET_V5_HEADER b on a.header_id=b.id "
                        +"WHERE b.unix_secs between "+StartSecsStr+" and "+EndSecsStr+" and srcaddr="+ip;
                ResultSet rset1Host = stmt.executeQuery(retrieve);
                
                //Instantiate a new birch tree instance       
                int maxNodeEntries = 40; //maximum entries per node;
                double distThreshold = 5; //initial distance threshold 
                int distFunction = CFTree.D0_DIST;//type of distance;
                boolean applyMergingRefinement = true;
                
                CFTree birchTree = new CFTree(maxNodeEntries,distThreshold,distFunction,applyMergingRefinement);
                birchTree.setMemoryLimit(100*1024*1024); //100MB
                birchTree.setAutomaticRebuild(true); 		
		birchTree.setPeriodicMemLimitCheck(10000); //check memory every 10000 insertion.
                
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
                ArrayList<Long> P2PdstAddrList = birchTree.getP2PtrafficID(50);
                if(P2PdstAddrList.size() > 0){
                    for(Long e:P2PdstAddrList){
                        retrieve = "REPLACE INTO P2P VALUES (" + e + ",NOW())" ;
                        stmt.executeUpdate(retrieve);
                    }
                    
                }
            }

            System.out.println("birchTree cluster complete");
            return true;
            
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
            } else {
                System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString());
                ex.printStackTrace();
            }

            System.out.println("\nFailed to perform the DNS lookups.\nRestart the program.");
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
        } 
        return false;
    }
        
public static boolean createP2PTable() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Create the table.
            String create = "CREATE TABLE IF NOT EXISTS P2P (id bigint unsigned NOT NULL, "
                    + "timestamp DATETIME NOT NULL, "
                    + "PRIMARY KEY (id))";
            stmt.executeUpdate(create);
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
            } else {
                System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString()
                        + "\nFailed to connect to database with URL: " + DatabaseProperties.getDatabase());
            }

            System.out.println("\nFailed to create the database tables.\nRestart the program.");
            return false;
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

}
