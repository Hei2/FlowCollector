package jsflow.header;

import jsflow.util.Utility;
import jsflow.util.UtilityException;

public class TransportHeader {
	private int srcPort;
	private int destPort;
	
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
	
	public static TransportHeader parse (byte[] data) throws UtilityException{
		TransportHeader tph = new TransportHeader();
		byte [] src = new byte[2];
		System.arraycopy(data, 0, src, 0, 2);
		tph.setSrcPort(Utility.twoBytesToInteger(src));
		
		byte [] dest = new byte[2];
		System.arraycopy(data, 2, dest, 0, 2);
		tph.setDestPort(Utility.twoBytesToInteger(dest));

		return tph;
	}
	
	public String toString(){
		String retVal = "\n[TransportHeader]" + "\n\tDestinationPort=" + this.getDestPort()
										+ "\n\tSourcePort=" + this.getSrcPort() + ")";
		return retVal;
	}
}
