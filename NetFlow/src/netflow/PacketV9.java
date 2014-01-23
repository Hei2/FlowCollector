package netflow;

/**
 *
 * @author Keenan
 */
public class PacketV9
{
    /*enum FieldTypes {
        IN_BYTES, IN_PKTS, FLOWS, PROTOCOL, SRC_TOS, TCP_FLAGS, L4_SRC_PORT,
        IPV4_SRC_ADDR, SRC_MASK, INPUT_SNMP, L4_DST_PORT, IPV4_DST_ADDR,
        DST_MASK, OUTPUT_SNMP, IPV4_NEXT_HOP, SRC_AS, DST_AS, BGP_IPV4_NEXT_HOP,
        MUL_DST_PKTS, MUL_DST_BYTES, LAST_SWITCHED
    }*/
    
    /*
     * Defined at: http://netflow.caligare.com/netflow_v9.htm
     * !!UNFINISHED!!
     */
    private static final int[][] FieldTypes = new int[][]
    {
        {1, 4}, {2, 4}, {3, 4}, {4, 1}, {5, 1}, {6, 1}, {7, 2}, {8, 4}, {9, 1},
        {10, 2}, {11, 2}, {12, 4}, {13, 1}, {14, 2}, {15, 4}, {16, 2}, {17, 2},
        {18, 4}, {19, 4}, {20, 4}
    };
    
    /*
     * IDEAS: Use a HashMap<Integer, String> connecting Value to Field Type.
     * Store the template as an ArrayList(?) of Values. When reading through a
     * Record, use the template to determine data types to store in another
     * ArrayList of data associated with a Value. When storing the data in the
     * database, use the Value in the 2nd ArrayList to find the Field Type in
     * the HashMap to construct an INSERT statement...
     * 
     * !IMPORTANT! Templates will need to be saved to the database.
     */
}
