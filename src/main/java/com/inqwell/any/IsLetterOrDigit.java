/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IsLetterOrDigit.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Test whether the given character is a letter.  Typically used to
 * test the return value of <code>charat(str, index)</code>.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IsLetterOrDigit extends    AbstractFunc
                             implements Cloneable
{
	private Any chr_;
	
	public IsLetterOrDigit(Any chr)
	{
		chr_   = chr;
	}
	
	public Any exec(Any a) throws AnyException
	{
		CharI chr = (CharI)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         chr_,
                                         CharI.class);

    if (chr == null)
      nullOperand(chr_);

		AnyBoolean ret =
      new AnyBoolean(Character.isLetterOrDigit(chr.getValue()));
    
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		IsLetterOrDigit i = (IsLetterOrDigit)super.clone();
		i.chr_ = chr_.cloneAny();
		return i;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    a.add(chr_);
  	return a.createIterator();
  }
}
