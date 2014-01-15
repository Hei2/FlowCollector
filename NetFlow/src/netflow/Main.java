package netflow;

import java.util.Scanner;

//http://stackoverflow.com/questions/5160414/read-netflow-rflow-dd-wrt-packet-content
//http://stackoverflow.com/questions/10556829/sending-and-receiving-udp-packets-using-java

/**
 *
 * @author Keenan
 */
public class Main
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /*System.out.println("Enter an IP Address to search for: ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        scanner.close();
        System.out.println("\nThe name associated with that IP is:\n" + Functions.getHostName(ip) + "\n");*/
        
        Capture capture = new Capture();
        //capture.setDaemon(true);
        capture.start();
        //System.out.println(capture.toString());
    }
}