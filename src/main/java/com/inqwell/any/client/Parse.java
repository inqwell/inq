/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Parse.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.Value;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.Array;
import com.inqwell.any.StringI;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ConstBoolean;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Iter;
import java.text.ParseException;


/**
 * Parse a string using a RenderInfo or explicit formatter.
 * to assign (and return) an <code>Any</code> result.
 * <p>
 * The string <code>toParse</code> is parsed according to the
 * format specified by <code>formatter</code> and the Value type
 * of <code>result</code> is assigned the value. This is also
 * the return value of the function.
 * <p>
 * The <code>formatter</code> argument can be either
 * <OL>
 * <LI>a <code>RenderInfo</code> instance</LI>
 * <LI>an <code>AnyFormat</code> instance or</LI>
 * <LI>a string containing the format pattern</LI>
 * </OL>
 * If the formatter is a string then a temporary AnyFormat instance
 * is created according to the pattern and the <code>toParse</code>
 * we are parsing into.
 * <p>
 * The <code>silent</code> argument defaults to <code>true</code>.
 * In this case parse errors are ignored and the result is set
 * to Inq <code>null</code>. Otherwise parse errors will raise an
 * exception.
 * <p>
 * If the <code>result</code> argument is not resolved then the
 * return value of the function will be <code>null</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Parse extends    AbstractFunc
									 implements Cloneable
{

  private Any        toParse_;
  private Any        formatter_;
  private Any        result_;
  private Any        silent_;

	public Parse(Any toParse, Any formatter, Any result)
	{
	  this(toParse, formatter, result, null);
	}

	public Parse(Any toParse, Any formatter, Any result, Any silent)
	{
    toParse_   = toParse;
    formatter_ = formatter;
    result_    = result;
    silent_    = silent;
	}

	public Any exec(Any a) throws AnyException
	{
		StringI str = (StringI)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 toParse_,
                                                 StringI.class);
    if (str == null)
      nullOperand(toParse_);

		Value res = (Value)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         result_);
    if (res == null)
      nullOperand(result_);

		Any fmt = EvalExpr.evalFunc(getTransaction(),
                                a,
                                formatter_);

    if (fmt == null)
      nullOperand(formatter_);

    AnyFormat formatter = null;

    if (fmt instanceof RenderInfo)
    {
      RenderInfo r = (RenderInfo)fmt;
      formatter = r.getFormat(res);
    }
    else if (fmt instanceof AnyFormat)
    {
      formatter = (AnyFormat)fmt;
    }
    else
    {
      if (!(fmt instanceof StringI))
        throw new AnyException("Formatter must be a renderinfo, a string or formatter passed to a verifier function");

      formatter = AnyFormat.makeFormat(res, fmt.toString());
      formatter.resolveDirectives(a, getTransaction());
    }

		Any silent = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   silent_);
    
    if (silent == null && silent_ != null)
      nullOperand(silent_);

    BooleanI b = null;
    if (silent != null)
      b = new ConstBoolean(silent);

    // silent mode is the default
    if (b != null && !b.getValue())
    {
      formatter.parseAny(str.toString(), res, false);
    }
    else
    {
      // silent mode.  If we fail just set result to null
      formatter.parseAny(str.toString(), res, true);
    }

		return res;
	}

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(toParse_);
  	a.add(formatter_);
  	a.add(result_);
  	if (silent_ != null)
    	a.add(silent_);

  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		Parse p = (Parse)super.clone();

		p.toParse_   = toParse_.cloneAny();
		p.formatter_ = formatter_.cloneAny();
		p.result_    = result_.cloneAny();
		p.silent_    = AbstractAny.cloneOrNull(silent_);

		return p;
  }

}
