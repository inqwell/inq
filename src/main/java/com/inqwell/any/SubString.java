/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SubString.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Inq substring functions. 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class SubString extends    AbstractFunc
                       implements Cloneable
{
  // BASIC (!) style substring functions.  All implemented here
  // and controlled by mode_ set from parser.
	public static short   LEFT   = 0;
	public static short   MID    = 1;
	public static short   RIGHT  = 2;

	private Any str_;
	private Any beginIndex_;
	private Any endIndex_;
  
  private short mode_;
	
	public SubString(Any str, Any beginIndex, Any endIndex, short mode)
	{
		str_        = str;
		beginIndex_ = beginIndex;
		endIndex_   = endIndex;
    mode_       = mode;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any str = (StringI)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         str_);

    // Any numeric type will do and we will croak if...
		Any beginIndex = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       beginIndex_);

		Any endIndex = EvalExpr.evalFunc(getTransaction(),
                                     a,
                                     endIndex_);

    if (str == null)
      nullOperand(str_);
   
    if (beginIndex == null)
      nullOperand(beginIndex_);
   
    if (endIndex_ != null && endIndex == null)
      nullOperand(endIndex_);
      
    //... it can't be converted here ...
    IntI iBegin = new ConstInt(beginIndex);
    IntI iEnd   = null;

    if (endIndex != null)
    {
      //... or here
      iEnd = new ConstInt(endIndex);
    }

    StringI ret;
    
    if (mode_ == RIGHT)
    {
      int l = str.toString().length();
      int i = l - iBegin.getValue();
      ret = new AnyString(str.toString().substring(i, l));
    }
    else if (mode_ == MID)
    {
      ret = new AnyString(str.toString().substring(iBegin.getValue(),
                                                   iEnd.getValue()+1));
    }
    else
    {
      // LEFT.  There's only one index, which is iBegin, but it
      // means the end.
      ret = new AnyString(str.toString().substring(0, iBegin.getValue()));
    }
		
		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		SubString s = (SubString)super.clone();
    
		s.str_        = str_.cloneAny();
		s.beginIndex_ = beginIndex_.cloneAny();
		s.endIndex_   = AbstractAny.cloneOrNull(endIndex_);
    
		return s;
  }
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
    
    a.add(str_);
    a.add(beginIndex_);
  	if (endIndex_ != null)
      a.add(endIndex_);
      
  	return a.createIterator();
  }
}
