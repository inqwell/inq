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
 * A 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class VectoredSet extends    SimpleSet
                         implements Set,
                                    Vectored,
                                    Cloneable
{
  private static final long serialVersionUID = 1L;

  // Unlike some other collection implementations that only
  // set up the ordering on demand, VectoredSet always
  // supports ordered access from the outset.  Likely use of
  // this class is low, so this is OK.
  private Array order_ = AbstractComposite.array();
  
  private AnyInt   index_ = new AnyInt(-1);
  
	protected void beforeAdd(Any item)
  {
    super.beforeAdd(item);
    
    // Only add to the array once
    if (!this.contains(item))
    {
      int i = index_.getValue();
      
      if (i >= 0)
        order_.add(i, item);
      else
        order_.add(item);
    }
    
    index_.setValue(-1);
  }
  

	protected void afterRemove(Any item)
  {
    // If we are removing by vector we already know the index
    int i = index_.getValue();

    if (i < 0)
      i = order_.indexOf(item);
    
    if (i >= 0)
      order_.remove(i);
    
    index_.setValue(-1);
  }
  
	protected void emptying()
  {
    index_.setValue(-1);
    order_.empty();
  }
  
  public void removeByVector (int at)
  {
    Any item = order_.get(at);
    index_.setValue(at);
    this.remove(item);
  }
  
  public void removeByVector (Any at)
  {
    index_.copyFrom(at);
    this.removeByVector(index_.getValue());
  }
  
  public int indexOf(Any a)
  {
    return order_.indexOf(a);
  }

  public void reverse()
  {
    order_.reverse();
  }
  
  public boolean containsValue (Any value)
  {
    return this.contains(value);
  }

  public Any getByVector (int at)
  {
    return order_.get(at);
  }
  
  public Any getByVector (Any at)
  {
    index_.copyFrom(at);
    return this.getByVector(index_.getValue());
  }
  
  public Any getKeyOfVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  public Any getKeyOfVector(Any at)
  {
    throw new UnsupportedOperationException();
  }
  
  public void addByVector(Any value)
  {
    this.add(value);
  }
  
  public Array initOrderBacking()
  {
    return order_;
  }

  public void addByVector(int at, Any value)
  {
    if (at < 0 || at > order_.entries())
    {
      throw new ArrayIndexOutOfBoundsException("entries is " +
                                     order_.entries() +
                                     " index is " + at);
    }
    
    index_.setValue(at);
    this.add(value);
  }
  
  public void addByVector(int at, Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }
  
  /**
	 * Unsupported operation.
	 */
  public void setSparse(boolean isSparse)
  {
    throw new UnsupportedOperationException();
  }

  public Object[] toArray()
  {
    return order_.toArray();
  }
  
}
