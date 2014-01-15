package netflow;

import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 *
 * @author Keenan
 */
public class PacketCapture extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketCapture(DatagramPacket received)
    {
        receivedPacket = received;
    }
    
    @Override
    public void run()
    {
        //Do we need to recreate the DatagramPacket just to be sure this
        //packet is not the same object used by other threads?
        SavePacket(receivedPacket);
    }
    
    //Assumes the packet is NetFlow v5
    private void SavePacket(DatagramPacket receivedPacket)
    {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength());
        DataInputStream in = new DataInputStream(byteIn);

        /*String input = "";
        while( (input = in.readLine()) != null)
        {
            System.out.println(input + "\n");
        }*/
    }
}