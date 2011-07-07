/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Compile.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
 
/**
 * Compile (but do not execute) the Inq source operand.  The Inq
 * source is read from a URL, parsed and the resulting
 * object tree returned.
 * <p>
 * The statements do not have to have a single "root" node (i.e.
 * multiple statements do not have to be scripted inside a block
 * statement. Instead, this function wraps statements at the top
 * level in a {@link Sequence} which, in turn, in placed inside
 * a func object. The correct script for <em>executing</em>
 * the <code>compile</code> function and holding the result for later
 * use is therefore
 * <pre><code>
 * any f = compile(u);
 * </code></pre>
 * where <code>f</code> is a context-less <code>cfunc</code>
 * and <code>u</code> is a url where the source can be found.
 * <p>
 * Later, the compiled script can be run in the normal way with
 * <pre><code>
 * xfunc(f, args);
 * </code></pre>
 * where <code>args</code> are any stack elements expected by the
 * compiled script.
 * <p>  
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class Compile extends    AbstractFunc
                     implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any inq_;
  private Any baseURL_;
  private Any level_;
  private Any placeAt_;
	
	public Compile (Any inq)
	{
		this(inq, inq, null, null);
	}

  public Compile (Any inq, Any baseURL)
  {
    this(inq, baseURL, null, null);
  }

  public Compile (Any inq, Any baseURL, Any placeAt)
  {
    this(inq, baseURL, placeAt, null);
  }

	Compile (Any inq, Any baseURL, Any placeAt, Any level)
	{
		inq_     = inq;
		baseURL_ = baseURL;
		placeAt_ = placeAt;
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

    Any aBaseURL  = EvalExpr.evalFunc (getTransaction(),
                                       root,
                                       baseURL_);

    NodeSpecification placeAt  =
        (NodeSpecification)EvalExpr.evalFunc (getTransaction(),
                                              root,
                                              placeAt_);

    // We must have source, even if its a relative URL
    if (inqSource == null)
      throw new AnyException("Could not resolve source operand " + inq_);
    
    if (placeAt == null && placeAt_ != null)
      nullOperand(placeAt_);
    
    // If the source is relative, we must have a base URL as well
    AnyURL sourceURL = new AnyURL(inqSource.toString());
    if (sourceURL.getURL() == null && aBaseURL == null)
      throw new AnyException("No base URL for relative source " + inq_);
    
    // Determine the source URL. If the given source URL is absolute
    // then use it as is.  Otherwise base it on the given base.
    URL    u;
    if ((u = sourceURL.getURL()) == null)
    {
      AnyURL bu      = new AnyURL(aBaseURL.toString());
      u = sourceURL.getURL(bu);
      sourceURL = new AnyURL(u);
    }
      
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
    
	  try
	  {
      if (privLevel >= 0)
        getTransaction().getProcess().setEffectivePrivilegeLevel(privLevel);

      Any ret = null;
      
      InputStream is = u.openStream();

      // If we are a client there is a single instance of
      // the interpreter. The swing and process threads don't
      // run together so there's no problem with synchronisation
      // (but what if we create more processes in the client ??)
      // If we are a server then make a new interpreter.
      if (Globals.process__ != null)
      {
        ret = Globals.interpreter__.compile(root, sourceURL, getTransaction(), is);
      }
      else
      {
        Interpreter intr = new InqInterpreter();
        ret = intr.compile(root, sourceURL, getTransaction(), is);
      }
      
      // When compiling (that is not executing code not within a
      // function or service definition) statements are returned
      // in an array.  Must return a function so put result in a
      // sequence (protected in a FuncHolder, of course)
      AnyFuncHolder.FuncHolder fh = new AnyFuncHolder.FuncHolder(new Sequence(ret));
      fh.setUrl(sourceURL);
      
      // If there is a placeAt then put the unencumbered function
      // at the specified path. It can then be evaluated just by
      // referencing it.
      if (placeAt != null)
      {
        BuildNodeMap b = new BuildNodeMap();
        b.setTransaction(getTransaction());
        b.build(placeAt, fh.getFunc().cloneAny(), root);
      }
      
      return fh;
    }
    catch (IOException e)
    {
    	throw new ContainedException(e);
    }
    finally
    {
      if (privLevel >= 0)
        getTransaction().getProcess().setEffectivePrivilegeLevel(currPrivLevel);
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
      AnyByteArray anyByteArray = (AnyByteArray)inqSource;
      is = new ByteArrayInputStream(anyByteArray.getValue());
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
  	Compile x = (Compile)super.clone();
  	
  	x.inq_        = inq_.cloneAny();
  	x.baseURL_    = baseURL_.cloneAny();
  	x.level_      = AbstractAny.cloneOrNull(level_);
    x.placeAt_    = AbstractAny.cloneOrNull(placeAt_);

    return x;
  }

}
