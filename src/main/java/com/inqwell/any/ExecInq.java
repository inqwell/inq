/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExecInq.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-09 20:16:37 $
 */
package com.inqwell.any;

import com.inqwell.any.*;
import com.inqwell.any.Process;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.client.swing.SwingInvoker;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Executes the Inq source operand.  The Inq source may be supplied as
 * a text stream contained in an <code>AnyByteArray</code> or as a
 * URL reference.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class ExecInq extends    AbstractFunc
                     implements Cloneable
{
  public static Object syncInq__ = new Object();

	private Any inq_;
	private Any baseURL_;
	private Any close_;
  private Any level_;

  private Any ret_;

	public ExecInq (Any inq)
	{
		this(inq, inq, null, null);
	}

	public ExecInq (Any inq, Any baseURL)
	{
		this(inq, baseURL, null, null);
	}

	ExecInq (Any inq, Any baseURL, Any level)
	{
		this(inq, baseURL, null, level);
	}

	ExecInq (Any inq, Any baseURL, Any close, Any level)
	{
		inq_     = inq;
		baseURL_ = baseURL;
		close_   = close;
    level_   = level;
	}

	/**
	 *
	 */
	public Any exec(Any root) throws AnyException
	{
    Any inqSource = EvalExpr.evalFunc(getTransaction(),
                                      root,
                                      inq_);

    Any aBaseURL    = EvalExpr.evalFunc (getTransaction(),
                                         root,
                                         baseURL_);

    if (inqSource == null)
      throw new AnyException("Could not resolve source operand " + inq_);

    URL    u       = null;
    AnyURL au      = null;
    AnyURL baseURL = null;
    
    if (aBaseURL == null)
      baseURL = AnyURL.getCwd();
    else
    {
      baseURL = new AnyURL(aBaseURL.toString());
      if (baseURL.getURL() == null)
        baseURL = AnyURL.getCwd();
    }
    
    if (!(inqSource instanceof AnyByteArray))
    {
      au = new AnyURL(inqSource.toString()); // the source operand
      u  = au.getURL(baseURL);
      au.setValue(u);
    }
    else
      au = baseURL;

    final URL    fu       = u;
    final AnyURL fbaseURL = au;

		BooleanI     close      = (BooleanI)EvalExpr.evalFunc (getTransaction(),
                                                           root,
                                                           close_,
                                                           BooleanI.class);
    Any level = EvalExpr.evalFunc (getTransaction(),
                                   root,
                                   level_);

    short privLevel = -1;
    short currPrivLevel = getTransaction().getProcess().getEffectivePrivilegeLevel();

    if (level != null)
    {
      ShortI s = new ConstShort(level);
      privLevel = s.getValue();
    }

    InputStream       ii    = null;
	  try
	  {
      if (privLevel >= 0)
        getTransaction().getProcess().setEffectivePrivilegeLevel(privLevel);

      Any ret = null;

      final InputStream i     = getInputStream(fu, inqSource);
      ii = i;
      final Any         lroot = root;

      // if we are a client then invoke on the swing thread,
      // otherwise go ahead here
      if (Globals.process__ != null)
      {
        SwingInvoker ss = new SwingInvoker()
        {
          protected void doSwing()
          {
            ret_ = Globals.interpreter__.run(lroot, fbaseURL, getTransaction(), i);
          }
        };

        ss.maybeSync();
        ret  = ret_;
        ret_ = null;
      }
      else
      {
        Interpreter intr = new InqInterpreter();
        ret = intr.run(root, fbaseURL, getTransaction(), i);
      }

	    if (close != null && close.getValue())
	    {
	    	//Process p = getTransaction().getProcess();
	    	//p.kill(p);
        Locate lo = new LocateNode(ServerConstants.ROCHANNEL);
        Locate li = new LocateNode(ServerConstants.RICHANNEL);
        lo.setTransaction(getTransaction());
        li.setTransaction(getTransaction());
        OutputChannel oc = (OutputChannel)lo.exec(root);
        OutputChannel ic = (OutputChannel)li.exec(root);
        ic.close();
        oc.close();
	    }

      return ret;
    }
    catch (IOException e)
    {
    	throw new ContainedException(e);
    }
    finally
    {
      if (privLevel >= 0)
        getTransaction().getProcess().setEffectivePrivilegeLevel(currPrivLevel);

      try
      {
        if (ii != null)
          ii.close();
      }
      catch(Exception e)
      {
        throw new ContainedException(e);
      }
    }
  }

  protected InputStream getInputStream(URL u, Any inqSource) throws IOException, AnyException
  {
    // It could be a byte array or it could be a URL to the
    // Inq resource.  Generally, Inq source is received from
    // an Inq server but we may reference Inq resources
    // on our classpath.
    InputStream is = null;

    if (u == null)
    {
      if (inqSource instanceof AnyByteArray)
      {
        AnyByteArray anyByteArray = (AnyByteArray)inqSource;
        is = new ByteArrayInputStream(anyByteArray.getValue());
      }
      else
      {
        AnyString anyString = (AnyString)inqSource;
        is = new ByteArrayInputStream(anyString.getValue().getBytes());
      }
    }
    else
    {
      // Assume its a URL.  Although usually resolved to
      // an absolute URL by now, local script exec can specify
      // a relative one so resolve w.r.t. given base anyway.
      is = u.openStream();
    }

    return is;
  }


  public Object clone() throws CloneNotSupportedException
  {
  	ExecInq x = (ExecInq)super.clone();

  	x.inq_        = inq_.cloneAny();
  	x.baseURL_    = baseURL_.cloneAny();
    x.level_      = AbstractAny.cloneOrNull(level_);
    x.close_      = AbstractAny.cloneOrNull(close_);

    return x;
  }

}
