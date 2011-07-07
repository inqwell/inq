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
 * Return the keys of the given map
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class KeysOf extends    AbstractFunc
                     implements Cloneable
{

  private Any map_;

  public KeysOf(Any map)
  {
    map_ = map;
  }

  public Any exec(Any a) throws AnyException
  {
    Map map = (Map)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           map_,
                                           Map.class);

    if (map == null)
      nullOperand(map_);
    
    return map.keys();
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(map_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    KeysOf c = (KeysOf)super.clone();
    c.map_ = map_.cloneAny();
    return c;
  }
}
