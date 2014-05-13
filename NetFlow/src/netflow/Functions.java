package netflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 *
 * @author Keenan
 */
public class Functions
{
    private static String user, pass, url;
    
    public static void main(String[] args) {
        
        //Get the url, user, and password from the config file.
        File config = new File("config.txt");
        
        try (Scanner scanner = new Scanner(config);)
        {
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (line.startsWith("URL="))
                {
                    url = line.substring(4).concat("/flowcollectordb");
                }
                else if (line.startsWith("USER="))
                {
                    user = line.substring(5);
                }
                else if (line.startsWith("PASS="))
                {
                    pass = line.substring(5);
                }
            }
        }
        catch (FileNotFoundException ex)
        {
            System.out.println(ex.getMessage());
            System.out.println("Error: FileNotFoundException occurred in Main.");
        }
        
        performDNSLookup();
    }
    
    public static String getHostName(String ip) {
        try {
            InetAddress ia = InetAddress.getByName(ip);
            String address = ia.getCanonicalHostName();
            return address;
        } catch (UnknownHostException e) {
            return "Unknown Host";
        }
    }
    
    public static boolean performDNSLookup() {
        try {
            System.out.println("Beginning IP collection");
            
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            //Connection conn = DriverManager.getConnection("jdbc:mysql://" + Main.GetUrl(), Main.GetUser(), Main.GetPass());
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + url, user, pass);

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Retrieve the IP addresses in the Flows table.
            String retrieve = "SELECT DISTINCT(SourceAddress) FROM Flows";
            ResultSet rset = stmt.executeQuery(retrieve);
            
            List<String> ipAddresses = new ArrayList<>();
            
            while(rset.next())
            {
                String ip = rset.getString(1);
                if (!ip.equals("Unknown Host")) {
                    ipAddresses.add(ip);
                }
            }
            
            //Retrieve the IP addresses in the Flows table.
            retrieve = "SELECT DISTINCT(DestinationAddress) FROM Flows";
            rset = stmt.executeQuery(retrieve);
            
            while(rset.next())
            {
                String ip = rset.getString(1);
                //Add only unique IPs.
                if (!ip.equals("Unknown Host") && !ipAddresses.contains(ip)) {
                    ipAddresses.add(ip);
                }
            }
            
            System.out.println("IP collection complete\nBeginning DNS lookups");
            
            //Perform a lookup on each IP.
            for (String ip : ipAddresses) {
                String hostname = getHostName(ip);
                if (!hostname.equals(ip)) {
                    long unixTime = System.currentTimeMillis()/1000L;
                    retrieve = "REPLACE INTO DNS_LOOKUP VALUES ('" + ip + "', '" + hostname + "', " + unixTime + ")";
                    stmt.executeUpdate(retrieve);
                }
            }
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.out.println("Error: Failed to connect to database with URL: " + Main.GetUrl());
            } else {
                System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString() +
                        "\nFailed to connect to database with URL: " + Main.GetUrl());
            }
            
            System.out.println("\nFailed to perform the DNS lookups.\nRestart the program.");
            return false;
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
        
        System.out.println("DNS lookups complete");
        return true;
    }
    
    public static boolean createDNSTable() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + Main.GetUrl(), Main.GetUser(), Main.GetPass());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Create the table.
            String create = "CREATE TABLE IF NOT EXISTS DNS_LOOKUP (ip VARCHAR(46) NOT NULL, " +
                            "hostname TEXT, " +
                            "timestamp BIGINT NOT NULL, " +
                            "PRIMARY KEY (ip))";
            stmt.executeUpdate(create);
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("08S01")) {
                System.out.println("Error: Failed to connect to database with URL: " + Main.GetUrl());
            } else {
                System.out.println("An unhandled error occured with the database\nSQL State: " + ex.getSQLState().toString() +
                        "\nFailed to connect to database with URL: " + Main.GetUrl());
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