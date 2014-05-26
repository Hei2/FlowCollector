/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jsflow;

/**
 * Represents a traffic node that is communicating in the network.
 * @author Wei Kang Lim
 */
public class TrafficNode {
    private String ipAddress;
    private double totalKiloBytesSent;
    private double totalKiloBytesReceived;
    private double totalPacketsSent;
    private double totalPacketsReceived;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public double getTotalKiloBytesSent() {
        return totalKiloBytesSent;
    }

    public void setTotalKiloBytesSent(double totalKiloBytesSent) {
        this.totalKiloBytesSent = totalKiloBytesSent;
    }

    public double getTotalKiloBytesReceived() {
        return totalKiloBytesReceived;
    }

    public void setTotalKiloBytesReceived(double totalKiloBytesReceived) {
        this.totalKiloBytesReceived = totalKiloBytesReceived;
    }

    public double getTotalPacketsSent() {
        return totalPacketsSent;
    }

    public void setTotalPacketsSent(double totalPacketsSent) {
        this.totalPacketsSent = totalPacketsSent;
    }

    public double getTotalPacketsReceived() {
        return totalPacketsReceived;
    }

    public void setTotalPacketsReceived(double totalPacketsReceived) {
        this.totalPacketsReceived = totalPacketsReceived;
    }
    
    @Override
    public boolean equals(Object n){
        if(n instanceof TrafficNode){
            return this.getIpAddress().equals(((TrafficNode) n).getIpAddress());
        } else{
            return false;
        }
    }
    
    public static class packetsSentComparator implements java.util.Comparator<TrafficNode>{
        @Override
        public int compare(TrafficNode o1, TrafficNode o2) {
            return (int) (o2.getTotalKiloBytesSent() - o1.getTotalKiloBytesSent());
        }       
    }
    
    public static class packetsReceivedComparator implements java.util.Comparator<TrafficNode>{
        @Override
        public int compare(TrafficNode o1, TrafficNode o2) {
            return (int) (o2.getTotalKiloBytesReceived() - o1.getTotalKiloBytesReceived());
        }       
    }

}
