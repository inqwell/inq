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
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Yield the field name for the given typedef and field symbols. If the
 * field does not exist an exception is thrown.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class FieldName extends    AbstractFunc
                  implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any field_;
  
  /**
   * Enum the given descriptor.
   */
  public FieldName(Any descriptor, Any field)
  {
    descriptor_     = descriptor;
    field_          = field;
  }

  public Any exec(Any a) throws AnyException
  {
    //System.out.println ("Enum.exec() : descriptor : " + descriptor_);
    Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
                                      (getTransaction(),
                                       a,
                                       descriptor_,
                                       Descriptor.class);

    Any field  = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   field_);

    if (descriptor == null)
      throw new AnyException("Could not resolve typedef " + descriptor_);
    
    if (field == null)
      nullOperand(field_);
    
    descriptor.getDataField(field, true);
    
    // If we get here then all's well. Just return the field value, as the
    // parser has made this a const string for us.
    return field;
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(descriptor_);
    a.add(field_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    FieldName f = (FieldName)super.clone();
    
    f.descriptor_    = descriptor_.cloneAny();        
    f.field_         = field_.cloneAny();        
    
    return f;
  }
}
