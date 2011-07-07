/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IsNull.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Checks if the first or only operand, which must be a Value or AnyObject,
 * is null.
 * When only a single operand is present, returns true if the value is
 * Java null or Inq null, false otherwise.
 * <p>
 * When the second operand is present, if the first operand evaluates
 * to null as above, the second operand is returned. Otherwise the
 * first operand is returned.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 * @see SetNull
 */
public class IsNull extends    AbstractFunc
                    implements Cloneable
{
	
	private static final long serialVersionUID = 1L;

	private Any any_;
  private Any val_;
	
	public IsNull(Any any, Any val)
	{
    any_ = any;
    val_ = val;
	}
	
	public Any exec(Any a) throws AnyException
	{
	  Transaction t = getTransaction();
	  
		Any av = EvalExpr.evalFunc(t,
                               a,
                               any_);
    
    Any        ret = null;
    AnyBoolean b   = new AnyBoolean();
    
    if (val_ == null)
    {
      // One argument version
      ret = b;
      
      if (!checkNull(av, b))
      {
        // If we couldn't resolve the null status then try a property binding
        Any pav = t.readProperty(av);
        if (pav != av)
          checkNull(pav, b);
      }
    }
    else
    {
      // Two argument version
      
      // If we will return a real value then
      // protect against field ripping
      if (!checkNull(av, b))
      {
        // If we couldn't resolve the null status then try a property binding
        Declare.ReadProperty r = new Declare.ReadProperty();
        Any pav = Declare.forcePropertyRead(av, r);
        if (pav != av)
          checkNull(pav, b);
      }
      else
      {
        av = AbstractAny.ripSafe(av, t);
      }
      
      ret = av;
      
      if (b.getValue())
      {
        // When null, return the alternative, which must resolve
        Any val = EvalExpr.evalFunc(t,
                                    a,
                                    val_);
        if (val == null)
          nullOperand(val_);
        
        val = AbstractAny.ripSafe(val, t);
        
        ret = val;
      }
    }
    
		return ret;
	}
	
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(any_);
  	if (val_ != null)
  	  a.add(val_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		IsNull i = (IsNull)super.clone();
		i.any_ = AbstractAny.cloneOrNull(any_);
		return i;
  }
  
  private boolean checkNull(Any av, AnyBoolean ret)
  {
    boolean knownType = true;
    
    if (av == null)
      ret.setValue(true);
    else if (av == AnyNull.instance())
      ret.setValue(true);
    else if (av instanceof Value)
    {
      Value v = (Value)av;
                                         
      ret.setValue(v.isNull());
    }
    else if (av instanceof ObjectI)
    {
      ObjectI o = (ObjectI)av;
      ret.setValue(o.getValue() == null || AnyNull.isNull(o.getValue()));
    }
    else
      knownType = false;
    
    return knownType;
  }
}
