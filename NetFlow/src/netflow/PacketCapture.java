package netflow;

import java.net.DatagramPacket;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

//NetFlow v9 RFC: http://www.ietf.org/rfc/rfc3954.txt
//Easier read for v9: http://netflow.caligare.com/netflow_v9.htm

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
    
    /*
     * First reads the version of the NetFlow packet. Accordingly grabs the 
     * rest of the packet information deemed relevant.
     */
    private void SavePacket(DatagramPacket receivedPacket)
    {
        try
        {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength());
            DataInputStream in = new DataInputStream(byteIn);

            short version = in.readShort();
            
            if (version == 9)
            {
                short count = in.readShort();
                in.skipBytes(4);
                int unix_secs = in.readInt();
                //in.skipBytes();
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}