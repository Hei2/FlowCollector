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
            int sys_uptime = in.readInt();
            int unix_secs = in.readInt();
            
            /*
             * Depending on the version of the NetFlow packet, grab the 
             * relevant information.
             * 
             * Consider changing the order of these if statements if it becomes
             * clear that certain versions will be seen most often.
             */
            if (version == 9)
            {
                int package_sequence = in.readInt();
                int source_id = in.readInt();
                
                for (int i = 0; i < count; i++)
                {
                    
                }
            }
            /*else if (version == 8)
            {
                int unix_nsecs = in.readInt();
                int flow_sequence = in.readInt();
                byte engine_type = in.readByte();
                byte engine_id = in.readByte();
                byte aggregation = in.readByte();
                byte agg_version = in.readByte();
                in.skipBytes(4); //Ignoring reserved
                
                for (int i = 0; i < count; i++)
                {
                    
                }
            }*/
            else if (version == 1)
            {
                /*
                 * !!ADDRESS THIS!!
                 */
                in.skipBytes(5); //Skip unix_nsecs
                
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    int nexthop = in.readInt();
                    short input = in.readShort();
                    short output = in.readShort();
                    int dPkts = in.readInt();
                    int dOctets = in.readInt();
                    int first = in.readInt();
                    int last = in.readInt();
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(2); //Ignoring pad1
                    byte prot = in.readByte();
                    byte tos = in.readByte();
                    byte flags = in.readByte();
                    in.skipBytes(8); //Ignoring pad2
                }
            }
            else if (version == 5)
            {
                int unix_nsecs = in.readInt();
                int flow_sequence = in.readInt();
                byte engine_type = in.readByte();
                byte engine_id = in.readByte();
                short sampling_interval = in.readShort();
                
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    int nexthop = in.readInt();
                    short input = in.readShort();
                    short output = in.readShort();
                    int dPkts = in.readInt();
                    int dOctets = in.readInt();
                    int first = in.readInt();
                    int last = in.readInt();
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(1); //Skip pad1
                    byte tcp_flags = in.readByte();
                    byte prot = in.readByte();
                    byte tos = in.readByte();
                    short src_as = in.readShort();
                    short dst_as = in.readShort();
                    byte src_mask = in.readByte();
                    byte dst_mask = in.readByte();
                    in.skipBytes(2); //Ignoring pad2
                }
            }
            else if (version == 6)
            {
                int unix_nsecs = in.readInt();
                int flow_sequence = in.readInt();
                byte engine_type = in.readByte();
                byte engine_id = in.readByte();
                short sampling_interval = in.readShort();
                
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    int nexthop = in.readInt();
                    short input = in.readShort();
                    short output = in.readShort();
                    int dPkts = in.readInt();
                    int dOctets = in.readInt();
                    int first = in.readInt();
                    int last = in.readInt();
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(1); //Ignoring pad1
                    byte tcp_flags = in.readByte();
                    byte prot = in.readByte();
                    byte tos = in.readByte();
                    short src_as = in.readShort();
                    short dst_as = in.readShort();
                    byte src_mask = in.readByte();
                    byte dst_mask = in.readByte();
                    in.skipBytes(6); //Ignoring pad2
                }
            }
            else if (version == 7)
            {
                int unix_nsecs = in.readInt();
                int flow_sequence = in.readInt();
                in.skipBytes(4); //Ignoring reserved
                
                for (int i = 0; i < count; i++)
                {
                    int srcaddr = in.readInt();
                    int dstaddr = in.readInt();
                    int nexthop = in.readInt();
                    short input = in.readShort();
                    short output = in.readShort();
                    int dPkts = in.readInt();
                    int dOctets = in.readInt();
                    int first = in.readInt();
                    int last = in.readInt();
                    short srcport = in.readShort();
                    short dstport = in.readShort();
                    in.skipBytes(1); //Ignoring pad1
                    byte tcp_flags = in.readByte();
                    byte prot = in.readByte();
                    byte tos = in.readByte();
                    short src_as = in.readShort();
                    short dst_as = in.readShort();
                    byte src_mask = in.readByte();
                    byte dst_mask = in.readByte();
                    short flags = in.readShort();
                    int router_sc = in.readInt();
                }
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}