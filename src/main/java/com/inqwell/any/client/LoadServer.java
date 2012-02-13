/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/LoadServer.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.ServerConstants;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Invokes the <code>RunInq</code> service in the server.  Runs
 * the specified script in the server at a particular context,
 * if specified.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class LoadServer extends    AbstractFunc
												implements Cloneable
{
	private Any         source_;   // the source (URL string)
  private Any         context_;
  
	private SendRequest sr_;
	
	private AnyURL      baseURL_;
	
	public LoadServer (Any source)
	{
    this(source, null);
	}
	
	public LoadServer (Any source, Any context)
	{
		source_      = source;
		context_     = context;
		init();
	}
	
	/**
	 * @return original argument.
	 */
	public Any exec(Any a) throws AnyException
	{
		Any source   = EvalExpr.evalFunc (getTransaction(),
																			a,
																			source_);
    
    if (source == null)
      throw new AnyException("source operand is null");
		                                   
		// Put the resolved arguments of this function into the
		// service request's input arguments.
    Map svcArgs = AbstractComposite.simpleMap();
		svcArgs.add(LoadClient.source__,  resolveSource(source));
		
    svcArgs.add(AnyURL.baseURLKey__, baseURL_);
    
    svcArgs.add(RunInq.level__, new ConstShort(getTransaction().getProcess().getEffectivePrivilegeLevel()));
		
		sr_.setArgs(svcArgs);
		sr_.setContext(context_);
		
		// The contents of the SendRequest instance is already resolved
		// with the exception of the output channel.  This must therefore
		// be reachable from the exec argument a.
		sr_.setTransaction(getTransaction());
		sr_.exec(a);
		
	  return a;
	}
	
	public void setBaseURL(String url)
	{
		baseURL_ = new AnyURL(url);
	}

  public Object clone() throws CloneNotSupportedException
  {
  	LoadServer ls = (LoadServer)super.clone();
  	
  	ls.source_     = source_.cloneAny();
  	ls.context_    = AbstractAny.cloneOrNull(context_);
  	ls.sr_         = (SendRequest)sr_.cloneAny();
  	
    return ls;
  }
  
  private Any resolveSource(Any source) throws AnyException
  {
    // Read the source at the given URL into a byte array before sending to
    // the peer jvm for execution
    
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buf = new byte[512];
    URL u = null;
    try
    {
      // If the URL we've been given is absolute then use
      // that and echo it back as the base url.  If its
      // relative then try to open w.r.t. the given base
      // and keep the given base url in this document.
      u = new URL(source.toString());
    }
    catch (MalformedURLException mue)
    {
      AnyURL au = new AnyURL(source);
      u = au.getURL(baseURL_);
      baseURL_ = new AnyURL(u);
    }

    try
    {
      InputStream is = u.openStream();
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

    buf = bos.toByteArray();
    return new AnyByteArray(buf);
  }
  
	private void init()
	{		
										 
		sr_ = new SendRequest (new ConstString("system.services.RunInq"),
													 null,  // context set before exec() called
													 null,  // args set before exec() called
													 new LocateNode (ServerConstants.ROCHANNEL));
	}
}
