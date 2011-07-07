/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/util/SendMailConstants.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.util;

import com.inqwell.any.*;

public interface SendMailConstants
{
	// SMTP host and port number to connect to
	final IntI    SMTPPORT		= new ConstInt(25);
	final StringI SMTPHOST	= new ConstString("geuk_exc");
		
	// Protocol strings
	final String HELO      = "HELO";
	final String MAILFROM  = "MAIL FROM:";
	final String RCPTTO    = "RCPT TO:";
	final String RSET      = "RSET";
	final String QUIT      = "QUIT";

	final String DATA      = "DATA";
	final String SUBJECT   = "Subject: ";
	final String TO        = "To: ";
	final String CC        = "Cc: ";
	final String FROM      = "From: ";
	final String DATE      = "Date: ";
    
  final String GLOB_SMTP = "$catalog.mail.smtphost";
  final String PROC_FROM = "$process.mail.from";

	// mailhost returns either "220" or "250" to indicate everything went OK
	//final String OKCMD 		 =  "220|250";
	final String ENDOFEMAIL  = ".";

	// keys into the email composite
	final Any FROMKEY     = new AnyString("from");
	final Any TOKEY	      = new AnyString("to");
	final Any CCKEY       = new AnyString("cc");
	final Any BCCKEY      = new AnyString("bcc");
	final Any SUBKEY      = new AnyString("subject");
	final Any TEXTKEY     = new AnyString("text");
  final Any BODYKEY     = new AnyString("body");
  final Any CONTENTKEY  = new AnyString("content");
	final Any ATTACHKEY	  = new AnyString("attachment");
	final Any ATTNAMEKEY  = new AnyString("attName");
	final Any MAILHOSTKEY = new AnyString("smtphost");
	final Any MAILPORTKEY = new AnyString("port");
	final Any DEBUGKEY    = new AnyString("debug");
	final Any ERROR       = new AnyString("error");
}
