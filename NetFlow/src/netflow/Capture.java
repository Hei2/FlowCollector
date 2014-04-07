package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
    
    /**
     * Determine the version of the packet being received, and then create a
     * thread for the appropriate packet object to read the packet and store it
     * into the database.
     */
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
                
                //Determine the version of the packet.
                ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, 2);
                DataInputStream in = new DataInputStream(byteIn);
                short version = in.readShort();
                
                /*
                 * Consider changing the order of these if statements if it becomes
                 * clear that certain versions will be seen most often.
                 */
                if (version == 9)
                {
                    PacketV9 packetV9 = new PacketV9(receivedPacket);
                    packetV9.start();
                }
                else if (version == 8)
                {
                    PacketV8 packetV8 = new PacketV8(receivedPacket);
                    packetV8.start();
                }
                else if (version == 7)
                {
                    PacketV7 packetV7 = new PacketV7(receivedPacket);
                    packetV7.start();
                }
                else if (version == 6)
                {
                    PacketV6 packetV6 = new PacketV6(receivedPacket);
                    packetV6.start();
                }
                else if (version == 5)
                {
                    PacketV5 packetV5 = new PacketV5(receivedPacket);
                    packetV5.start();
                }
                else if (version == 1)
                {
                    PacketV1 packetV1 = new PacketV1(receivedPacket);
                    packetV1.start();
                }
                else
                {
                    System.out.println("Received an unexpected packet version: " + version);
                }
                
                System.out.println("Received packet: Version " + version);
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