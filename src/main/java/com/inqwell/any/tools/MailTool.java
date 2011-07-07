/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/tools/MailTool.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */
package com.inqwell.any.tools;

import com.inqwell.any.*;
import com.inqwell.any.util.*;
import java.io.*;


public class MailTool implements SendMailConstants
{
	public static void main(String[] args)
	{
		CommandArgs cArgs = new CommandArgs(args);
		
		// "From" must be supplied
		AnyString from = new AnyString();
		if (!cArgs.arg("-from", from))
			usage();

		// "To" must be supplied could be many
		AnyString to = new AnyString();
		Array toArray = AbstractComposite.array();
		if (!cArgs.arg("-to", toArray, to))
			usage();

		// "Subject" must be supplied
		AnyString subject = new AnyString();
		if (!cArgs.arg("-sub", subject))
			usage();

		// "TextBody" must be supplied
		AnyString text = new AnyString();
		if (!cArgs.arg("-text", text))
			usage();

		boolean debug = false;
		if (cArgs.arg("-debug"))
			debug = true;

		// "FileAttachment" may be supplied
		AnyString attachFile = new AnyString();
		AnyString attachData = null;
		if (cArgs.arg("-attach", attachFile))
		{
			try
			{
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();				
				FileInputStream fin = new FileInputStream(attachFile.toString());
				while (fin.available() > 0)
				{
					buffer.write(fin.read());
				}				
				attachData = new AnyString(buffer.toString());
			}
			catch (IOException ioX)
			{
				System.out.println("Problems with " + attachFile + ", exception: " + ioX);
				System.exit(1);
			}			
		}
		
		// "MailHost" may be supplied
		StringI mailHost = new AnyString();
		if (!cArgs.arg("-host", mailHost)) 
		{
			// if not supplied then use hard-coded default
			mailHost = SMTPHOST;
		}
			
		// create the email composite		
		Map map = AbstractComposite.map();
		map.add(FROMKEY, from);
		map.add(TOKEY, toArray);
		map.add(SUBKEY, subject);
		map.add(TEXTKEY, text);
		map.add(MAILHOSTKEY, mailHost);
		if (attachData != null)
		{
			map.add(ATTACHKEY, attachData);
			// and the name of the attachment
			map.add(ATTNAMEKEY, attachFile);
		}

		if (debug)
			System.out.println("MailTool: email composite is " + map);
			
		com.inqwell.any.util.SendMail sm = new com.inqwell.any.util.SendMail(map, debug);
	}

	static void usage ()
	{
		System.out.println ("SendMail: Usage");
		System.out.println ("      -from <From>");
		System.out.println ("      -to <To, To,...,To>");
		System.out.println ("      -sub <Subject>");
		System.out.println ("      -text <Text>");
		System.out.println ("      [-attach <fileAttachment>");
		System.out.println ("      [-host <SMTPHost - default: " + 
			SendMailConstants.SMTPHOST + " >");
		System.out.println ("      [-debug]");
		System.exit(1);
	}
}
