/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

import java.util.Enumeration;

/**
 * An <code>Iter</code> implementation to wrap a {@link java.util.Enumeration}.
 * Conversion to <code>Any</code> is presently quite crude - if the underlying
 * object returned by the iterator is not an Any then its <code>toString()</code>
 * method is called and a {@link ConstString} is returned.
 * @author tom
 */
public class EnumerationIter extends AbstractIter implements Iter
{
  private Enumeration enumeration_;
  
  public EnumerationIter(Enumeration e)
  {
    enumeration_ = e;
  }
  
  public Any getIterRoot()
  {
    throw new UnsupportedOperationException();
  }

  public boolean hasNext()
  {
    return enumeration_.hasMoreElements();
  }

  public Any next()
  {
    Object o = enumeration_.nextElement();
    
    if (o == null)
      return null;
    
    if (o instanceof Any)
      return (Any)o;
    
    return AbstractValue.flyweightString(o.toString());
  }

  public Any previous()
  {
    throw new UnsupportedOperationException();
  }

  public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
