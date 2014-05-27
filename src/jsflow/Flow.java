/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsflow;

/**
 * Represents a one direction network Flow.
 * @author weika_000
 */
public class Flow implements Comparable<Flow>{
    private String destIP;
    private String srcIP;
    private int protocol;
    private int srcPort;
    private int destPort;
    private double kilobytesTransferred;
    private double noSamplePackets;
    private int packetsSent;


    public void readFrom(FlowSamplePacket fsp) {
        setDestIP(fsp.getDestIP());
        setSrcIP(fsp.getSrcIP());
        setProtocol(fsp.getProtocol());
        setSrcPort(fsp.getSourcePort());
        setDestPort(fsp.getDestPort());
    }
    
    public double getNoSamplePackets() {
        return noSamplePackets;
    }

    public void setNoSamplePackets(double noSamplePackets) {
        this.noSamplePackets = noSamplePackets;
    }

    public String getDestIP() {
        return destIP;
    }

    public void setDestIP(String destIP) {
        this.destIP = destIP;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public double getKilobytesTransferred() {
        return kilobytesTransferred;
    }

    public void setKilobytesTransferred(double kilobytesTransferred) {
        this.kilobytesTransferred = kilobytesTransferred;
    }
    
    public int getPacketsSent() {
        return packetsSent;
    }

    public void setPacketsSent(int packetsSent) {
        this.packetsSent = packetsSent;
    }

    @Override
    public int compareTo(Flow f) {
        if(this.getKilobytesTransferred() == f.getKilobytesTransferred()){
            return this.getDestIP().compareTo(f.getDestIP());
        } else {
            return (int) (this.getKilobytesTransferred() - f.getKilobytesTransferred());
        }
    }

}
