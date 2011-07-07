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

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.inqwell.any.util.SendMailConstants;
import com.inqwell.any.util.Base64;
import com.inqwell.any.io.PrintStream;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.ByteStream;

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

  private boolean          debug_ = false;
  private Socket           socket_;
  private BufferedReader   in_;
  private PrintWriter      prout_;

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

    validateMap(mailInfo, a);

    // If we get here then we can do the mail message

    int port = 25;

    if (mailInfo.contains(MAILPORTKEY))
    {
      IntI p = new ConstInt(mailInfo.get(MAILPORTKEY));
      port = p.getValue();
    }

    String host = mailInfo.get(MAILHOSTKEY).toString();

    AnyInt responseCode = new AnyInt();
    StringWriter sw = new StringWriter();
    PrintWriter errorBuffer = new PrintWriter(sw);
    if (!doMail(host, port, mailInfo, errorBuffer, responseCode))
    {
      // Put the error buffer in the caller's map
      errorBuffer.flush();
      mailInfo.add(ERROR, new ConstString(sw.toString()));
      ret.setValue(false);
    }

		return ret;
	}

  public Object clone () throws CloneNotSupportedException
  {
		SendMail c  = (SendMail)super.clone();

		c.mailInfo_ = AbstractAny.cloneOrNull(mailInfo_);
    c.debug_    = false;
    c.socket_   = null;
    c.in_       = null;
    c.prout_    = null;

		return c;
  }

  private boolean doMail(String      host,
                         int         port,
                         Map         mailInfo,
                         PrintWriter errorBuffer,
                         IntI        responseCode) throws AnyException
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

    socket_ = getSocket(host, port);

    // Read the initial connect response line
    String str = readIncoming(responseCode);
    if (!responseOK(responseCode))
      return false;

    str = ehlo(responseCode);
    if (!responseOK(responseCode))
    {
      errorBuffer.println(str);
      return false;
    }

    str = mailFrom(mailInfo, responseCode);
    if (!responseOK(responseCode))
    {
      errorBuffer.println(str);
      return false;
    }

    rcptTo(errorBuffer, mailInfo, responseCode);
    if (!responseOK(responseCode))
      return false;

    data(errorBuffer, mailInfo, responseCode, attachments);
    if (!responseOK(responseCode))
      return false;

    quit(responseCode);
    if (!responseOK(responseCode))
    {
      errorBuffer.println(str);
      return false;
    }

    close();
    return true;
  }

  private String ehlo(IntI responseCode) throws AnyException
  {
    try
    {
      String localhost = InetAddress.getLocalHost().getHostName();

      writeCommand("EHLO " + localhost + "\r\n");
      return readIncoming(responseCode);
    }
    catch (UnknownHostException uhe)
    {
      throw new ContainedException(uhe);
    }
  }

  private String mailFrom(Map mailInfo, IntI responseCode) throws AnyException
  {
    writeCommand(MAILFROM + "<" + mailInfo.get(FROMKEY) + ">\r\n");
    return readIncoming(responseCode);
  }

  private void rcptTo(PrintWriter errorBuffer,
                      Map         mailInfo,
                      IntI        responseCode) throws AnyException
  {
    String   s      = null;
    boolean  rcptOK = false;

    Vectored to     = (Vectored)mailInfo.get(TOKEY);
    rcptOK = rcptToItems(to, errorBuffer, responseCode);

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
      rcptOther = rcptToItems(to, errorBuffer, responseCode);
      if (!rcptOK)
        rcptOK = rcptOther;
    }

    if (mailInfo.contains(BCCKEY))
    {
      to = (Vectored)mailInfo.get(BCCKEY);
      rcptOther = rcptToItems(to, errorBuffer, responseCode);
      if (!rcptOK)
        rcptOK = rcptOther;
    }

    // As long as we got an OK response for at least one of the
    // specified to: addresses make sure the response code is OK
    if (rcptOK)
      responseCode.setValue(250);
  }

  private boolean rcptToItems(Vectored    recipients,
                              PrintWriter errorBuffer,
                              IntI      responseCode) throws AnyException
  {
    String  s;
    boolean rcptOK = false;

    for(int i = 0; i < recipients.entries(); i++)
    {
      writeCommand(RCPTTO + "<" + recipients.getByVector(i) + ">\r\n");
      s = readIncoming(responseCode);
      if (!responseOK(responseCode))
        errorBuffer.println(s);
      else
        rcptOK = true;
    }

    return rcptOK;
  }

  private void data(PrintWriter errorBuffer,
                    Map         mailInfo,
                    IntI        responseCode,
                    Array       attachments) throws AnyException
  {
    writeCommand(DATA + "\r\n");
    String s = readIncoming(responseCode);
    if (!responseOK(responseCode))
    {
      errorBuffer.println(s);
      return;
    }

    writeHeader(FROM + "<" + mailInfo.get(FROMKEY) + ">");

    writeRecipientsHeader(TO, mailInfo.get(TOKEY));

    SimpleDateFormat sent = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
    writeHeader(DATE + sent.format(new Date()));

    if (mailInfo.contains(CCKEY))
      writeRecipientsHeader(CC, mailInfo.get(CCKEY));

    if (mailInfo.contains(SUBKEY))
    {
      Any a = mailInfo.get(SUBKEY);
      writeHeader(SUBJECT + a.toString());
    }

    writeHeader("X-Mailer: com.inqwell.any.SendMail version " + version__);

    boolean isMime = false;
    String contentType = "text/plain; charset=\"us-ascii\"";
    Any cType = mailInfo.getIfContains(CONTENTKEY);
    if (cType != null)
      contentType = cType.toString();

    if (attachments != null)
    {
      isMime = true;
      // If there are attachments then turn the body into a MIME message
      writeHeader("Mime-Version: 1.0");
			writeHeader("Content-Type: multipart/mixed; boundary=\"" +
                   boundary__ +
                   "\"");
      prout_.print("\r\n");
			prout_.print("This is a multipart message in MIME format.\r\n");
			prout_.print("If you see this message, your mail reading client does not support MIME properly\r\n");
			prout_.print("\r\n--" + boundary__);
      prout_.print("\r\n");
			//writeHeader("Content-Type: text/plain; charset=\"us-ascii\"");
			writeHeader("Content-Type: " + contentType);
			writeHeader("Content-Disposition: inline");
    }
    else
      writeHeader("Content-Type: " + contentType);

    if (mailInfo.contains(BODYKEY))
    {
      // blank line before body text
      prout_.print("\r\n");
      prout_.flush();

      // The SMTP specification sets maximum line length of 1000
      // characters
      Any body = mailInfo.get(BODYKEY);
      if (body instanceof PrintStream)
      {
        Any a;
        PrintStream ps = (PrintStream)body;
        while ((a = ps.read()) != AnyNull.instance())
          dataString(a.toString());
      }
      else if (body instanceof StringI)
      {
        dataString(body.toString());
      }
      else
      {
        // The given Any is unacceptable as the body text.
        // We do not want to send a mail in these circumstances
        // so having got this far reset the SMTP server and
        // set an error response.
        endOfMessage(errorBuffer, responseCode);
        reset(errorBuffer, responseCode);
        responseCode.setValue(504);
        return;
      }
    }

    if (isMime)
    {
      // Do the attachments
      if (!doAttachments(attachments, errorBuffer, responseCode))
      {
        // If any of the attachments fails then don't send the mail.
        // Since we have written some data, end the message and reset
        // the SMTP server so the message doesn't go
        endOfMessage(errorBuffer, responseCode);
        reset(errorBuffer, responseCode);
        responseCode.setValue(504);
        return;
      }
      // Ultimate boundary
      prout_.print("\r\n--" + boundary__ + "--\r\n");
      prout_.flush();
    }

    // Empty data is OK.  Terminate the data transfer
    endOfMessage(errorBuffer, responseCode);
  }

  private String quit(IntI responseCode) throws AnyException
  {
    writeCommand("QUIT\r\n");
    return readIncoming(responseCode);
  }

  private boolean endOfMessage(PrintWriter errorBuffer,
                               IntI        responseCode) throws AnyException
  {
    boolean ok = true;
    writeCommand(".\r\n");
    String s = readIncoming(responseCode);
    if (!responseOK(responseCode))
    {
      ok = false;
      errorBuffer.println(s);
    }
    return ok;
  }

  private boolean reset(PrintWriter errorBuffer,
                        IntI        responseCode) throws AnyException
  {
    boolean ok = true;
    writeCommand("RSET\r\n");
    String s = readIncoming(responseCode);
    if (!responseOK(responseCode))
    {
      ok = false;
      errorBuffer.println(s);
    }
    return ok;
  }

  private void dataString(String s)
  {
    int len = s.length();
    if (len > 1000)
    {
      int idx = 0;
      int wrt = 1000;
      do
      {
        String s1 = s.substring(idx, idx+wrt);
        writeDataString(s1);
        idx += wrt;
        wrt = ((idx + wrt) > len) ? len - idx
                                  : 1000;
      }
      while(idx < len);
    }
    else
    {
      writeDataString(s);
    }
    prout_.flush();
  }

  private void writeDataString(String s)
  {
    // Just check if a single period escape is necessary
    if(s.length() == 1 && s.charAt(0) == '.')
      prout_.print(".");

    prout_.print(s);
    prout_.print("\r\n");
  }

  private void writeCommand(String cmd)
  {
    prout_.print(cmd);
    prout_.flush();
    if (debug_)
      System.out.println(cmd);
  }

  private void writeHeader(String hdr)
  {
    if (hdr.length() > 1000)
      hdr = hdr.substring(0,1000);

    prout_.print(hdr);
    prout_.print("\r\n");
  }

  private void writeRecipientsHeader(String hdr, Any recipientsList)
  {
    Vectored recipients = (Vectored)recipientsList;

    prout_.print(hdr);
    for (int i = 0; i < recipients.entries(); i++)
    {
      if (i != 0)
      {
        prout_.print(",\r\n "); // fold the header
        //prout_.print(", "); // fold the header
      }

      prout_.print("<" + recipients.getByVector(i) + ">");
    }
    prout_.print("\r\n");
    prout_.flush();
  }

  private boolean doAttachments(Array       attachments,
                                PrintWriter errorBuffer,
                                IntI        responseCode) throws AnyException
  {
    for (int i = 0; i < attachments.entries(); i++)
    {
      if (!doAttachment(attachments.get(i),
                        errorBuffer,
                        responseCode))
        return false;
    }
    return true;
  }

  private boolean doAttachment(Any attachment,
                               PrintWriter errorBuffer,
                               IntI        responseCode) throws AnyException
  {
    boolean ok = true;

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
      OutputStream os = socket_.getOutputStream();
      // Work out the attachment name from the last path component
      String name = bs.getURL().getLastPath();
			prout_.print("\r\n--" + boundary__ + "\r\n");
      writeHeader("Content-Type: application/octet-stream; name=\"" + name + "\"");
      writeHeader("Content-Transfer-Encoding: Base64");
      writeHeader("Content-Disposition: inline; filename=\"" + name + "\"");
      prout_.print("\r\n");

      // Flush the writer as we will write directly to the
      // underlying output stream for the attachments.
      prout_.flush();

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

        ret.add(attachment);
      }
      finally
      {
        bs.close();
      }
    }

    return ret;
  }

  private boolean responseOK(IntI responseCode)
  {
    int i = responseCode.getValue();
    if (debug_)
      System.out.println("Response code " + i);

    return (i >= 200 && i < 400);
  }

  // Connect to the mail server, set the read timeout and
  // establish the streams
  private Socket getSocket(String host, int port) throws AnyException
  {
    socket_ = null;
    try
    {
      socket_ = new Socket(host, port);

      socket_.setSoTimeout(30000);  // timeout on responses from server is 30s
			in_ = new BufferedReader(new InputStreamReader(socket_.getInputStream()));

			prout_ = new PrintWriter(socket_.getOutputStream());
    }
    catch (Exception e)
    {
      throw new ContainedException(e);
    }

    return socket_;
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
	private String readIncoming(IntI responseCode) throws AnyException
  {
    String s = null;
    try
    {
      // Discard all but last line.
      boolean continuation = true;
      do
      {
        s = in_.readLine();

        if (debug_)
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
      close();
      throw new ContainedException(ex);
    }

    return s;
  }
	
	private void processContinuation(String cmd)
	{
	  // Handle continuation response
	}

  private void close()
  {
    // close the socket
    try
    {
      in_.close();
      prout_.close();
      socket_.close();
    }
    catch (IOException ioX)
    {
    }
  }

  private void validateMap(Map m, Any a) throws AnyException
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

    debug_ = m.contains(DEBUGKEY);
  }
}
