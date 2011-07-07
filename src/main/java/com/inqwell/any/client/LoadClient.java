/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/LoadClient.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.ServerConstants;

/**
 * Invokes the <code>CompileInq</code> service in the server.  The
 * invocation includes a response specification to run the compiled
 * script in this JVM.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class LoadClient extends    AbstractFunc
												implements Cloneable
{
	private Any         source_;   // the source (URL string) we want
																 // the server send back
  
  private Any         sync_;
  
	private AnyURL      baseURL_;
	
	static Any source__   = new ConstString("source");
	static Any switches__ = new ConstString("switches");

  static private Any compile__ = AbstractValue.flyweightString("system.services.CompileInq");
  
	public LoadClient (Any source)
	{
		source_    = source;
	}
	
	public LoadClient (Any source, Any synchronous)
	{
		source_      = source;
    sync_        = synchronous;
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
      nullOperand(source_);
		                                   
    Any sync = (BooleanI)EvalExpr.evalFunc (getTransaction(),
                                            a,
                                            sync_,
                                            BooleanI.class);

    if (source == null && sync_ != null)
      nullOperand(sync_);
                                       
		// Put the resolved arguments of this function into the
		// service request's input arguments.
    Map svcArgs = AbstractComposite.simpleMap();
		svcArgs.add(source__,  source);
		
		Map switches = AbstractComposite.map();
    switches.add(AnyURL.baseURLKey__, baseURL_);
    if (sync != null)
      switches.add(NodeSpecification.atSyncGUI__, sync);
    
    
		svcArgs.add(switches__, switches);
		
    SendRequest sr = new SendRequest (compile__,
                                      null,
                                      null,
                                      null,
                                      new LocateNode (ServerConstants.ROCHANNEL),
                                      null);

    sr.setArgs(svcArgs);
		
		// The contents of the SendRequest instance is already resolved
		// with the exception of the output channel.  This must therefore
		// be reachable from the exec argument a.
		sr.setTransaction(getTransaction());
		sr.exec(a);
		
	  return a;
	}
	
	public void setBaseURL(String url)
	{
		baseURL_ = new AnyURL(url);
	}

  public Object clone() throws CloneNotSupportedException
  {
  	LoadClient lc = (LoadClient)super.clone();
  	
  	lc.source_     = source_.cloneAny();
  	lc.sync_       = AbstractAny.cloneOrNull(sync_);
  	
    return lc;
  }
}
