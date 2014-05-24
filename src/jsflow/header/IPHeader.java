package jsflow.header;
/*
 * This file is part of jsFlow.
 *
 * 
 * Author:Wei Kang Lim <weikanglim@gmail.com>
 *
 * This software is licensed under the GNU Public License (GPL) license. A copy of 
 * the license agreement is included in this distribution.
 */
import jsflow.util.Address;
import jsflow.util.Utility;
import jsflow.util.UtilityException;

/**
 * Information regarding the Internet Layer: http://en.wikipedia.org/wiki/IPv4
 * @author wei
 *
 */
public class IPHeader {
	private int protocol;
	private String srcAddress;
	private String destAddress;
	private int ihl;
	private TransportHeader tph;
	
	public int getProtocol() {
		return protocol;
	}
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}
	public String getSrcAddress() {
		return srcAddress;
	}
	public void setSrcAddress(String srcAddress) {
		this.srcAddress = srcAddress;
	}
	public String getDestAddress() {
		return destAddress;
	}
	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}
	public TransportHeader getTph() {
		return tph;
	}
	public void setTph(TransportHeader tph) {
		this.tph = tph;
	}
	
	public static IPHeader parse (byte[] data) throws UtilityException{
		IPHeader iph = new IPHeader();
		int ihl = data[0] & 0x0F; // length in word (32 bits)
		iph.setProtocol(Utility.oneByteToInteger(data[9]));
		
		String srcIP = "";
		byte [] srcData = new byte[4];
		System.arraycopy(data, 12, srcData, 0, 4);
		Address srcAddress = new Address(srcData);
		iph.setSrcAddress(srcAddress.toString());
		
		byte [] destData = new byte[4];
		System.arraycopy(data, 16, destData, 0, 4);
		Address destAddress = new Address(destData);
		iph.setDestAddress(destAddress.toString());
		
		int ipLength = ihl*4; // in bytes
		byte subdata[] = new byte[data.length-ipLength];
		System.arraycopy(data, ipLength, subdata, 0, data.length - ipLength);
		iph.setTph(TransportHeader.parse(subdata));
		return iph;
	}
	
	public String toString(){
		String retVal = "\n[IPHeader]" + "\n\tDestination=" + this.getDestAddress()
										+ "\n\tSource=" + this.getSrcAddress()
										+ "\n\tprotocol=" + this.getProtocol();
		retVal += this.getTph();
		return retVal;
	}
}
