/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NewInstance.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Make a new, non-transactional instance from the given descriptor.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class NewInstance extends    AbstractFunc
												 implements Cloneable
{
	private Any descriptor_;
	private Any copyFrom_;
  
  private Any field_;  // supplied by parser and read only. 
	
	public NewInstance(Any descriptor)
	{
		descriptor_ = descriptor;
	}
	
	public NewInstance(Any descriptor, Any copyFrom)
	{
		descriptor_ = descriptor;
    copyFrom_   = copyFrom;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 descriptor_,
																					 Descriptor.class);
    if (descriptor == null)
      throw new AnyException("typedef not found: " + descriptor_);

		Any        copyFrom   = EvalExpr.evalFunc
																					(getTransaction(),
																					 a,
																					 copyFrom_);
    
    if (copyFrom != null && copyFrom == null)
      nullOperand(copyFrom_);
    
		Any any = null;
    
    if (field_ == null)
      any = descriptor.newInstance();
    else
    {
      // Check if the 'field' is, in fact, a key name
      Map keys = descriptor.getAllKeys();
      if (keys != null && (field_.equals(KeyDef.primaryKey__) || keys.contains(field_)))
      {
        // Its a named key
        KeyDef k = (KeyDef)keys.get(field_.equals(KeyDef.primaryKey__) ? KeyDef.defaultKey__
                                                                       : field_);
        if (copyFrom == null)
          copyFrom = k.getKeyProto();

        if (!(copyFrom instanceof Map))
          throw new IllegalArgumentException("Not a Map");
        Map m = k.makeKeyVal((Map)copyFrom);
        //m.remove(Descriptor.descriptor__);
        return m;
      }
      else if (field_.equals(ListenTo.listen__))
      {
        // The listen data. If there's an initial value
        // copy using AssignByFields
        Map m = (Map)descriptor.getListenerData().cloneAny();
        if (copyFrom != null)
        {
          if (!(copyFrom instanceof Map))
            throw new IllegalArgumentException("Not a Map");
          
          // For the listen data map use AssignByFields
          Func f = new EvalExpr(m, copyFrom, new AssignByFields());
          f.setTransaction(getTransaction());
          f.exec(a);
        }
        return m;
      }
      else
      {
        Map proto = descriptor.getProto();
        
        if (proto.contains(field_))
          any = proto.get(field_).cloneAny();
        else
          throw new AnyException("Not a field or a key name: " + field_);
      }
    }
		
		if (copyFrom != null)
			any.copyFrom(copyFrom);

		return any;
	}
	
  public void setField(Any field)
  {
    field_ = field;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    NewInstance n = (NewInstance)super.clone();
    
    n.descriptor_   = descriptor_.cloneAny();
    n.copyFrom_     = AbstractAny.cloneOrNull(copyFrom_);
    
    return n;
  }
	
}
