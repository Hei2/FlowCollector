package netflow;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

/**
 *
 * @author Keenan
 */
public class Capture extends Thread
{
    int RECEIVE_PORT;//2055 as default for receiving NetFlow data.
    
    @Override
    public void run()
    {
        CapturePackets();
    }
    
    private void CapturePackets()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the port number to listen on (Incorrect ports will CRASH): ");
        RECEIVE_PORT = scanner.nextInt();
        scanner.close();
        
        System.out.println("Capturing packets on port: " + RECEIVE_PORT);
        try
        {
            DatagramSocket serverSocket = new DatagramSocket(RECEIVE_PORT);
            byte[] receiveData = new byte[1584];
            /*
             * V1 max size = 17 + 24 * 49 = 1193
             * V5 max size = 24 + 30 * 48 = 1464
             * V6 max size = 24 + 30 * 52 = 1584
             * V7 max size = 24 + 30 * 52 = 1584
             * V8 max size = ???
             * V9 max size = ???
             */
            
            while (true)
            {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                
                PacketCapture packetCapture = new PacketCapture(receivedPacket);
                packetCapture.start();
                
                System.out.println("Received packet");
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
    /*private void CapturePackets()
    {
        System.out.println("Capturing packets on port: " + RECEIVE_PORT);
        try
        {
            DatagramSocket serverSocket = new DatagramSocket(RECEIVE_PORT);
            byte[] receiveData = new byte[48]; //48 for NetFlow V5
            
            while (true)
            {
                DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivedPacket);
                ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength());
                DataInputStream in = new DataInputStream(byteIn);
                String input = "";
                
                System.out.println("Received packet");
                
                while( (input = in.readLine()) != null)
                {
                    System.out.println(input + "\n");
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
    }*/
}