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
	final Any FROMKEY     = new ConstString("from");
	final Any TOKEY	      = new ConstString("to");
	final Any CCKEY       = new ConstString("cc");
	final Any BCCKEY      = new ConstString("bcc");
	final Any SUBKEY      = new ConstString("subject");
	final Any TEXTKEY     = new ConstString("text");
  final Any BODYKEY     = new ConstString("body");
  final Any CONTENTKEY  = new ConstString("content");
	final Any ATTACHKEY	  = new ConstString("attachment");
	final Any CONTENTTYPE = new ConstString("contentType");
	final Any CONTENTDISP = new ConstString("contentDisposition");
	final Any CONTENTHDRS = new ConstString("headers");
	final Any ATTNAMEKEY  = new ConstString("attName");
	final Any MAILHOSTKEY = new ConstString("smtphost");
	final Any MAILPORTKEY = new ConstString("port");
	final Any DEBUGKEY    = new ConstString("debug");
	final Any ERROR       = new ConstString("error");
}
