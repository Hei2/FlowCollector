/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

//http://netflow.caligare.com/netflow_v7.htm

/**
 *
 * @author Keenan
 */
public class PacketV7 extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketV7(DatagramPacket received)
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
     * Grabs all of the packet information.
     */
    private void SavePacket(DatagramPacket receivedPacket)
    {
        try
        {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength());
            DataInputStream in = new DataInputStream(byteIn);

            try
            {
                //short version = in.readShort();
                in.skipBytes(2); //Skip version
                short count = in.readShort();
                int sys_uptime = in.readInt();
                int unix_secs = in.readInt();
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
            finally
            {
                in.close();
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
}