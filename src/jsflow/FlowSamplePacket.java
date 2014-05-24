package jsflow;
import java.util.Scanner;


public class FlowSamplePacket implements Comparable<FlowSamplePacket>{

	private String destIP;
	private String srcIP;
	private long sampleSeqNo;// sample packets uses unsigned int
	private long samplePool; // sample packets uses unsigned int
	private long packetSize;
	private int inputInterface;
	private int outputInterface;
	private int protocol;
	private int srcPort;
	private int destPort;
	private int inputVlan;
	private int outputVlan;
	private String source;
	
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(long packetSize) {
		this.packetSize = packetSize;
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
	public long getSampleSeqNo() {
		return sampleSeqNo;
	}
	public void setSampleSeqNo(long sampleSeqNo) {
		this.sampleSeqNo = sampleSeqNo;
	}
	public long getSamplePool() {
		return samplePool;
	}
	public void setSamplePool(long samplePool) {
		this.samplePool = samplePool;
	}
	
	public int getInputInterface() {
		return inputInterface;
	}

	public void setInputInterface(int inputInterface) {
		this.inputInterface = inputInterface;
	}

	public int getOutputInterface() {
		return outputInterface;
	}

	public void setOutputInterface(int outputInterface) {
		this.outputInterface = outputInterface;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public int getSourcePort() {
		return srcPort;
	}

	public void setSourcePort(int inputPort) {
		this.srcPort = inputPort;
	}

	public int getDestPort() {
		return destPort;
	}

	public void setDestPort(int outputPort) {
		this.destPort = outputPort;
	}

	public int getInputVlan() {
		return inputVlan;
	}

	public void setInputVlan(int inputVlan) {
		this.inputVlan = inputVlan;
	}

	public int getOutputVlan() {
		return outputVlan;
	}

	public void setOutputVlan(int outputVlan) {
		this.outputVlan = outputVlan;
	}


	public String toString(){
		return "\n\nPacket Information:" + 
				"\nSample Sequence: " + sampleSeqNo + 
				"\nSample Pool: " + samplePool +
				"\nSource: " + source +
				"\nSource IP: " + srcIP + 
				"\nDestination IP: " + destIP +
				"\nInput Interface: " + inputInterface + 
				"\nOutput Interface: " + outputInterface +
				"\nProtocol: " + protocol +
				"\nInput Port: " + srcPort +
				"\nOutput Port: " + destPort +
				"\nInput Vlan: " + inputVlan +
				"\nOutput Vlan: " + outputVlan;
	}
        
        public boolean equals(FlowSamplePacket f){
            return (this.srcIP.equals(f.getSrcIP()) && this.destIP.equals(f.getDestIP()));
        }
        
    @Override
    public int compareTo(FlowSamplePacket f) {
        if(this.getSrcIP().equals(f.getSrcIP())){
            return this.getDestIP().compareTo(f.getDestIP());
        } else {
            return this.getSrcIP().compareTo(f.getSrcIP());
        }
    }
}
