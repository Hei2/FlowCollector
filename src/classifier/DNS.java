package classifier;

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
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.xbill.DNS.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import static netflow.Functions.getHostName;

public class DNS{
    private static SecondLevelDomain sld;
    public DNS(){
        sld = new SecondLevelDomain();
    }
    /**
     *  nested class
     * @author http://architects.dzone.com/articles/extract-second-and-top-level
     */
    public static class SecondLevelDomain {
      private StringBuilder sb = new StringBuilder();
      private Pattern pattern;

      public SecondLevelDomain() {
        try {
          ArrayList<String> terms = new ArrayList<String>();

          BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("res/effective_tld_names.dat")));
          String s = null;
          while ((s = br.readLine()) != null) {
            s = s.trim();
            if (s.length() == 0 || s.startsWith("//") || s.startsWith("!")) continue;
            terms.add(s);
          }
          Collections.sort(terms, new StringLengthComparator());
          for(String t: terms) add(t);
          compile();
          br.close();
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      }

      protected void add(String s) {
        s = s.replace(".", "\\.");
        s = "\\." + s;
        if (s.startsWith("*")) {
          s = s.replace("*", ".+");
          sb.append(s).append("|");
        } else {
          sb.append(s).append("|");
        }
      }

      public void compile() {
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        sb.insert(0, "[^.]+?(");
        sb.append(")$");
        pattern = Pattern.compile(sb.toString());
        sb = null;
      }

      public String extract2LD(String host) {
        Matcher m = pattern.matcher(host);
        if (m.find()) {
          return m.group(0);
        }
        return null;
      }

      public String extractTLD(String host) {
        Matcher m = pattern.matcher(host);
        if (m.find()) {
          return m.group(1);
        }
        return null;
      }

      public static class StringLengthComparator implements Comparator<String> {
        public int compare(String s1, String s2) {
          if (s1.length() > s2.length()) return -1;
          if (s1.length() < s2.length()) return 1;
          return 0;
        }
      }
    }    
    
    // depreciated method
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

    //DNS Lookup method for Netflow
    public static void performDNSLookup4NetFlow(String StartSecsStr,String EndSecsStr) {
        try {
            //String slddomain1 = sld.extract2LD("ec2-107-20-193-65.compute-1.amazonaws.com");
            createDNSTableNetflow();
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
            retrieve = "SELECT DISTINCT inet_ntoa(dstaddr&0xffffffff) as remotehost FROM PACKET_V5 "
                    + "a left join PACKET_V5_HEADER b on a.header_id=b.id WHERE dstaddr not in (select ip from DNS_LOOKUP_Nf) and b.unix_secs between "+StartSecsStr+" and "+EndSecsStr;
            ResultSet rset = stmt.executeQuery(retrieve);
            
            HashSet<String> ipAddresses = new HashSet<>();
            while (rset.next()) {
                String ip = rset.getString(1);
                if (!ip.isEmpty()) {
                    ipAddresses.add(ip);
                }
            }
            System.out.println("IP collection complete\nBeginning DNS lookups");
           
            //Perform a lookup on each IP.
            ExecutorService executorService = Executors.newFixedThreadPool(40);
            List<Callable<String[]>> tasklst = new ArrayList<Callable<String[]>>();
            for (String ip : ipAddresses) {
                tasklst.add(new ReverseDNSLookupThread(ip));
            }            
            try {
                List<Future<String[]>> HostLst= executorService.invokeAll(tasklst);
                try {
                    for (Future<String[]> hostname : HostLst) {                                           
                        if(!hostname.get()[0].equals(hostname.get()[1])){
                            //extract domain
                            String slddomain = sld.extract2LD(hostname.get()[1].substring(0,hostname.get()[1].length()-1));
                            //insert ip with positive dns reverse lookup result to table.
                            retrieve = "REPLACE INTO DNS_LOOKUP_Nf VALUES (cast(case when INET_ATON('" + hostname.get()[0] + "')>0x80000000 then INET_ATON('" + hostname.get()[0] + "')|0xffffffff00000000 else INET_ATON('" + hostname.get()[0] + "')&0xffffffff end as signed), '" + hostname.get()[1] + "','"+ slddomain+"'," + " NOW()" + ")";
                            stmt.executeUpdate(retrieve);   
                        }
                        
                    }
                } catch (ExecutionException ex) { ex.printStackTrace(); }
            }
            catch(InterruptedException ex){
                System.out.println("Threads pool interrupted");
                executorService.shutdown();
            }
            executorService.shutdown();
            System.out.println("DNS lookups complete");
            
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
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    //DNS Lookup method for sFlow
    public static void performDNSLookup4sFlow(String StartSecsStr,String EndSecsStr) {
        try {
            //String slddomain1 = sld.extract2LD("ec2-107-20-193-65.compute-1.amazonaws.com");
            createDNSTablesFlow();
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
	    
            //TODO: delete old DNS lookup
            
            //Retrieve the IP addresses in the Flows table.
	    String retrieve = "";
            retrieve = "SELECT INET6_NTOA(ipadd) from (SELECT DISTINCT(DestinationAddress) as ipadd FROM FLOWS "
                    + "WHERE DestinationAddress not in (select ip from DNS_LOOKUP_sf) and DateTimeInitiated between "+ "'"+StartSecsStr+"' and '"+EndSecsStr+"') as t" ;
            ResultSet rset = stmt.executeQuery(retrieve);
            
            HashSet<String> ipAddresses = new HashSet<>();
            while (rset.next()) {
                String ip = rset.getString(1);
                if (!ip.isEmpty()) {
                    ipAddresses.add(ip);
                }
            }
            System.out.println("IP collection complete\nBeginning DNS lookups");
           
            //Perform a lookup on each IP.
            ExecutorService executorService = Executors.newFixedThreadPool(40);
            List<Callable<String[]>> tasklst = new ArrayList<Callable<String[]>>();
            for (String ip : ipAddresses) {
                tasklst.add(new ReverseDNSLookupThread(ip));
            }            
            try {
                List<Future<String[]>> HostLst= executorService.invokeAll(tasklst);
                try {
                    for (Future<String[]> hostname : HostLst) {                                           
                        if(!hostname.get()[0].equals(hostname.get()[1])){
                            //extract domain
                            String slddomain = sld.extract2LD(hostname.get()[1].substring(0,hostname.get()[1].length()-1));
                            //insert ip with positive dns reverse lookup result to table.
                            retrieve = "REPLACE INTO DNS_LOOKUP_sf VALUES (INET6_ATON('" + hostname.get()[0] + "'), '" + hostname.get()[1] + "','"+ slddomain+"'," + " NOW()" + ")";
                            stmt.executeUpdate(retrieve);   
                        }
                        
                    }
                } catch (ExecutionException ex) { ex.printStackTrace(); }
            }
            catch(InterruptedException ex){
                System.out.println("Threads pool interrupted");
                executorService.shutdown();
            }
            executorService.shutdown();
            System.out.println("DNS lookups complete");
            
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
        } catch (Exception ex){
            ex.printStackTrace();
        }
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
    
    //Netflow use different data type for ip address in Mysql thus another table is created with signed int format ip address colomn
    public static boolean createDNSTablesFlow() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Create the table.
            String create = "CREATE TABLE IF NOT EXISTS DNS_LOOKUP_sf (ip VARBINARY(16) NOT NULL, "
                    + "hostname TEXT, "
                    + "domain TEXT, "
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
    
        //Netflow use different data type for ip address in Mysql thus another table is created with signed int format ip address colomn
    public static boolean createDNSTableNetflow() {
        try {
            //Load the driver.
            Class.forName("com.mysql.jdbc.Driver");

            //Create a connection.
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + DatabaseProperties.getDatabase(), DatabaseProperties.getUser(), DatabaseProperties.getPassword());

            //Create a statement object to use.
            Statement stmt = conn.createStatement();

            //Create the table.
            String create = "CREATE TABLE IF NOT EXISTS DNS_LOOKUP_Nf (ip int NOT NULL, "
                    + "hostname TEXT, "
                    + "domain TEXT, "
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

