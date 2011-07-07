/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MinVal.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Return the maximum value for the given scalar or integer null
 * if not a supported type.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MinVal extends    AbstractFunc
                    implements Cloneable
{
  private Any     any_;
  private VMinVal mv_;
	
	public MinVal(Any any)
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
      VMinVal v = new VMinVal();
      any.accept(v);
      ret = v.getMinVal();
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
		MinVal m = (MinVal)super.clone();
		m.any_ = AbstractAny.cloneOrNull(any_);
		return m;
  }
	
  static private class VMinVal extends AbstractVisitor
  {
    private Any minVal_;
    
    Any getMinVal()
    {
      Any ret = minVal_;
      minVal_ = null;
      return ret;
    }
    
    public void visitAnyByte (ByteI b)
    {
      minVal_ = ConstByte.minVal__;
    }
    
    public void visitAnyChar (CharI c)
    {
      minVal_ = ConstChar.minVal__;
    }
    
    public void visitAnyInt (IntI i)
    {
      minVal_ = ConstInt.minVal__;
    }
    
    public void visitAnyShort (ShortI s)
    {
      minVal_ = AnyShort.minVal__;
    }
    
    public void visitAnyLong (LongI l)
    {
      minVal_ = ConstLong.minVal__;
    }
  
    protected void unsupportedOperation(Any o)
    {
      AnyInt i = new AnyInt();
      i.setNull();
      minVal_ = i;
    }
  }
}
