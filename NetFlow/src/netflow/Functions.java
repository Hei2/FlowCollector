package netflow;

import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 *
 * @author Keenan
 */
public class Functions
{
    public static String getHostName(String ip)
    {
        try
        {
            InetAddress ia = InetAddress.getByName(ip);
            String address = ia.getCanonicalHostName();
            return address;
        }
        catch (UnknownHostException e)
        {
            return "Unknown Host";
        }
    }
}