package jsflow.header;

import java.util.Vector;

import jsflow.util.Utility;
import jsflow.util.UtilityException;

/**
 * Added support for Flow Sample data types.
 * @author Wei Kang Lim
 * @since  2/19/2014
 *
 */
public class FlowSampleHeader extends GenericFlowSampleHeader{
	public static FlowSampleHeader parse(byte[] data) throws UtilityException, HeaderParseException  {
		if (data.length < 32) throw new HeaderParseException("Data array too short. FlowSampleHeader has length " + data.length);
		FlowSampleHeader fsh = new FlowSampleHeader();
		// sample sequence number
		byte[] seqNumber = new byte[4];
		System.arraycopy(data, 0, seqNumber, 0, 4);
		fsh.setSequenceNumber(Utility.fourBytesToLong(seqNumber));
		// source id type
		byte[] sourceIDType = new byte[4];
		System.arraycopy(data, 4, sourceIDType, 0, 4);
		fsh.setSourceIDType(Utility.fourBytesToLong(sourceIDType));
		// sampling rate
		byte[] samplingRate = new byte[4];
		System.arraycopy(data, 8, samplingRate, 0, 4);
		fsh.setSamplingRate(Utility.fourBytesToLong(samplingRate));
		// sample pool
		byte[] samplePool = new byte[4];
		System.arraycopy(data, 12, samplePool, 0, 4);
		fsh.setSamplePool(Utility.fourBytesToLong(samplePool));
		// drops
		byte[] drops = new byte[4];
		System.arraycopy(data, 16, drops, 0, 4);
		fsh.setDrops(Utility.fourBytesToLong(drops));
		// input
		byte[] input = new byte[4];
		System.arraycopy(data, 20, input, 0, 4);
		fsh.setInput(Utility.fourBytesToLong(input));
		// output 
		byte[] output = new byte[4];
		System.arraycopy(data, 24, output, 0, 4);
		fsh.setOutput(Utility.fourBytesToLong(output));
		// number flow records
		byte[] numberFlowRecords = new byte[4];
		System.arraycopy(data, 28, numberFlowRecords, 0, 4);
		fsh.setNumberFlowRecords(Utility.fourBytesToLong(numberFlowRecords));
		
		// flow records
		int offset = 32;
		for (int i = 0; i < fsh.getNumberFlowRecords(); i++) {
			byte[] subData = new byte[data.length - offset]; 
			System.arraycopy(data, offset, subData, 0, data.length - offset);
			FlowRecordHeader fr = FlowRecordHeader.parse(subData);
			fsh.addFlowRecord(fr);
			offset += (fr.getFlowDataLength()+8); // Flow data + length header +  header protocol
		}
		return fsh;
	}
	
	public byte[] getBytes() throws HeaderBytesException {
		try {
			int lengthFlowRecords = 0;
			for (FlowRecordHeader fr : super.getFlowRecords()) {
				lengthFlowRecords += (fr.getFlowDataLength() + 8);
			}
			byte[] data = new byte[44 + lengthFlowRecords];
			// sequence number
			System.arraycopy(Utility.longToFourBytes(super.getSequenceNumber()), 0, data, 0, 4);
			// source id type
			System.arraycopy(Utility.longToFourBytes(super.getSourceIDType()), 0, data, 4, 4);
			// sampling rate
			System.arraycopy(Utility.longToFourBytes(super.getSamplingRate()), 0, data, 8, 4);
			// sample pool
			System.arraycopy(Utility.longToFourBytes(super.getSamplePool()), 0, data, 12, 4);
			// drops
			System.arraycopy(Utility.longToFourBytes(super.getDrops()), 0, data, 16, 4);
			// input interface 
			System.arraycopy(Utility.longToFourBytes(super.getInput()), 0, data, 20, 4);
			// output interface 
			System.arraycopy(Utility.longToFourBytes(super.getOutput()), 0, data, 24, 4);
			// number flow records
			System.arraycopy(Utility.longToFourBytes(super.getNumberFlowRecords()), 0, data, 28, 4);
			
			int offset = 0;
			for (FlowRecordHeader fr : super.getFlowRecords()) {
				byte[] temp = fr.getBytes();
				System.arraycopy(temp, 0, data, 44 + offset, temp.length);
				offset += (fr.getFlowDataLength() + 8);
			}
			return data;
		} catch (Exception e) {
			throw new HeaderBytesException("Error while generating the bytes: " + e.getMessage());
		}
	}
	
	public String toString(){
		String retVal = "\n[ExpandedFlowSampleHeader]" + "\n\tSequenceNumber=" + this.getSequenceNumber()
													 + "\n\tSourceIDtype=" + this.getSourceIDType()
													 + "\n\tSamplingRate=" + this.getSamplingRate()
													 + "\n\tSamplePool=" + this.getSamplePool()
													 + "\n\tDrops=" + this.getDrops()
													 + "\n\tInput=" + this.getInput()
													 + "\n\tOutput=" + this.getOutput()
													 + "\n\tFlowRecords(" + this.getNumberFlowRecords() + ")";
		for(FlowRecordHeader frh : super.getFlowRecords()){
			retVal += "\t" + frh;
		}
		return retVal;
	}
}
