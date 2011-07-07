/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DynamicMakeRenderInfo.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.StringI;

/**
 * A dynamic version of MakeRenderInfo. Supports the specification of
 * the typedef/field combo via expressions, instead of the literal syntax
 * Works by resolving the operands and converting them into the string
 * representations used by the base class. Slightly round-a-bout way of
 * doing it but the easiest to implement and of no real consequence.
 */
public class DynamicMakeRenderInfo extends MakeRenderInfo
{
  private Any dynType_;
  private Any dynField_;

  public DynamicMakeRenderInfo(Any    dynType,
                               Any    dynField,
                               Any    toRender,
                               Any    responsible,
                               String fQName,
                               String field,
                               Any    format,
                               Any    label,
                               Any    width,
                               Any    type,
                               Any    always,
                               Any    editable)
  {
    super(toRender,
          responsible,
          fQName,
          field,
          format,
          label,
          width,
          type,
          always,
          editable);
    
    dynType_  = dynType;
    dynField_ = dynField;
  }

  public Any exec(Any a) throws AnyException
  {
    Descriptor d = (Descriptor)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 dynType_,
                                                 Descriptor.class);
    
    if (d == null)
      nullOperand(dynType_);

    StringI    f = (StringI)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              dynField_,
                                              StringI.class);
    if (f == null)
      nullOperand(dynField_);

    setDescriptorFQName(d.getFQName().toString());
    setDescriptorField(f.toString());
    
    return super.exec(a);
  }

  public Object clone() throws CloneNotSupportedException
  {
    DynamicMakeRenderInfo m = (DynamicMakeRenderInfo)super.clone();
    
    m.dynType_  = dynType_.cloneAny();
    m.dynField_ = dynField_.cloneAny();
    
    return m;
  }  
}
