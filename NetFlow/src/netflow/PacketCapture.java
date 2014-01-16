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
            short count = in.readShort();
            in.skipBytes(4);
            int unix_secs = in.readInt();
            
            /*
             * Depending on the version of the NetFlow packet, grab the 
             * relevant information.
             * !!DETERMINE WHAT NEEDS TO BE COLLECTED!!
             */
            if (version == 9)
            {
                //in.skipBytes();
            }
            else if (version == 8)
            {
                
            }
            else if (version == 1)
            {
                in.skipBytes(5); //Skip unix_nsecs
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    in.skipBytes(24); //Skip nexthop through last
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(2); //Skip pad1
                    byte prot = in.readByte();
                    in.skipBytes(10); //Ignoring tos through pad2
                }
            }
            else if (version == 5)
            {
                in.skipBytes(12); //Skip unix_nsecs through sampling_interval
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    in.skipBytes(24); //Skip nexthop through last
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(2); //Skip pad1 through tcp_flags
                    byte prot = in.readByte();
                    in.skipBytes(9); //Ignoring tos through pad2
                }
            }
            else if (version == 6) //!!CONSIDER CONSOLIDATING VERSIONS 6 & 7!!
            {
                in.skipBytes(12); //Skip unix_nsecs through sampling_interval
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    in.skipBytes(24); //Skip nexthop through last
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(2); //Skip pad1 through tcp_flags
                    byte prot = in.readByte();
                    in.skipBytes(13); //Ignoring tos through pad2
                }
            }
            else if (version == 7)
            {
                in.skipBytes(12); //Skip unix_nsecs through reserved
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    in.skipBytes(24); //Skip nexthop through last
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(2); //Skip pad1 through tcp_flags
                    byte prot = in.readByte();
                    in.skipBytes(13); //Ignoring tos through router_sc
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}