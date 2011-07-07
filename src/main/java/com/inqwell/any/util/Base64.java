/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/util/Base64.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.util;

/**
 * A utility class to allow Base64 encoding
 * Pulled from http://www.javaworld.com/javaworld/javatips/jw-javatip36.html
 */
public class Base64
{
	/**
	 * Base64 encode the attachment
	 * This code was pulled from
	 * http://www.javaworld.com/javaworld/javatips/jw-javatip36.html
	 */	
	public final static String encodeData(String data)
	{
		if (data == null)
			return null;

		byte byteData[] = data.getBytes();
		return new String(base64Encode(byteData));				
	}

	public final static byte[] base64Encode(byte[] byteData) 
	{
		if (byteData == null)  
			return  null;
		
		int iSrcIdx;      // index into source (byteData)
		int iDestIdx;     // index into destination (byteDest)
		byte byteDest[] = new byte[((byteData.length+2)/3)*4];

		for (iSrcIdx=0, iDestIdx=0; iSrcIdx < byteData.length-2; iSrcIdx += 3) 
		{
			byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx] >>> 2) & 077);
			byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx+1] >>> 4) & 017 |
									(byteData[iSrcIdx] << 4) & 077);
			byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx+2] >>> 6) & 003 |
									(byteData[iSrcIdx+1] << 2) & 077);
			byteDest[iDestIdx++] = (byte) (byteData[iSrcIdx+2] & 077);
		}

		if (iSrcIdx < byteData.length) 
		{
			byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx] >>> 2) & 077);
			if (iSrcIdx < byteData.length-1) 
			{
				byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx+1] >>> 4) & 017 |
										(byteData[iSrcIdx] << 4) & 077);
				byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx+1] << 2) & 077);
			}
			else
				byteDest[iDestIdx++] = (byte) ((byteData[iSrcIdx] << 4) & 077);
		}

		for (iSrcIdx = 0; iSrcIdx < iDestIdx; iSrcIdx++) 
		{
			if (byteDest[iSrcIdx] < 26)  
				byteDest[iSrcIdx] = (byte)(byteDest[iSrcIdx] + 'A');
			else if (byteDest[iSrcIdx] < 52)  
				byteDest[iSrcIdx] = (byte)(byteDest[iSrcIdx] + 'a'-26);
			else if (byteDest[iSrcIdx] < 62)  
				byteDest[iSrcIdx] = (byte)(byteDest[iSrcIdx] + '0'-52);
			else if (byteDest[iSrcIdx] < 63)  
				byteDest[iSrcIdx] = (byte)'+';
			else                              
				byteDest[iSrcIdx] = (byte)'/';
		}

		for ( ; iSrcIdx < byteDest.length; iSrcIdx++)
			byteDest[iSrcIdx] = (byte)'=';

		return byteDest;
	}
}
