/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SendMail.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-03 20:50:50 $
 */
package com.inqwell.any;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.inqwell.any.io.ByteStream;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.PrintStream;
import com.inqwell.any.util.Base64;
import com.inqwell.any.util.SendMailConstants;

/**
 * Send a mail via a given SMTP server.  The message and any
 * attachments are sent synchronously to the SMTP server. If
 * the message is lengthy and the calling thread does not want to
 * wait then it should dispatch the action to another process.
 * <p>
 * The mandatory single operand is a map containing well-known
 * keys to define the mail specification as follows:
 * <BL>
 * <LI><code>to</code> the recipients that will appear in
 * the <b>to:</b> header of the mail.
 * </LI>
 * <LI><code>cc</code> the recipients that will appear in
 * the <b>cc:</b> header of the mail.
 * </LI>
 * <LI><code>bcc</code> the recipients that will not appear in
 * the mail headers.  One of <code>to</code> or <code>bcc</code>
 * must be present for the specification to be valid.
 * </LI>
 * <LI><code>subject</code> the subject header of the mail, if any.
 * </LI>
 * <LI><code>from</code> the originator for the SMTP session
 * and the <code>from</code> header in the mail. If not present then
 * this value defaults to that at <code>$process.mail.from</code>.
 * </LI>
 * <LI><code>smtphost</code> the host to use for the SMTP session.
 * If not present then defaults to that
 * at <code>$catalog.mail.smtphost</code>.
 * <LI><code>port</code> the port to use for the SMTP session.
 * If not present then defaults to 25.
 * </LI>
 * <LI><code>body</code> the body text (as distinct from attachments)
 * for the email, if any.  This item must be either a string or an
 * open PrintStream from which the body lines will be read.
 * <P>The SMTP standard documented in RFC2821 says that lines can
 * be at most 1000 characters long.  Lines delivered in the body
 * text are wrapped at this length.
 * </LI>
 * <LI><code>attachment</code> any attachments that are to be
 * included in the mail.  Presently, all attachments are base-64
 * encoded and given a MIME type of application/octet-stream.
 * <P>
 * The attachment(s) must be a URL conforming to the supported
 * protocols that can be opened from the current Inq environment.
 * An Inq <code>ByteStream</code> is used to open the URL, that is
 * this class does not assume anything about the structure or content
 * of the attachment.  The name given in the attachment header is
 * that of the last path component of the path element of the URL.
 * </LI>
 * </LE>
 * <P>
 * The <code>to</code>, <code>cc</code>, <code>bcc</code>
 * and <code>attachment</code> components are assumed to
 * be <code>vector</code>s.
 * <P>
 * Any attachments are opened prior to establishing the SMTP
 * session and any failure at this point will mean that no mail
 * is sent.  Errors incurred during the processing of the
 * attachments into the mail will mean that an incomplete mail
 * will be sent.
 * <P>
 * The return value of this function is boolean <code>true</code>
 * if the mail was sent successfully or <code>false</code>
 * otherwise. If <code>false</code> is returned then the
 * mail specification is augmented to contain an <code>error</code>
 * element describing the problem encountered.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class SendMail extends    AbstractFunc
                      implements SendMailConstants,
                                 Cloneable
{

	private Any              mailInfo_;

  private static final String version__  = "$Revision: 1.6 $";
  private static final String boundary__ = "com.inqwell.any.SendMail.Boundary.1234";


	public SendMail(Any mailInfo)
	{
		mailInfo_ = mailInfo;
	}

	public Any exec(Any a) throws AnyException
	{
		Map mailInfo = (Map)EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          mailInfo_,
                                          Map.class);

		BooleanI ret = new AnyBoolean(true);

    boolean debug = validateMap(mailInfo, a);

    // If we get here then we can do the mail message

    int port = 25;

    if (mailInfo.contains(MAILPORTKEY))
    {
      IntI p = new ConstInt(mailInfo.get(MAILPORTKEY));
      port = p.getValue();
    }

    String host = mailInfo.get(MAILHOSTKEY).toString();

    Socket         socket = null;
    BufferedReader in     = null;
    PrintWriter    prout  = null;
    OutputStream   out    = null;
    try
    {
    	if (debug)
        System.out.println("Connecting to " + host + ":" + port);

      socket = new Socket(host, port);

      socket.setSoTimeout(30000);  // timeout on responses from server is 30s
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			if (debug)
				out = new TeeOutputStream(socket.getOutputStream(), new FileOutputStream("mail.raw.txt"));
			else
				out = socket.getOutputStream();
			
			prout = new PrintWriter(out);
    }
    catch (Exception e)
    {
    	if (prout != null)
    		prout.close();
    	
    	if (in != null)
    	{
    		try
    		{
      		in.close();
    		}
    		catch (Exception e2)
    		{
    			throw new RuntimeContainedException(e2);
    		}
    	}
      throw new ContainedException(e);
    }

    try
    {
      AnyInt responseCode = new AnyInt();
      StringWriter sw = new StringWriter();
      PrintWriter errorBuffer = new PrintWriter(sw);
      if (!doMail(in, prout, out, mailInfo, errorBuffer, responseCode, debug))
      {
        // Put the error buffer in the caller's map
        errorBuffer.flush();
        mailInfo.add(ERROR, new ConstString(sw.toString()));
        ret.setValue(false);
      }
    }
    finally
    {
    	close(prout, in);
    }
    return ret;
	}

  public Object clone () throws CloneNotSupportedException
  {
		SendMail c  = (SendMail)super.clone();

		c.mailInfo_ = AbstractAny.cloneOrNull(mailInfo_);

		return c;
  }

  private boolean doMail(BufferedReader in,
  		                   PrintWriter    prout,
  		                   OutputStream   os,
  		                   Map            mailInfo,
                         PrintWriter    errorBuffer,
                         IntI           responseCode,
                         boolean        debug) throws AnyException
  {
    // If there are any attachments, validate them first.  We cannot
    // stop a mail being sent once it has entered the DATA phase
    // of the protocol.
    Array attachments = null;
    if (mailInfo.contains(ATTACHKEY))
    {
      Vectored v = (Vectored)mailInfo.get(ATTACHKEY);
      attachments = validateAttachments(v, errorBuffer, responseCode);
      if (attachments == null)
        return false;
    }

    // Read the initial connect response line
    String str = readIncoming(in, responseCode, debug);
    if (!responseOK(responseCode, debug))
      return false;

    str = ehlo(in, prout, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      errorBuffer.println(str);
      return false;
    }

    str = mailFrom(in, prout, mailInfo, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      errorBuffer.println(str);
      return false;
    }

    rcptTo(in, prout, errorBuffer, mailInfo, responseCode, debug);
    if (!responseOK(responseCode, debug))
      return false;

    data(in, prout, os, errorBuffer, mailInfo, responseCode, attachments, debug);
    if (!responseOK(responseCode, debug))
      return false;

    quit(in, prout, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      errorBuffer.println(str);
      return false;
    }

    close(prout, in);
    return true;
  }

  private String ehlo(BufferedReader in, PrintWriter prout, IntI responseCode, boolean debug) throws AnyException
  {
    try
    {
      String localhost = InetAddress.getLocalHost().getHostName();

      writeCommand(prout, "EHLO " + localhost + "\r\n", debug);
      return readIncoming(in, responseCode, debug);
    }
    catch (UnknownHostException uhe)
    {
      throw new ContainedException(uhe);
    }
  }

  private String mailFrom(BufferedReader in,
                          PrintWriter    prout,
                          Map            mailInfo,
                          IntI           responseCode,
                          boolean        debug) throws AnyException
  {
    writeCommand(prout, MAILFROM + "<" + mailInfo.get(FROMKEY) + ">\r\n", debug);
    return readIncoming(in, responseCode, debug);
  }

  private void rcptTo(BufferedReader in,
  		                PrintWriter    prout,
  		                PrintWriter    errorBuffer,
                      Map            mailInfo,
                      IntI           responseCode,
                      boolean        debug) throws AnyException
  {
    boolean  rcptOK = false;

    Vectored to     = (Vectored)mailInfo.get(TOKEY);
    rcptOK = rcptToItems(in, prout, to, errorBuffer, responseCode, debug);

    // The only way that mail can actually be delivered
    // anywhere is via the RCPTTO command to the SMTP server.
    // The to: cc: and bcc: fields are just headers in the
    // SMTP message body that disclose (or otherwise) who
    // the recipients are.

    // Process any other recipients here.  Note that the RFCs
    // say that (at least the first of the) RCPTTO addresses
    // may get copied to the message header by the SMTP server,
    // though this is non-standard.  Thus bcc addresses may be
    // visible - consider revising this code should this happen
    // to send to each recipient with a completely separate
    // SMTP session (slower).

    boolean rcptOther;
    if (mailInfo.contains(CCKEY))
    {
      to = (Vectored)mailInfo.get(CCKEY);
      rcptOther = rcptToItems(in, prout, to, errorBuffer, responseCode, debug);
      if (!rcptOK)
        rcptOK = rcptOther;
    }

    if (mailInfo.contains(BCCKEY))
    {
      to = (Vectored)mailInfo.get(BCCKEY);
      rcptOther = rcptToItems(in, prout, to, errorBuffer, responseCode, debug);
      if (!rcptOK)
        rcptOK = rcptOther;
    }

    // As long as we got an OK response for at least one of the
    // specified to: addresses make sure the response code is OK
    if (rcptOK)
      responseCode.setValue(250);
  }

  private boolean rcptToItems(BufferedReader in,
  		                        PrintWriter    prout,
  		                        Vectored       recipients,
                              PrintWriter    errorBuffer,
                              IntI           responseCode,
                              boolean        debug) throws AnyException
  {
    String  s;
    boolean rcptOK = false;

    for(int i = 0; i < recipients.entries(); i++)
    {
      writeCommand(prout, RCPTTO + "<" + recipients.getByVector(i) + ">\r\n", debug);
      s = readIncoming(in, responseCode, debug);
      if (!responseOK(responseCode, debug))
        errorBuffer.println(s);
      else
        rcptOK = true;
    }

    return rcptOK;
  }

  private void data(BufferedReader in,
  		              PrintWriter    prout,
  		              OutputStream   os,
  		              PrintWriter    errorBuffer,
                    Map            mailInfo,
                    IntI           responseCode,
                    Array          attachments,
                    boolean        debug) throws AnyException
  {
    writeCommand(prout, DATA + "\r\n", debug);
    String s = readIncoming(in, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      errorBuffer.println(s);
      return;
    }

    writeHeader(prout, FROM + "<" + mailInfo.get(FROMKEY) + ">");

    writeRecipientsHeader(prout, TO, mailInfo.get(TOKEY));

    SimpleDateFormat sent = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
    writeHeader(prout, DATE + sent.format(new Date()));

    if (mailInfo.contains(CCKEY))
      writeRecipientsHeader(prout, CC, mailInfo.get(CCKEY));

    if (mailInfo.contains(SUBKEY))
    {
      Any a = mailInfo.get(SUBKEY);
      writeHeader(prout, SUBJECT + a.toString());
    }

    writeHeader(prout, "X-Mailer: com.inqwell.any.SendMail version " + Version.getVersion());

    boolean isMime = false;
    String contentType = "text/plain; charset=\"us-ascii\"";
    Any cType = mailInfo.getIfContains(CONTENTKEY);
    if (cType != null)
      contentType = cType.toString();

    if (attachments != null)
    {
      isMime = true;
      // If there are attachments then turn the body into a MIME message
      writeHeader(prout, "Mime-Version: 1.0");
			writeHeader(prout, "Content-Type: multipart/related; boundary=\"" +
                   boundary__ +
                   "\"");
      prout.print("\r\n");
			prout.print("This is a multipart message in MIME format.\r\n");
			prout.print("If you see this message, your mail reading client does not support MIME properly\r\n");
			prout.print("\r\n--" + boundary__);
      prout.print("\r\n");
			//writeHeader("Content-Type: text/plain; charset=\"us-ascii\"");
			writeHeader(prout, "Content-Type: " + contentType);
			writeHeader(prout, "Content-Disposition: inline");
    }
    else
      writeHeader(prout, "Content-Type: " + contentType);

    if (mailInfo.contains(BODYKEY))
    {
      // blank line before body text
      prout.print("\r\n");
      prout.flush();

      // The SMTP specification sets maximum line length of 1000
      // characters
      Any body = mailInfo.get(BODYKEY);
      if (body instanceof PrintStream)
      {
        Any a;
        PrintStream ps = (PrintStream)body;
        while ((a = ps.read()) != AnyNull.instance())
          dataString(prout, a.toString());
      }
      else if (body instanceof StringI)
      {
        dataString(prout, body.toString());
      }
      else
      {
        // The given Any is unacceptable as the body text.
        // We do not want to send a mail in these circumstances
        // so having got this far reset the SMTP server and
        // set an error response.
        endOfMessage(in, prout, errorBuffer, responseCode, debug);
        reset(in, prout, errorBuffer, responseCode, debug);
        responseCode.setValue(504);
        return;
      }
    }

    if (isMime)
    {
      // Do the attachments
      if (!doAttachments(prout, os, attachments, errorBuffer, responseCode))
      {
        // If any of the attachments fails then don't send the mail.
        // Since we have written some data, end the message and reset
        // the SMTP server so the message doesn't go
        endOfMessage(in, prout, errorBuffer, responseCode, debug);
        reset(in, prout, errorBuffer, responseCode, debug);
        responseCode.setValue(504);
        return;
      }
      // Ultimate boundary
      prout.print("\r\n--" + boundary__ + "--\r\n");
      prout.flush();
    }

    // Empty data is OK.  Terminate the data transfer
    endOfMessage(in, prout, errorBuffer, responseCode, debug);
  }

  private String quit(BufferedReader in, PrintWriter prout, IntI responseCode, boolean debug) throws AnyException
  {
    writeCommand(prout, "QUIT\r\n", debug);
    return readIncoming(in, responseCode, debug);
  }

  private boolean endOfMessage(BufferedReader in,
                               PrintWriter    prout,
  		                         PrintWriter    errorBuffer,
                               IntI           responseCode,
                               boolean        debug) throws AnyException
  {
    boolean ok = true;
    writeCommand(prout, ".\r\n", debug);
    String s = readIncoming(in, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      ok = false;
      errorBuffer.println(s);
    }
    return ok;
  }

  private boolean reset(BufferedReader in,
  		                  PrintWriter    prout,
  		                  PrintWriter    errorBuffer,
                        IntI           responseCode,
                        boolean        debug) throws AnyException
  {
    boolean ok = true;
    writeCommand(prout, "RSET\r\n", debug);
    String s = readIncoming(in, responseCode, debug);
    if (!responseOK(responseCode, debug))
    {
      ok = false;
      errorBuffer.println(s);
    }
    return ok;
  }

  private void dataString(PrintWriter prout, String s)
  {
    int len = s.length();
    if (len > 1000)
    {
      int idx = 0;
      int wrt = 1000;
      do
      {
        String s1 = s.substring(idx, idx+wrt);
        writeDataString(prout, s1);
        idx += wrt;
        wrt = ((idx + wrt) > len) ? len - idx
                                  : 1000;
      }
      while(idx < len);
    }
    else
    {
      writeDataString(prout, s);
    }
    prout.flush();
  }

  private void writeDataString(PrintWriter prout, String s)
  {
    // Just check if a single period escape is necessary
    if(s.length() == 1 && s.charAt(0) == '.')
      prout.print(".");

    prout.print(s);
    prout.print("\r\n");
  }

  private void writeCommand(PrintWriter prout, String cmd, boolean debug)
  {
    prout.print(cmd);
    prout.flush();
    if (debug)
      System.out.println(cmd);
  }

  private void writeHeader(PrintWriter prout, String hdr)
  {
    if (hdr.length() > 1000)
      hdr = hdr.substring(0,1000);

    prout.print(hdr);
    prout.print("\r\n");
  }

  private void writeRecipientsHeader(PrintWriter prout, String hdr, Any recipientsList)
  {
    Vectored recipients = (Vectored)recipientsList;

    prout.print(hdr);
    for (int i = 0; i < recipients.entries(); i++)
    {
      if (i != 0)
      {
        prout.print(",\r\n "); // fold the header
        //prout_.print(", "); // fold the header
      }

      prout.print("<" + recipients.getByVector(i) + ">");
    }
    prout.print("\r\n");
    prout.flush();
  }

  private boolean doAttachments(PrintWriter  prout,
  		                          OutputStream os,
  		                          Array        attachments,
                                PrintWriter  errorBuffer,
                                IntI         responseCode) throws AnyException
  {
    for (int i = 0; i < attachments.entries(); i++)
    {
      if (!doAttachment(prout,
      		              os,
      		              attachments.get(i),
                        errorBuffer,
                        responseCode))
        return false;
    }
    return true;
  }

  private boolean doAttachment(PrintWriter  prout,
  		                         OutputStream os,
  		                         Any          attachment,
                               PrintWriter  errorBuffer,
                               IntI         responseCode) throws AnyException
  {
    boolean ok = true;
    
    // Default content type and disposition
    String contentType        = "application/octet-stream";
    String contentDisposition = null;
    Any    otherHeaders       = null;
    
    if (attachment instanceof Map)
    {
    	Map m = (Map)attachment;
    	Any a;
    	
    	if ((a = m.getIfContains(CONTENTTYPE)) != null)
    		contentType = a.toString();
    	
    	if ((a = m.getIfContains(CONTENTDISP)) != null)
    		contentDisposition = a.toString();
    	
    	otherHeaders = m.getIfContains(CONTENTHDRS);
    	
    	attachment = m.get(ATTACHKEY);
    }

    // The attachment should be a URL we can open with a byte stream
    // that is then written to the mail message as base 64 encoded data.
    ByteStream   bs = new ByteStream(1023);

    if (!bs.open(getTransaction().getProcess(),
                 attachment,
                 PhysicalIO.read__))
    {
      errorBuffer.println("Can't process " + attachment + ", mail incomplete");
      return false;
    }

    try
    {
      // Work out the attachment name from the last path component
      String name = bs.getURL().getLastPath();
			prout.print("\r\n--" + boundary__ + "\r\n");
      writeHeader(prout, "Content-Type: " + contentType);// + "; name=\"" + name + "\"");
      writeHeader(prout, "Content-Transfer-Encoding: Base64");
      if (contentDisposition != null)
        writeHeader(prout, "Content-Disposition: " + contentDisposition + "; filename=\"" + name + "\"");
      
      // Put out any other headers that were specified
      if (otherHeaders != null)
      {
      	Map m = (Map)otherHeaders;
      	Iter i = m.createKeysIterator();
      	while (i.hasNext())
      	{
      		Any hdr = i.next();
      		Any val = m.get(hdr);
      		writeHeader(prout, hdr.toString() + ": " + val.toString());
      	}
      }
      
      // Headers done
      prout.print("\r\n");

      // Flush the writer as we will write directly to the
      // underlying output stream for the attachments.
      prout.flush();

      Any a;
      while ((a = bs.read()) != AnyNull.instance())
      {
        AnyByteArray byteArray = (AnyByteArray)a;
        byte[] b = byteArray.getValue();
        b = Base64.base64Encode(b);
        int len = b.length;
        if (len > 76)
        {
          int idx = 0;
          int wrt = 76;
          do
          {
            os.write(b, idx, wrt);
            os.write('\r');
            os.write('\n');
            idx += wrt;
            wrt = ((idx + wrt) > len) ? len - idx
                                      : 76;
          }
          while(idx < len);
        }
        else
        {
          os.write(b);
          os.write('\r');
          os.write('\n');
        }
      }
      os.flush();
    }
    catch (Exception e)
    {
      ok = false;
    }
    finally
    {
      bs.close();
    }
    return ok;
  }

  private Array validateAttachments(Vectored    attachments,
                                    PrintWriter errorBuffer,
                                    IntI        responseCode)
                                      throws AnyException
  {
    Array ret = null;

    // The attachment should be a URL we can open with a byte stream
    // that is then written to the mail message as base 64 encoded data.
    ByteStream   bs = new ByteStream(1);

    for (int i = 0; i < attachments.entries(); i++)
    {
      Any attachment = attachments.getByVector(i);
      Any item = attachment;
      if (attachment instanceof Map)
      	attachment = ((Map)attachment).get(ATTACHKEY);

      try
      {
        if (!bs.open(getTransaction().getProcess(),
                     attachment,
                     PhysicalIO.read__))
        {
          errorBuffer.println("Can't process " + attachment + ", mail not sent");
          responseCode.setValue(504);
          return null;
        }

        if (ret == null)
          ret = AbstractComposite.array();

        ret.add(item);
      }
      finally
      {
        bs.close();
      }
    }

    return ret;
  }

  private boolean responseOK(IntI responseCode, boolean debug)
  {
    int i = responseCode.getValue();
    if (debug)
      System.out.println("Response code " + i);

    return (i >= 200 && i < 400);
  }

  // Read response lines from the mail server returning the last
  // available or null if a timeout occurs.
  // Response lines in SMTP are characterised by a three digit
  // response code, either a space or a "-" sign and trailing
  // text.  The "-" sign indicates that there are more response
  // lines to follow and is absent on the ultimate line.
  // See http://www.faqs.org/rfcs/rfc2821.html
  // This method only throws if something goes wrong with the
  // underlying communication
	private String readIncoming(BufferedReader in, IntI responseCode, boolean debug) throws AnyException
  {
    String s = null;
    try
    {
      // Discard all but last line.
      boolean continuation = true;
      do
      {
        s = in.readLine();

        if (debug)
          System.out.println(s);

        if (s.length() > 3)
        {
          if (s.charAt(3) != '-')
          {
            responseCode.fromString(s.substring(0,3));
            continuation = false;
            s = s.substring(4);
          }
          else
          {
            processContinuation(s = s.substring(4));
          }
        }
        else
        {
          continuation = false;
          // Line may be malformed or non-compliant
          if (s.length() == 3)
            responseCode.fromString(s);
          s = null;
        }
      }
      while(continuation);
    }
    catch (SocketTimeoutException stox)
    {
      return null;
    }

    catch(Exception ex)
    {
      throw new ContainedException(ex);
    }

    return s;
  }
	
	private void processContinuation(String cmd)
	{
	  // Handle continuation response
	}

  private void close(PrintWriter prout, BufferedReader in)
  {
    // close the socket
    try
    {
    	prout.close();
      in.close();
    }
    catch (IOException ioX)
    {
    	throw new RuntimeContainedException(ioX);
    }
  }

  private boolean validateMap(Map m, Any a) throws AnyException
  {
    // 1) Map must contain at least one to: address.
    // 2) An SMTP host can be specified or it defaults
    //    to $catalog.mail.smtphost
    // 3) A from: address can be specified but defaults
    //    to $process.mail.from
    // The map is invalid if these items (or their defaults)
    // are unavailable.  The message does not require a body
    // or a subject.
    
    Any to = m.getIfContains(TOKEY);
    if (to == null)
      throw new AnyException("No To: address specified");
    if (!(to instanceof Vectored))
      throw new AnyException("To: must be a vector");

    if (!m.contains(MAILHOSTKEY))
    {
      LocateNode l = new LocateNode(GLOB_SMTP);
      Any smtphost = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       l);
      if (smtphost != null)
        m.add(MAILHOSTKEY, smtphost);
      else
        throw new AnyException("No specific or globally configured SMTP host");
    }

    if (!m.contains(FROMKEY))
    {
      LocateNode l = new LocateNode(PROC_FROM);
      Any from = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   l);
      if (from != null)
        m.add(FROMKEY, from.cloneAny());
      else
        throw new AnyException("No from address");
    }

    return AnyBoolean.TRUE.equals(m.getIfContains(DEBUGKEY));
  }
  
  static private class TeeOutputStream extends OutputStream
  {
  	private final OutputStream s1;
  	private final OutputStream s2;

  	private TeeOutputStream(OutputStream s1)
  	{
  		this(s1, null);
  	}
  	
  	private TeeOutputStream(OutputStream s1, OutputStream s2)
  	{
  		this.s1 = s1;
  		this.s2 = s2;
  	}
  	
		@Override
		public void write(int b) throws IOException
		{
      s1.write(b);
      if (s2 != null)
      	s2.write(b);
		}
  	
    public void flush() throws IOException
    {
      s1.flush();
      if (s2 != null)
      	s2.flush();
    }

    public void close() throws IOException
    {
      s1.close();
      if (s2 != null)
      	s2.close();
    }
  }
}
