/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author Keenan
 */
public class Capture extends Thread
{
    final int RECEIVE_PORT = 2055; //2055 as default for receiving NetFlow data.
    
    @Override
    public void run()
    {
        CapturePackets();
    }
    
    private void CapturePackets()
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