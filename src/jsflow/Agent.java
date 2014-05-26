/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jsflow;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Agent is a representation of the router which the sflow data is coming from.
 * As each agent may have multiple sub-agents (different physical interfaces),
 * analysis can only be done at the sub agent level.
 * @author weika_000
 */
public class Agent {
    private String ipAddress;
    private ArrayList<SubAgent> subAgents;
    
    public Agent(String ipAddress){
        subAgents = new ArrayList<>();
        this.ipAddress = ipAddress;
    }
    
    public void add(SubAgent sa){
        subAgents.add(sa);
    }
    
    public void clear(){
        subAgents.clear();
    }
    
    public SubAgent get(String id){
        for(SubAgent sa : subAgents){
            if(sa.getId().equals(id)){
                return sa;
            }
        }
        
        return null;
    }
    
    public ArrayList<SubAgent> getSubAgents(){
        return subAgents;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof Agent){
            return ((Agent) o ).getIpAddress().equals(this.getIpAddress());
        } else {
            return false;
        }
    }
    
    public void printFlows(){
        System.out.println(this.getIpAddress());
        for(SubAgent sa : subAgents){
            sa.printFlows();
        }
    }
    
    public void printTop(){
        ArrayList<TrafficNode> nodes = new ArrayList<>();
        for(SubAgent sa : subAgents){
            nodes.addAll(sa.aggregateTraffic());
        }
        
        Collections.sort(nodes, (new TrafficNode.packetsSentComparator()));
        System.out.println("Top 10 Sources");
        System.out.println("---------------");
        for(int i = 0; i < Math.min(10, nodes.size()); i++){
            System.out.println(String.format("%d\t%s\t%.3f MB", i+1, nodes.get(i).getIpAddress(), nodes.get(i).getTotalKiloBytesSent() / 1024));
        }
        System.out.println();
        
        Collections.sort(nodes, (new TrafficNode.packetsReceivedComparator()));
        System.out.println("Top 10 Destinations");
        System.out.println("---------------");
        for(int i = 0; i < Math.min(10, nodes.size()); i++){
            System.out.println(String.format("%d\t%s\t%.3f MB", i+1, nodes.get(i).getIpAddress(), nodes.get(i).getTotalKiloBytesReceived() / 1024));
        }
    }
    
    public String toString(){
        return String.format("%s\n%s", this.getIpAddress() , this.getSubAgents().toString());
    }
}
