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

import com.inqwell.any.beans.UIFacade;
import com.inqwell.any.client.AnyAttributeSet;

/**
 * Perform assignment to maps such that only overlapping fields are copied.
 * <p/>
 * This class corresponds to the Inq operator := which assigns only the
 * fields present in the first operand from the second. It therefore gives
 * the author of the script explicit control over this function, rather
 * than relying on the semantics of first operand's {@link Any#copyFrom(Any)}
 * method, as is the case with =.
 * <p/>
 * Having said that, this class makes certain exceptions to preserve
 * the integrity of the first operand, for example by skipping special
 * children such as {@link KeyDef#key__} that characterise the map.
 * Overall, while deterministic from the point of view of the script,
 * this class is taking on the combined roles of the
 * various copyFrom(Any a) methods which makes it more complex.
 * <p/>
 * Although intent on only copying field values this class also handles
 * the special constants <code>equals</code> and <code>null</code>, during
 * which it may replace fields having or with those values. Under
 * these circumstances the value copied from is cloned to make a new
 * instance in the target map.
 * 
 * @author tom
 */
public class AssignByFields extends Assign
{
  protected void copyToMap(Map to, Any a)
  {
    if (a != this)
    {
      validate(to);
      
      Map from = (Map)a;
      
      Iter i = from.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        if (k.equals(KeyDef.key__) || k.equals(Descriptor.descriptor__))
          continue;
        
        Any v = to.getIfContains(k);
        
        doField(to, k, from.get(k), v);
      }
    }
  }
  
  // Check if the map is one of the types that don't support copying
  private void validate(Map m)
  {
    if (m instanceof WeakMap ||
        m instanceof Process ||
        m instanceof UIFacade ||
        m instanceof AnyAttributeSet)
      throw new UnsupportedOperationException("assign by fields " + m.getClass());
  }
  
  // Allow for special values.
  // 1) if the target value is the equals constant and we are assigning
  //    something else then replace in the map.
  private void doField(Map m, Any fieldName, Any from, Any to)
  {
    if (AnyAlwaysEquals.isAlwaysEquals(to))
    {
      // Copying to the equals constant. Replace if from is not
      // the equals constant
      if (!AnyAlwaysEquals.isAlwaysEquals(from))
        m.replaceItem(fieldName, from.cloneAny());
    }
    else if (AnyAlwaysEquals.isAlwaysEquals(from))
    {
      // Copying from the null constant. Replace if to is not
      // the null constant
      if (!AnyAlwaysEquals.isAlwaysEquals(to))
        m.replaceItem(fieldName, AnyAlwaysEquals.instance());
    }
    else if (AnyNull.isNull(to))
    {
      // Copying to the null constant. Replace if from is not
      // the null constant
      if (!AnyNull.isNull(from))
        m.replaceItem(fieldName, from.cloneAny());
    }
    else if (AnyNull.isNull(from))
    {
      // Copying from the null constant. Replace if to is not
      // the null constant
      if (!AnyNull.isNull(to))
        m.replaceItem(fieldName, AnyNull.instance());
    }
    else
    {
      // Normal copy. Will throw if to is const
      to.copyFrom(from);
    }
  }
}
