/*
 * This file is part of jsFlow.
 *
 * Copyright (c) 2009 DE-CIX Management GmbH <http://www.de-cix.net> - All rights
 * reserved.
 * 
 * Author: Thomas King <thomas.king@de-cix.net>
 *
 * This software is licensed under the GNU Public License (GPL) license. A copy of 
 * the license agreement is included in this distribution.
 */
package jsflow.header;

import jsflow.util.Utility;
import jsflow.util.UtilityException;


public class SampleDataHeader {
	public final static long EXPANDEDFLOWSAMPLE = 3;
	public final static long EXPANDEDCOUNTERSAMPLE = 4;
	public final static long FLOWSAMPLE = 1;
	
	private long sampleDataFormat; // 20 bit enterprise & 12 bit format; standard enterprise 0, format 1, 2, 3, 4 
	private long sampleLength; // in byte
	
	private ExpandedFlowSampleHeader expandedFlowSampleHeader;
	private ExpandedCounterSampleHeader expandedCounterSampleHeader;
	private FlowSampleHeader flowSampleHeader;
	
	public SampleDataHeader() {
	}
	
	public void setSampleDataFormat(long format) {
		this.sampleDataFormat = format;
	}
	
	public void setSampleLength(long sampleLength) {
		this.sampleLength = sampleLength;
	}
	
	public long getSampleDataFormat() {
		return sampleDataFormat;
	}
	
	public long getSampleLength() {
		return sampleLength;
	}
	
	public void setExpandedFlowSampleHeader(ExpandedFlowSampleHeader efs) {
		expandedFlowSampleHeader = efs;
	}
	
	public ExpandedFlowSampleHeader getExpandedFlowSampleHeader() {
		return expandedFlowSampleHeader;
	}
	
	public void setExpandedCounterSampleHeader(ExpandedCounterSampleHeader ecs) {
		expandedCounterSampleHeader = ecs;
	}

	
	public ExpandedCounterSampleHeader getExpandedCounterSampleHeader() {
		return expandedCounterSampleHeader;
	}
	
	public void setFlowSampleHeader(FlowSampleHeader flowSampleHeader){
		this.flowSampleHeader = flowSampleHeader;
	}
	
	public FlowSampleHeader getFlowSampleHeader(){
		return flowSampleHeader;
	}
	
	public static SampleDataHeader parse(byte[] data) throws HeaderParseException, UtilityException   {
		if (data.length < 8) throw new HeaderParseException("Data array too short.");
		SampleDataHeader sdh = new SampleDataHeader();
		// format
		byte[] format = new byte[4];
		System.arraycopy(data, 0, format, 0, 4);
		sdh.setSampleDataFormat(Utility.fourBytesToLong(format));
		// length
		byte[] length = new byte[4];
		System.arraycopy(data, 4, length, 0, 4);
		sdh.setSampleLength(Utility.fourBytesToLong(length));
		
		if (sdh.getSampleDataFormat() == EXPANDEDFLOWSAMPLE) {
			byte[] subData = new byte[(int) sdh.getSampleLength()]; 
			System.arraycopy(data, 8, subData, 0, (int) sdh.getSampleLength());
			ExpandedFlowSampleHeader efs = ExpandedFlowSampleHeader.parse(subData);
			sdh.setExpandedFlowSampleHeader(efs);
		} 
		if (sdh.getSampleDataFormat() == EXPANDEDCOUNTERSAMPLE) {
			byte[] subData = new byte[(int) sdh.getSampleLength()];
			System.arraycopy(data, 8, subData, 0, (int) sdh.getSampleLength());
			ExpandedCounterSampleHeader ecs = ExpandedCounterSampleHeader.parse(subData);
			sdh.setExpandedCounterSampleHeader(ecs);
		}
		
		if (sdh.getSampleDataFormat() == FLOWSAMPLE) {
			byte[] subData = new byte[(int) sdh.getSampleLength()];
			System.arraycopy(data, 8, subData, 0, (int) sdh.getSampleLength());
			FlowSampleHeader  fs = FlowSampleHeader.parse(subData);
			sdh.setFlowSampleHeader(fs);
		}
		
		if ((sdh.getSampleDataFormat() != EXPANDEDFLOWSAMPLE) && (sdh.getSampleDataFormat() != EXPANDEDCOUNTERSAMPLE)
				&& (sdh.getSampleDataFormat() != FLOWSAMPLE)) {
			System.err.println("Sample data format not yet supported: " + sdh.getSampleDataFormat());
		}
		
		return sdh;	
	}
	
	public byte[] getBytes() throws Exception {
		int length = 0;
		byte[] expandedFlowSampleHeaderBytes = null;
		byte[] expandedCounterSampleHeaderBytes = null;
		if (expandedFlowSampleHeader != null) {
			expandedFlowSampleHeaderBytes = expandedFlowSampleHeader.getBytes();
			length += expandedFlowSampleHeaderBytes.length;
		}
		if (expandedCounterSampleHeader != null) {
			expandedCounterSampleHeaderBytes = expandedCounterSampleHeader.getBytes(); 
			length += expandedCounterSampleHeaderBytes.length;
		}
		byte[] data = new byte[8 + length];
		// format
		System.arraycopy(Utility.longToFourBytes(sampleDataFormat), 0, data, 0, 4);
		// length
		System.arraycopy(Utility.longToFourBytes(sampleLength), 0, data, 4, 4);
		
		// expanded flow sample header
		if (expandedFlowSampleHeader != null) System.arraycopy(expandedFlowSampleHeaderBytes, 0, data, 8, expandedFlowSampleHeaderBytes.length);
		// expanded counter sample header
		if (expandedCounterSampleHeader != null) System.arraycopy(expandedCounterSampleHeaderBytes, 0, data, 8, expandedCounterSampleHeaderBytes.length);
		return data;
	}
	
	public String toString(){
		String retVal = "\n[SampleHeader]" 	+ "\n\tFormat=" + this.sampleDataFormat 
											+ "\n\tLength=" + this.sampleLength;
		if(this.getSampleDataFormat() == EXPANDEDCOUNTERSAMPLE){
			retVal += expandedCounterSampleHeader;
		}else if(this.getSampleDataFormat() == EXPANDEDFLOWSAMPLE){
			retVal += expandedFlowSampleHeader;
		}else if(this.getSampleDataFormat() == FLOWSAMPLE){
			retVal += flowSampleHeader;
		}
		return retVal;
	}
}
