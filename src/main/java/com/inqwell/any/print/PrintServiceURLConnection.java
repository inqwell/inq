/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/PrintServiceURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.print;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Locale;
import javax.print.*;
import javax.print.attribute.*;

import com.inqwell.any.*;
import com.inqwell.any.net.NetUtil;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>printer://</code> style URLs.
 * <p>
 * URLs of the
 * style <code>printer://host:[port]/file?arg1=val&arg2=val</code> are
 * handled by this derivation of <code>java.net.URLConnection</code>.
 * If this connection is opened by calling
 * its <code>connect()</code> method then the resulting stream can
 * be written to. This will likely result in unformatted, raw
 * printing of plain text to the printer.
 * <p>
 * Only <code>getOutputStream()</code> returns a valid stream, to which
 * the application writes its print data.  When the stream is
 * closed by the application the print job completes.
 * <p>
 * At present only the URL argument <code>printerName</code> is
 * supported but future releases will map other print
 * job attributes. URL arguments are supported as follows:
 * <BL>
 * <LI><code>printerName </code> specify the named printer handle the
 * print job.  An exception is thrown if the printer name is not
 * found in the local environment or the printer does not support
 * the specified attributes.  If not specified, the default print
 * service is used.
 * </BL>
 */
public class PrintServiceURLConnection extends    URLConnection
                                       implements PrintConstants
{
  private OutputStream os_;
  
	public PrintServiceURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
		return null;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
		connect();
		return os_;
	}
	
	public void connect() throws IOException
	{
    if (!this.connected)
    {
      Map args = AbstractComposite.simpleMap();
      NetUtil.parseURLQuery(getURL().getQuery(), args);
      
      System.out.println("PrintServiceURLConnection args are: " + args);
      
      // Check to see if the URL arguments contain a particular
      // printer name.
      Any printerName = null;
      if (args.contains(PRINTER_NAME))
        printerName = args.get(PRINTER_NAME);
      
      // May be we check for other things like paper size etc.
      
      // Presently, we always present the data to the Java print
      // service as an InputStream, so we use a DocFlavor.INPUT_STREAM.
      // Within this we hard-code the AUTOSENSE mime type for
      // the present.  May be encode this as an argument also.
      final DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
      final PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
      
      PrintService ps = null;

      // If we are given a printer name then look up the services
      // that are available and that satisfy the request attributes
      // (currently none).
      if (printerName != null)
      {
        PrintService[]           service =
           PrintServiceLookup.lookupPrintServices(flavor, aset);
        for (int i = 0; i < service.length; i++)
        {
          if (printerName.toString().equals(service[i].getName()) ||
              printerName.toString().endsWith(service[i].getName()))
          {
            ps = service[i];
            break;
          }
        }
        if (ps == null)
          throw new AnyRuntimeException
            ("Printer " + printerName +
             " does not exist or does not support the specified attributes");
      }
      else
      {
        ps = PrintServiceLookup.lookupDefaultPrintService();
        if (ps == null)
          throw new AnyRuntimeException
            ("Unable to locate default print service");
      }
      
      // By now we have a print service.  Set up the remainder
      // of the print job.
      final DocPrintJob pj = ps.createPrintJob();
      PipedOutputStream pos = new PipedOutputStream();
      final PipedInputStream  pis = new PipedInputStream(pos);
      
      // We are using piped streams so do the print job in a
      // separate thread.
      Thread t = new Thread()
      {
        public void run()
        {
          // Document attributes are TBD
          Doc doc = new SimpleDoc(pis, flavor, null);
          try
          {
            pj.print(doc, aset);
          }
          catch (PrintException pex)
          {
            // not ideal but what to do - not easy to send to
            // process i/p channel.
            pex.printStackTrace();
          }
        }
      };
      t.start();
      os_ = pos;
      this.connected = true;
    }
	}
}
