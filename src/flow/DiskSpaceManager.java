/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package flow;

import static classifier.DNS.createDNSTable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

/**
 *
 * @author gxia
 * 
 */
public class DiskSpaceManager {
   
    public static synchronized void spaceChecker(){
        try {
            System.out.println("Beginning spaceChecker");
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");
            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());
            //Create a statement object to use.
            Statement stmt = conn.createStatement();
	    
            //Retrieve total occupied space and available space.
	    String retrieve = "";
            retrieve = "SELECT round(sum(data_length + index_length ) /1024/1024), round(sum( data_free )/1024/1024) " +
                        " FROM information_schema.TABLES where table_schema = DATABASE();";
            ResultSet rset = stmt.executeQuery(retrieve);
            
            while (rset.next()) {
                String occupiedSpace = rset.getString(1);
                String freeSpace = rset.getString(2);
                System.out.println("Occupied Disk Space: " + occupiedSpace + " MB");
                System.out.println("Free Disk Space: " + freeSpace + " MB\n");
                int free = Integer.parseInt(freeSpace);
                if(free < 500){
                    System.out.println("Caution: free space is less than 500 MB"); //can be replaced by other operation such as delete old data
                }
            }
            //query table disk space usage
            retrieve = "SELECT table_name, round((data_length + index_length ) / 1024 /1024), round( data_free/ 1024 /1024)\n" +
                        " FROM information_schema.TABLES where table_schema = DATABASE() order by (data_length + index_length) desc limit 10;";
            rset = stmt.executeQuery(retrieve);
            //print header
            if (rset.isBeforeFirst() ) {    
                System.out.format("%-18s\t%-15s%-15s\n","Table Name","Occupied Space","Free Space"); 
            } 
            while (rset.next()) {
                System.out.format("%-18s\t%-15s%-15s\n",rset.getString(1),rset.getString(2)+" MB",rset.getString(3)+" MB");
            }
            
            

            System.out.println("spaceChecker ends");
        }catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.out.println("Error: Failed to connect to database with URL: " + DatabaseProperties.getDatabase());
            } else {
                System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString());
                ex.printStackTrace();
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    
}
