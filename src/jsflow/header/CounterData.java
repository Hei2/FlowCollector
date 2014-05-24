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


public class CounterData {
	byte[] data;
	
	public CounterData() {
	}
	
	public static CounterData parse(byte[] data) throws HeaderParseException {
		CounterData fd = new CounterData();
		fd.data = data;
		return fd;
	}
	
	public byte[] getBytes() throws HeaderBytesException {
		return data;
	}
}
