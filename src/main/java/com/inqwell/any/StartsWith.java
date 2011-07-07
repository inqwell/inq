/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/StartsWith.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Returns boolean <code>true</code> if first operand starts with the
 * second operand, <code>false</code> otherwise.  Operands
 * must resolve to <code>StringI</code>s. An optional <code>offset</code>
 * operand specifies the point to start looking in the first operand.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class StartsWith extends    AbstractFunc
                        implements Cloneable
{
	
	private Any s1_;
	private Any s2_;
	private Any offset_;
	
	public StartsWith(Any s1, Any s2, Any offset)
	{
		s1_     = s1;
		s2_     = s2;
		offset_ = offset;
	}
	
	public Any exec(Any a) throws AnyException
	{
		StringI s1 = (StringI)EvalExpr.evalFunc(getTransaction(),
																a,
																s1_,
																StringI.class);

		StringI s2 = (StringI)EvalExpr.evalFunc(getTransaction(),
																a,
																s2_,
																StringI.class);

    // Any numeric type will do and we will croak if...
		Any offset = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   offset_);
    
    if (s1 == null)
      nullOperand(s1_);
   
    if (s2 == null)
      nullOperand(s2_);
      
    if (offset_ != null && offset == null)
      nullOperand(offset_);
    
    IntI iOffset = null;
    int    off     = 0;
    if (offset != null)
    {
      iOffset = new ConstInt(offset);  //... it can't be converted
      off = iOffset.getValue();
    }

		BooleanI ret = new AnyBoolean(s1.toString().startsWith(s2.toString(), off));
		
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		StartsWith e = (StartsWith)super.clone();
		e.s1_     = s1_.cloneAny();
		e.s2_     = s2_.cloneAny();
		e.offset_ = AbstractAny.cloneOrNull(offset_);
		return e;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    
  	if (s1_ != null)
      a.add(s1_);
  	if (s2_ != null)
      a.add(s2_);
  	if (offset_ != null)
      a.add(offset_);
      
  	return a.createIterator();
  }
}
