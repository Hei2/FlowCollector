/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

//http://netflow.caligare.com/netflow_v1.htm

/**
 *
 * @author Keenan
 */
public class PacketV1 extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketV1(DatagramPacket received)
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
            //!!CONSIDER OFFSET OF 2 SINCE VERSION IS CURRENTLY BEING SKIPPED!!
            ByteArrayInputStream byteIn = new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength());
            DataInputStream in = new DataInputStream(byteIn);

            try
            {
                //short version = in.readShort();
                in.skipBytes(2); //Skip version
                short count = in.readShort();
                int sys_uptime = in.readInt();
                int unix_secs = in.readInt();

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