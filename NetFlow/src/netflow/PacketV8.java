/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package netflow;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

//http://netflow.caligare.com/netflow_v8.htm

/**
 *
 * @author Keenan
 */
public class PacketV8 extends Thread
{
    DatagramPacket receivedPacket;
            
    public PacketV8(DatagramPacket received)
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
                byte engine_type = in.readByte();
                byte engine_id = in.readByte();
                byte aggregation = in.readByte();
                byte agg_version = in.readByte();
                in.skipBytes(4); //Ignoring reserved

                for (int i = 0; i < count; i++)
                {

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