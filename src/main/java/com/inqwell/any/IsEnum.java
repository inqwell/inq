/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Determine if a given value is a valid internal enumeration
 * for the given typedef and field.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class IsEnum extends    AbstractFunc
                  implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any field_;
  private Any value_;
  
  /**
   * IsEnum the given descriptor.
   */
  public IsEnum(Any descriptor, Any field, Any value)
  {
    descriptor_     = descriptor;
    field_          = field;
    value_          = value;
  }

  public Any exec(Any a) throws AnyException
  {
    //System.out.println ("IsEnum.exec() : descriptor : " + descriptor_);
    Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
                                      (getTransaction(),
                                       a,
                                       descriptor_,
                                       Descriptor.class);

    Any field  = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   field_);

    Any value  = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   value_);

    if (descriptor == null)
      throw new AnyException("Could not resolve tpedef " + descriptor_);
    
    Map enums = descriptor.getEnums();
    
    if (enums == null)
      throw new AnyException("Descriptor " + descriptor_ + " has no enums");
    
    Map m = (Map)enums.getIfContains(field);
    
    if (m == null)
      throw new AnyException("Field " + field + " of " + descriptor_ + " is not an enum or does not exist");
    
    return new AnyBoolean(m.contains(value));
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(descriptor_);
    a.add(field_);
    a.add(value_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    IsEnum e = (IsEnum)super.clone();
    
    e.descriptor_    = descriptor_.cloneAny();        
    e.field_         = field_.cloneAny();        
    e.value_         = value_.cloneAny();        
    
    return e;
  }
}
