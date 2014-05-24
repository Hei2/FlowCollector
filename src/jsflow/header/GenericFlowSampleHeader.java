package jsflow.header;

import java.util.Vector;

public class GenericFlowSampleHeader {
	private long seqNumber;
	private long sourceIDType; // 0 = ifindex, 1 = smonVlanDataSource, 2 = entPhysicalEntry
	private long samplingRate;
	private long samplePool; // total number of packets that could have been sampled
	private long drops; // packets dropped due a lack of resources
	private long input; // (SNMP ifIndex of input interface, 0 if not known)
	private long output;  	// (SNMP ifIndex of output interface, 0 if not known)
						 	// broadcast or multicast are handled as follows:
						 	// the ﬁrst bit indicates multiple destinations, the lower order bits number of interfaces
	private long numberFlowRecords;
	
	private Vector<FlowRecordHeader> flowRecords;
	
	public GenericFlowSampleHeader(){
		flowRecords = new Vector<>();
	}
	
	public long getSequenceNumber() {
		return seqNumber;
	}

	public long getSourceIDType() {
		return sourceIDType;
	}

	public long getSamplingRate() {
		return samplingRate;
	}

	public long getSamplePool() {
		return samplePool;
	}

	public long getDrops() {
		return drops;
	}

	public long getInput() {
		return input;
	}

	public long getOutput() {
		return output;
	}

	public long getNumberFlowRecords() {
		return numberFlowRecords;
	}

	public void setSequenceNumber(long seqNumber) {
		this.seqNumber = seqNumber;
	}

	public void setSourceIDType(long sourceIDType) {
		this.sourceIDType = sourceIDType;
	}

	public void setSamplingRate(long samplingRate) {
		this.samplingRate = samplingRate;
	}

	public void setSamplePool(long samplePool) {
		this.samplePool = samplePool;
	}

	public void setDrops(long drops) {
		this.drops = drops;
	}

	public void setInput(long input) {
		this.input = input;
	}

	public void setOutput(long output) {
		this.output = output;
	}

	public void setNumberFlowRecords(long numberFlowRecords) {
		this.numberFlowRecords = numberFlowRecords;
	}
	
	public void addFlowRecord(FlowRecordHeader flowRecord) {
		flowRecords.add(flowRecord);
	}
	
	public Vector<FlowRecordHeader> getFlowRecords() {
		return flowRecords;
	}

}
