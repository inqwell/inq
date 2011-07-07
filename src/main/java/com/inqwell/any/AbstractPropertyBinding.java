/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

public abstract class AbstractPropertyBinding extends    AbstractAny
                                              implements PropertyBinding
{
  public Any copyFrom(Any a)
  {
    setProperty(a);
    return this;
  }

  public void accept(Visitor v)
  {
    // If we are being written to then visit on self as unknown. We end up
    // in this.copyFrom(Any a)
    if (v instanceof Assign)
    {
      v.visitUnknown(this);
    }
    else
    {
      // For any other operation get our property value and then re-visit on that
      Any a = getProperty();
      if (a != null)
        a.accept(v);
    }
  }

  public String toString()
  {
    return getProperty().toString();
    // return i_.toString() + " on " + o_.toString();
  }
}
