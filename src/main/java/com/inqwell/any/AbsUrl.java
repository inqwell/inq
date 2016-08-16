/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Create an absolute URL from a relative one. The absolute
 * URL is created from a given relative URL and an optional
 * absolute one on which it is based. If not supplied, the
 * absolute URL of the currently executing script is used.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class AbsUrl extends    AbstractFunc
                    implements Cloneable
{
	private Any relative_;
	private Any absolute_;

	public AbsUrl(Any relative, Any absolute)
	{
    relative_ = relative;
    absolute_ = absolute;
	}

	public Any exec(Any a) throws AnyException
	{
		Any relative = EvalExpr.evalFunc(getTransaction(),
                                     a,
                                     relative_);

		Any absolute = EvalExpr.evalFunc(getTransaction(),
                                     a,
                                     absolute_);

    if (relative == null)
      nullOperand(relative_);

    if (absolute_ != null && absolute == null)
      nullOperand(absolute_);

    AnyURL base;

    if (absolute == null)
      base = new AnyURL(getTransaction().getExecURL());
    else
      base = new AnyURL(absolute);

    AnyURL rel = new AnyURL(relative);

    return new AnyString(rel.getURL(base).toString());
	}

  public Object clone () throws CloneNotSupportedException
  {
		AbsUrl a = (AbsUrl)super.clone();

    a.relative_   = relative_.cloneAny();
    a.absolute_   = AbstractAny.cloneOrNull(absolute_);

    return a;
  }
}
