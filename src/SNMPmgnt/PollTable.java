/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SNMPmgnt;

import com.adventnet.snmp.beans.SnmpTable;
import com.adventnet.snmp.beans.SnmpTableEvent;
import com.adventnet.snmp.beans.SnmpTableListener;

/**
 * Poll entire table periodically
 * @author gxia
 * Usage: ptable = new PollTable("localhost","LAB","./HOST-RESOURCES-MIB","hrProcessorTable")
            ptable.setRetries(100);
            ptable.setPollInterval(10);
            ptable.start(); 
 */
public class PollTable {
    private String hostname;
    private String community;
    private String MIBresource;
    private String tableOID;
    private int retries = 10;
    private int interval = 60; //seconds, set to 0 for one time poll
    
    
    public PollTable(){
        
    }
    
    public PollTable(String host,String comm,String MIBres,String tableOID){
        this.hostname = host;
        this.community = comm;
        this.MIBresource = MIBres;
        this.tableOID = tableOID;
                
    }
    
    public void setRetries(int retry){
        this.retries = retry;
    }
    public void setPollInterval(int inter){
        this.interval = inter;
    }
    
    public void start(){
        try{
           
            SnmpTable table = new SnmpTable();
            table.setTargetHost( this.hostname ); // set the agent hostname
            table.setCommunity(this.community);
            table.loadMibs(this.MIBresource); // load MIBs  HOST-RESOURCES-MIB
            
            SnmpTableListener listener = new SnmpTableListener()
            {
               @Override
                public void tableChanged(SnmpTableEvent e) {
                    SnmpTable table = (SnmpTable)e.getSource();
                    StringBuffer sb = new StringBuffer();    
                    
                    // print column names, may be omitted
                    if (e.getFirstRow() == 0) {  
                      for (int i=0;i<table.getColumnCount();i++)  
                        sb.append(table.getColumnName(i)+" \t");
                      System.out.println(sb.toString());
                    }
                    
                    // print the rows we're getting and refreshed
                    sb = new StringBuffer();
                    if(e.getFirstRow() != -1) //no changes( -1 means row didn't change, refresh is considered as changed even value stays same)
                    {    
                        for (int j=e.getFirstRow();j<=e.getLastRow();j++) { 
                            for (int i=0;i<table.getColumnCount();i++) 
                                sb.append(table.getValueAt(j,i)+" \t"); //replace by other operation if necessary
                        }
                        System.out.println(sb.toString());
                    }
                }
            };

            table.addSnmpTableListener(listener);  // specify the listener
            table.setRetries(this.retries);
            table.setPollInterval(this.interval);

            table.setTableOID(this.tableOID);  // this starts polling of table data  hrProcessorTable
            table.startPollingTable();

        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
