/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/util/SendMail.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.util;

import java.io.*;
import java.net.*;
import com.inqwell.any.*;

/**
 * The <code>SendMail</code> generates MIME emails (currently application/octet-stream only) 
 * and sends them to an SMTP host.
 */
public class SendMail implements Runnable, SendMailConstants
{
	private static final Any version__ = 
		new ConstString("$Revision: 1.2 $");
	
	private Thread t_;
	private Map email_;
	private boolean debug_ = false;

	/**
	 * Send some mail as a sperate Thread since it can be a time consuming activity
	 * The <code>Map</code> must contain at least the <code>FROMKEY</code> and <code>TOKEY</code> entries.
	 * All the values are <code>StringI</code>s except TOKEY which must be an <code>Array</code>
	 * of <code>StringI</code>s.  
	 * @see SendMailConstants
	 */
	public SendMail(Map email, boolean debug)
	{
		email_ = email;
		debug_ = debug;
		
		t_ = new Thread(this);
		t_.start();		
	}
	
	public SendMail(Map email)
	{
		this(email, false);
	}

	public void run()
	{
		SendMailSession sess = null;
		try
		{
			sess = new SendMailSession(email_, debug_);
			sess.readIncoming();
			sess.helo();
			sess.rset();
			sess.mailFrom();			
			sess.rcptTo();
			sess.message();
			sess.endOfMessage();
			sess.quit();
			sess.close();
		}
		catch(IOException ioX)
		{
			// Oh well...!
			System.err.println("Sendmail: Exception: " + ioX);
			sess.close();
		}
	}
	
	/**
	 * Inner class to handle the detailed communications
	 * with the smtp host
	 */
	class SendMailSession
	{
		String			incoming_;
		Socket			socket_;
		BufferedReader	din_;
		PrintWriter		prout_;
		boolean 		debug_ = false;
		Map				email_;
		
		Any get(Any key)
		{
			Any ret = null;
      
      if (email_ != null && email_.contains(key))
        ret = email_.get(key);

      return ret;
		}
				
		SendMailSession(Any email) throws IOException
		{
			email_ = (Map)email;
			
			// if exists, get the mail host name from the email any 
			Any host = get(MAILHOSTKEY);
			
			if (host == null)
				host = SMTPHOST;
		
			socket_ = new Socket(host.toString(), SMTPPORT.getValue());
			incoming_ = new String();
			
			InputStream inStream = socket_.getInputStream();
			din_ = new BufferedReader(new InputStreamReader(inStream));
			
			OutputStream outStream = socket_.getOutputStream();
			prout_ = new PrintWriter(outStream);
		}
		
		SendMailSession(Any email, boolean debug) throws IOException
		{
			this(email);
			debug_ = debug;
		}			
		
		protected void finalize() throws Throwable
		{
			super.finalize();
			socket_.close();
		}
		
		void readIncoming()
		{
			try
			{
				// Ensure the stream is clear before proceeding
				do
				{
					// Read mail server's reply
					incoming_ = din_.readLine();

					if (debug_)				
						System.out.println(incoming_);
				}
				while (din_.ready());			
			}
			catch(IOException ioX)
			{
				// Oh well...
				System.err.println("SendMail: Exception " + ioX);
				close();
			}
		}
		
		void helo()
		{
			// Say HELO to mail server...
			prout_.println(HELO);
			prout_.flush();
			readIncoming();
		}

		void rset()
		{
			// reset mail server...
			prout_.println(RSET);
			prout_.flush();
			readIncoming();
		}

		void quit()
		{
			// reset mail server...
			prout_.println(QUIT);
			prout_.flush();
			readIncoming();
		}

		
		void mailFrom()
		{
			// Tell mail server who we are
			prout_.print(MAILFROM);
			Any from = get(FROMKEY);
			prout_.println(from);			
			prout_.flush();
			readIncoming();
		}
		
		void rcptTo()
		{
			// Tell mail server who it's sending the mail to...			
			Array toArray = (Array)get(TOKEY);
			Iter i = toArray.createIterator();
			while (i.hasNext())
			{
				prout_.print(RCPTTO);
				prout_.println(i.next().toString());
				prout_.flush();
				readIncoming();
			}				
		}
		
		void message()
		{
			// Warn mail server it is about to receive the message contents...
			prout_.println(DATA);
			prout_.flush();
			readIncoming();
			
			// Send the Subject: (Optional)
			Any sub = get(SUBKEY);
			if (sub != null)
			{
				prout_.print(SUBJECT);
				prout_.println(sub);
			}

			// Send the To: (Required)
			Array toArray = (Array)get(TOKEY);
			int size = toArray.entries();
			prout_.print(TO);
			Iter i = toArray.createIterator();
			while (i.hasNext())
			{
				Any rcpt = i.next();
				prout_.print(rcpt);
				size--;
				if (size > 0) 
					prout_.print(", ");
			}
			prout_.println();						
			
			// Send the From: (Required)
			Any from = get(FROMKEY);
			prout_.print(FROM);
			prout_.println(from);

			// Now for the Mime types to send the message body
			Any boundary = new ConstString("com.inqwell.any.util.SendMail.Boundary.1234");
			prout_.println("X-Mailer: com.inqwell.any.util.SendMail version " + version__);
			prout_.println("Mime-Version: 1.0");
			prout_.println("Content-Type: multipart/mixed; boundary=\"" + 
				boundary + "\"");
			prout_.println("\n\nThis is a multipart message in MIME format.");
			prout_.println("If you see this message - your mail reading\nclient doesn't support MIME properly");
			prout_.println("\n--" + boundary);
			prout_.println("Content-Type: text/plain; charset=\"us-ascii\"");
			prout_.println("Content-Disposition: inline");
			
			// send the text: (Optional)
			Any text = get(TEXTKEY);
			if (text != null)
			{
				prout_.println();
				prout_.println(text);
			}
			
			// now do any file attachments
			StringI attachData = (StringI)get(ATTACHKEY);
			Any attachName = get(ATTNAMEKEY);
			if (attachData != null || attachName != null)
			{
				prout_.println("\n--" + boundary);
				
				prout_.println("Content-Type: application/octet-stream; name=\"" + attachName + "\"");
				prout_.println("Content-Transfer-Encoding: Base64");
				prout_.println("Content-Disposition: inline; filename=\"" + attachName + "\"\n");
								
				sendAttachment(attachData);
			}
			
			prout_.println("\n--" + boundary + "--");
			prout_.println();

			prout_.flush();			
		}

		void sendAttachment(StringI attachData)
		{
			String encStr = Base64.encodeData(attachData.getValue());

			// write out the emcoded attachment 74 characters at a time
			int numChars = encStr.length();
			int numLines = numChars/74;
			int i;
			for (i = 0; i < numLines; i++)
			{
				prout_.println(encStr.substring(i*74, i*74 + 74));
			}
			
			// Now write out the remainder
			if (numLines*74 < numChars)
			{
				prout_.println(encStr.substring(i*74, numChars));
			}
		}


		void endOfMessage()
		{
			prout_.println();
			prout_.println(ENDOFEMAIL);
			prout_.flush();			
			readIncoming();
		}
		
		void close()
		{
			// close the socket
			try
			{
				socket_.close();
			}
			catch (IOException ioX)
			{
				// oh well....
				System.err.println("SendMail: Exception: " + ioX);
			}
		}
	}


}
