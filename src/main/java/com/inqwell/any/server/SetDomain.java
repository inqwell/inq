/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/SetDomain.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.*;

/**
 * Set the domain of the current server environment.
 * This function is usually only used when an
 * <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup>
 * environment is initialising.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetDomain extends    AbstractFunc
											 implements Cloneable
{
	private Any domain_;
	
	public SetDomain(Any domain)
	{
		domain_ = domain;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any domain    = EvalExpr.evalFunc(getTransaction(),
																			a,
																			domain_);

		Server.instance().setDomain(domain);
		
		return a;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    SetDomain s = (SetDomain)super.clone();
    
    s.domain_   = domain_.cloneAny();
    
    return s;
  }
}
