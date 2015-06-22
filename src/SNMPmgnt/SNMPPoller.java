/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SNMPmgnt;

import com.adventnet.snmp.beans.DataException;
import com.adventnet.snmp.beans.ResultAdapter;
import com.adventnet.snmp.beans.ResultEvent;
import com.adventnet.snmp.beans.SnmpPoller;

/**
 * Poll single Object
 * @author gxia
 *  Usage: sPoller = new SNMPPoller("localhost","LAB","./HOST-RESOURCES-MIB",".1.3.6.1.2.1.25.1.1.0")
            sPoller.setPollInterval(10);
            sPoller.start(); 
 */
public class SNMPPoller {

    SnmpPoller poller;
    private String hostname;
    private String community;
    private String MIBresource;
    private String OID;
    private int interval = 1; //seconds, set to 0 for one time poll
    
    public void setPollInterval(int inter){
        this.interval = inter;
    }
    
    public SNMPPoller(String host,String comm,String MIBres,String OID){
        poller = new SnmpPoller();
        
        try {
            this.hostname = host;
            this.community = comm;
            this.MIBresource = MIBres;
            this.OID = OID;
                        
            poller.loadMibs(this.MIBresource);
            poller.setTargetHost(this.hostname);
            poller.setCommunity(this.community);
            poller.setObjectID(this.OID);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    public void start(){
        // We need to add a listener to listen for responses
        ResultAdapter listener = new ResultAdapter() {

            // This method will be invoked when the response is received
            public void setResult( ResultEvent e ) {
                try { 
                    System.out.println(e.getStringValue()); //replace by other operation if necessary
                } catch (DataException de) {
                    System.out.println("Error in getting agent data: "+de + e.getErrorString());
                }
            }
        };
    poller.setPollInterval(interval);
    poller.addResultListener(listener);  // listen for response events
    }
    
    
}
