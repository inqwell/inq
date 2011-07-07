/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/CompileInq.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A pre-canned service on the INQ server.
 * <p>
 * This service resolves the source URL and sends
 * the text to the client for execution.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class CompileInq extends Service
{

  private static Any servicePath__ = new ConstString("system.services.CompileInq");
  private static Any sourceParam__ = new ConstString("source");
  private static Any switches__ = new ConstString("switches");

  public CompileInq() throws AnyException
  {
    init();
  }

//  public Object clone () throws CloneNotSupportedException
//  {
//    Transmit t = (Transmit)super.clone();
//
//		transmitTo_    = transmitTo_.cloneAny();
//		transmitWhat_  = transmitWhat_.cloneAny();
//		responseTo_    = responseTo_.cloneAny();
//
//    return t;
//  }

  private void init() throws AnyException
  {
	  // These are this service's input parameters
    addParam (sourceParam__.toString(), new AnyString());
    addParam (switches__.toString(), AbstractComposite.map());

		setExpr(new ReadSource());

		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://CompileInq"));
		setFQName(new ConstString("system:CompileInq"));
	}

  static public Any sendSource(Any         sourceURL,
                               Any         baseURL,
                               Any         syncGui,
                               Any         context,
                               Transaction t) throws AnyException
  {
    return sendSource(sourceURL, baseURL, syncGui, context, t, true);
  }

  static public Any sendSource(Any         sourceURL,
                               Any         baseURL,
                               Any         syncGui,
                               Any         context,
                               Transaction t,
                               boolean     propagateContext) throws AnyException
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[512];
    URL u = null;

    try
    {
      // If the URL we've been given is absolute then use
      // that and echo it back as the base url.  If its
      // relative then try to open w.r.t. the given base
      // and keep the given base url in this document.
      u = new URL(sourceURL.toString());
      baseURL = new AnyURL(u);
    }
    catch (MalformedURLException mue)
    {
      AnyURL au = new AnyURL(sourceURL);
      u = au.getURL(baseURL);
      baseURL = new AnyURL(u);
    }

    InputStream is = null;
    try
    {
      is = u.openStream();
      int bytesRead = 1;

      while (bytesRead > 0)
      {
        bytesRead = is.read(buf);

        if (bytesRead > 0)
          bos.write(buf, 0, bytesRead);
      }
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
    finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        }
        catch(Exception e)
        {
          throw new ContainedException(e);
        }
      }
    }

    // Set up the args that will be passed back to the client
    // when the response is sent there.
    Map args = AbstractComposite.simpleMap();

    buf = bos.toByteArray();
    args.add(RunInq.source__,
             new AnyByteArray(buf));
    args.add(AnyURL.baseURLKey__, baseURL);
    
    if (syncGui != null)
      args.add(NodeSpecification.atSyncGUI__, syncGui);

    SendRequest sr = new SendRequest(RunInq.servicePath__,
                          args,
                          new LocateNode(ServerConstants.ROCHANNEL));

    sr.setPropagateContext(propagateContext);
    sr.setTransaction(t);
    sr.exec(context);

    return null;
  }

  // Resolve the source operand and return a byte array
  // containing the source text.
	static private class ReadSource extends    AbstractFunc
	                                implements Cloneable
	{
		private Any source_   = new LocateNode("$stack.source");
		private Any switches_ = new LocateNode("$stack.switches");

		public Any exec(Any a) throws AnyException
		{
			Any source   = EvalExpr.evalFunc (getTransaction(),
																				a,
																				source_);

			Map switches = (Map)EvalExpr.evalFunc (getTransaction(),
																						 a,
																						 switches_,
																						 Map.class);

      // Read the source at the given URL into a byte array
      Any baseURL = switches.get(AnyURL.baseURLKey__);
      
      Any syncGui = switches.getIfContains(NodeSpecification.atSyncGUI__);

      return sendSource(source, baseURL, syncGui, a, getTransaction());

	  }

	  public Object clone() throws CloneNotSupportedException
	  {
	  	ReadSource r = (ReadSource)super.clone();

	  	r.source_   = source_.cloneAny();
	  	r.switches_ = switches_.cloneAny();

	    return r;
	  }
	}
}
