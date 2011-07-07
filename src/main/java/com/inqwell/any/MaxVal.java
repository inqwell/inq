/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MaxVal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the maximum value for the given scalar or integer null
 * if not a supported type.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MaxVal extends    AbstractFunc
                    implements Cloneable
{
  private Any     any_;
  private VMaxVal mv_;
	
	public MaxVal(Any any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Any any = EvalExpr.evalFunc(getTransaction(),
																a,
																any_);

    Any ret;
    
    if (any != null)
    {
      VMaxVal v = new VMaxVal();
      any.accept(v);
      ret = v.getMaxVal();
    }
    else
    {
      AnyInt i = new AnyInt();
      i.setNull();
      ret = i;
    }
    
    return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		MaxVal m = (MaxVal)super.clone();
		m.any_ = AbstractAny.cloneOrNull(any_);
		return m;
  }
	
  static private class VMaxVal extends AbstractVisitor
  {
    private Any maxVal_;
    
    Any getMaxVal()
    {
      Any ret = maxVal_;
      maxVal_ = null;
      return ret;
    }
    
    public void visitAnyByte (ByteI b)
    {
      maxVal_ = ConstByte.maxVal__;
    }
    
    public void visitAnyChar (CharI c)
    {
      maxVal_ = ConstChar.maxVal__;
    }
    
    public void visitAnyInt (IntI i)
    {
      maxVal_ = ConstInt.maxVal__;
    }
    
    public void visitAnyShort (ShortI s)
    {
      maxVal_ = ConstShort.maxVal__;
    }
    
    public void visitAnyLong (LongI l)
    {
      maxVal_ = ConstLong.maxVal__;
    }
  
    protected void unsupportedOperation(Any o)
    {
      AnyInt i = new AnyInt();
      i.setNull();
      maxVal_ = i;
    }
  }
}
