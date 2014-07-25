package flow;

import flow.DatabaseProperties;
import org.xbill.DNS.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
//import static netflow.Functions.getHostName;

public class DNS{

    public static boolean performDNSLookup(String StartTimeStr,String EndTimeStr) {
        try {
            System.out.println("Beginning IP collection");
            createDNSTable();
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VERSION()");
            rs.first();
	    String [] sql_v = rs.getString(1).split("\\.",0);
            double mysql_version = Integer.parseInt(sql_v[0]) + Integer.parseInt(sql_v[1])*0.1d;
	    
            //Retrieve the IP addresses in the Flows table.
	    String retrieve = "";
	    if(mysql_version >= 5.6){
		retrieve = "SELECT DISTINCT(INET6_NTOA(SourceAddress)) FROM FLOWS WHERE DateTimeInitiated between "+ "'"+StartTimeStr+"' and '"+EndTimeStr+"'";
	    } 
	    else {
		retrieve = "SELECT DISTINCT(SourceAddress) FROM FLOWS WHERE DateTimeInitiated between "+ "'"+StartTimeStr+"' and '"+EndTimeStr+"'";
	    }
            ResultSet rset = stmt.executeQuery(retrieve);

            HashSet<String> ipAddresses = new HashSet<>();

            while (rset.next()) {
                String ip = rset.getString(1);
                if (!ip.equals("Unknown Host")) {
                    ipAddresses.add(ip);
                }
            }

            //Retrieve the IP addresses in the Flows table.
	    if(mysql_version >= 5.6){
		retrieve = "SELECT DISTINCT(INET6_NTOA(DestinationAddress)) FROM FLOWS WHERE DateTimeInitiated between "+ "'"+StartTimeStr+"' and '"+EndTimeStr+"'";
	    } 
	    else {
		retrieve = "SELECT DISTINCT(DestinationAddress) FROM FLOWS WHERE DateTimeInitiated between "+ "'"+StartTimeStr+"' and '"+EndTimeStr+"'";
	    }
            rset = stmt.executeQuery(retrieve);

            while (rset.next()) {
                String ip = rset.getString(1);
                //Add only unique IPs.
                //!ip.equals("Unknown Host") part may be removed, cause no "Unknown Host" assigment in getHostName function
                if (!ip.equals("Unknown Host") && !ip.startsWith("192.")) {
                    ipAddresses.add(ip);
                }
            }

            System.out.println("IP collection complete\nBeginning DNS lookups");

            //Perform a lookup on each IP.
            for (String ip : ipAddresses) {
                String hostname = getHostName(ip);
                if (!hostname.equals(ip)) {
	    	    if(mysql_version >= 5.6){
                    	retrieve = "REPLACE INTO DNS_LOOKUP VALUES (INET6_ATON('" + ip + "'), '" + hostname + "', " + " NOW()" + ")";
		    } else {
                    	retrieve = "REPLACE INTO DNS_LOOKUP VALUES ('" + ip + "', '" + hostname + "', " + " NOW()" + ")";
		    }
                    stmt.executeUpdate(retrieve);
                }
            }

            System.out.println("DNS lookups complete");
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean createDNSTable() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Create the table.
            String create = "CREATE TABLE IF NOT EXISTS DNS_LOOKUP (ip VARBINARY(16) NOT NULL, "
                    + "hostname TEXT, "
                    + "timestamp DATETIME NOT NULL, "
                    + "PRIMARY KEY (ip))";
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

    public static String getHostName(String hostIp) throws IOException {
	try{
		Record opt = null;
		Resolver res = new ExtendedResolver();

		Name name = ReverseMap.fromAddress(hostIp);
		int type = Type.PTR;
		int dclass = DClass.IN;
		Record rec = Record.newRecord(name, type, dclass);
		Message query = Message.newQuery(rec);
		Message response = res.send(query);

		Record[] answers = response.getSectionArray(Section.ANSWER);
		if (answers.length == 0) {
		    return hostIp;
		} else {
		    return answers[0].rdataToString();
		}
	}catch(SocketTimeoutException ste){}
	return hostIp;
    }
}
