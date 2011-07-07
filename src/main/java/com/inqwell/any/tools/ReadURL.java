/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/tools/ReadURL.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */
package com.inqwell.any.tools;

import com.inqwell.any.*;
import com.inqwell.any.util.CommandArgs;
import java.io.*;
import java.net.*;


public class ReadURL
{
 
  public static void main (String args[]) throws IOException
  {
    if (args.length == 0)
      usage();
      
    CommandArgs cArgs = new CommandArgs(args);
    
    // URL statement must be supplied
    AnyString url = new AnyString();
    if (!cArgs.arg("-url", url))
      usage();
    
		// could have a file name
		AnyString filename = new AnyString();
		OutputStream outStr = System.out;
		if (cArgs.arg("-file", filename))
		{
			try
			{
				System.out.println ("Opening file " + filename);
				outStr = new FileOutputStream(filename.toString());
			}
			catch (IOException ioX)
			{
				System.out.println("Problems with " + filename + ", exception: " + ioX);
				System.exit(1);
			}
		}
		
		try
		{
			URL toOpen = new URL(url.toString());
			URLConnection c = toOpen.openConnection();
			InputStream is = c.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int readByte;
			while ((readByte = is.read()) >= 0)
			{
				buffer.write(readByte);
			}				
			outStr.write (buffer.toString().getBytes());
			//System.out.println (c.getContent().toString());
		}
		catch (Exception contX)
		{
			contX.printStackTrace();
			System.exit(1);
		}
	}		

  static void usage ()
  {
    System.out.println ("ReadURL: Usage");
    System.out.println ("      -url <URL>");
		System.out.println ("     [-file <filename>]");
    System.exit(1);
  }
}

