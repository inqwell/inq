/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyByteArray.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 19:57:08 $
 */


package com.inqwell.any;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an Any abstraction for the JDK byte array.  Seen
 * as an Array by Inq.
 */
public class AnyByteArray extends    AbstractAny
													implements Array,
                                     Vectored,
                                     Cloneable
{
  private byte[]    value_;
  
  private ShortI b_;
  private IntI   i_;

  public AnyByteArray() { }
  public AnyByteArray(byte[] b) { value_ = b; }


  static final String HEX_DIGITS = "0123456789abcdef";
  
  public String toString()
  {
    StringBuilder b = new StringBuilder();
    b.append('[');
    
    if (value_ != null && value_.length > 0)
    {
      for (int i = 0; i < value_.length; i++)
      {
        if (i != 0)
          b.append(", ");
        byte bb = value_[i];
        b.append("0x");
        b.append(HEX_DIGITS.charAt((bb & 0xF0) >> 4))
        .append(HEX_DIGITS.charAt((bb & 0x0F)));
      }
    }
    
		// Uses default platform encoding
    //return (value_ != null) ? new String(value_) : AnyString.EMPTY.toString();

    b.append(']');
    
    return b.toString();
  }

  public void accept (Visitor v)
  {
    v.visitArray(this);
  }

  public boolean equals(Any a)
  {
    return this == a;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (getClass() == a.getClass())
      {
        AnyByteArray ab = (AnyByteArray)a;
        byte[] orig = ab.getValue();
        if (orig == null)
          setValue(null);
        else
          setValue (Arrays.copyOf(orig, orig.length));
      }
      else
      {
    		// Uses default platform encoding
        setValue(a.toString().getBytes());
      }
		}
    return this;
  }

  public void fromString(Any a, String charsetName)
  {
    try
    {
      value_ = a.toString().getBytes(charsetName);
    }
    catch(UnsupportedEncodingException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Iter createIterator () {return DegenerateIter.i__;}

  public byte[]  getValue() { return value_; }
  public void setValue(byte[] value) { value_ = value; }

  private void initByte()
  {
    if (b_ == null)
      b_ = new AnyShort();
  }
  
  private void initInt()
  {
    if (i_ == null)
      i_ = new AnyInt();
  }
  
  @Override
  public void add(Any element)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int at, Any element)
  {
    checkInit();
    initByte();
    b_.copyFrom(element);
    value_[at] = (byte)b_.getValue();
  }

  @Override
  public void add(Any at, Any element)
  {
    initInt();
    i_.copyFrom(at);
    add(i_.getValue(), element);
  }

  @Override
  public void addAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Array bestowIdentity()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iter createReverseIterator()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any get(int at)
  {
    checkInit();
    return new ArrayElement(value_[at], at);
  }

  @Override
  public Any get(Any at)
  {
    initInt();
    i_.copyFrom(at);
    return get(i_.getValue());
  }

  @Override
  public List getList()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(Any a)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any remove(int at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any remove(Any at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceItem(int at, Any item)
  {
    add(at, item);
  }

  @Override
  public void replaceItem(Any at, Any item)
  {
    add(at, item);
  }

  @Override
  public void replaceValue(int at, Any value)
  {
    add(at, value);
  }

  @Override
  public void replaceValue(Any at, Any value)
  {
    add(at, value);
  }

  @Override
  public void reverse()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Array shallowCopy()
  {
    return new AnyByteArray(value_);
  }
  
  @Override
  public Object[] toArray()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Any a)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAny(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void empty()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int entries()
  {
    checkInit();
    return value_.length;
  }

  @Override
  public Any getNameInParent()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any getNodeSet()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Composite getParentAny()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any getPath(Any to)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Process getProcess()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasIdentity()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int identity()
  {
    checkInit();
    return value_.hashCode();
  }

  @Override
  public boolean isDeleteMarked(Any id)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isEmpty()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isParentable()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void markForDelete(Any id)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeInParent()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void retainAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNodeSet(Any nodeSet)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setParent(Composite parent)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Composite shallowCopyOf()
  {
    return shallowCopy();
  }

  @Override
  public void addByVector(Any value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addByVector(int at, Any value)
  {
    add(at, value);
  }

  @Override
  public void addByVector(int at, Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsValue(Any value)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any getByVector(int at)
  {
    return get(at);
  }
  
  @Override
  public Any getByVector(Any at)
  {
    return get(at);
  }

  @Override
  public Any getKeyOfVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Any getKeyOfVector(Any at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public Array initOrderBacking()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeByVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void removeByVector(Any at)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSparse(boolean isSparse)
  {
    throw new UnsupportedOperationException();
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    if (value_ != null)
      return new AnyByteArray(Arrays.copyOf(value_, value_.length));
    else
      return new AnyByteArray();
  }
  
  private void checkInit()
  {
    if (value_ == null)
      throw new IllegalStateException("array not initialised");
  }
  
  private void resize(int to)
  {
    if (value_ == null)
      value_ = new byte[to];
    else
      value_ = Arrays.copyOf(value_, to);
  }
  
  // When fetching values from the array bind them to it so things
  // like myArray[10] = 32 work as expected. The downside of this is
  // that any foo = myArray[10] binds foo to the array too, well
  // why would you do that anyway?
  private class ArrayElement extends AnyShort
  {
    private ArrayElement(byte value, int idx)
    {
      super(value);
      idx_ = idx;
    }
    
    int idx_;
    
    public Any copyFrom(Any a)
    {
      Any ret = super.copyFrom(a);
   
      value_[idx_] = (byte)getValue();
      
      return ret;
    }
  }
}

