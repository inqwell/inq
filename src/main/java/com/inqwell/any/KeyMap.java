/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ClientObject.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

public class KeyMap extends    AnyMap
                    implements Map,
                               Cloneable
{
  /**
   * Override.  Default implementation recreates the contents of this
   * because it does not assume that this is similar to the argument.
   * In this case we assume that we are taking on transaction private
   * values whereby we contain the same type of children as the argument
   */
  public Any copyFrom (Any a)
  {
    if (a == this)
      return this;
      
    if (!(a instanceof Map))
      throw new IllegalArgumentException (a.getClass().toString() + " is not a map");

    Map from = (Map)a;
    
    Iter i = from.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      if (k.equals(KeyDef.key__) || k.equals(Descriptor.descriptor__))
        continue;
      if (this.contains(k))
        this.get(k).copyFrom(from.get(k));
    }
    return this;
  }
}
