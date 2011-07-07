/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version  $
 * @see 
 */

package com.inqwell.any;

/**
 * A concurrent-safe extension of ForEach
 * @author Tom
 *
 */
public class ForEachCS extends ForEach
{

  public ForEachCS(Any root, Any expression)
  {
    super(root, expression);
  }
  
  protected Iter makeIter(Map m, Any root)
  {
    if (m != null)
      return m.keys().createIterator();
    else
    {
      if (root instanceof Composite)
      {
        Composite c = (Composite)root;
        return c.shallowCopyOf().createIterator();
      }
      else
        return root.createIterator();
    }
  }
}
