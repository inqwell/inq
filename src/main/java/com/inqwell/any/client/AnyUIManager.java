/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyUIManager.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

import javax.swing.UIManager;

/**
 * Access to the UIManager as an Any supporting property access.
 */
public class AnyUIManager extends PropertyAccessMap
{
  // There is no object to wrap because the UIManager has
  // only static methods.
  
  private Map   propertyMap_;
  
  private Unbox unbox_;

  public AnyUIManager()
  {
  }
  
  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }
        
      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }
  
  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  /**
   * Puts the key/value pairs contained within <code>objects</code>
   * into the UIDefaults table.
   */
  public void setObjects(Map objects)
  {
    Iter i = objects.createKeysIterator();
    
    while (i.hasNext())
    {
      Any    k  = i.next();
      Any    av = objects.get(k);
      Object ov = unbox(av);
      
      UIManager.put(k.toString(), ov);
    }
  }
  
  public Map getObjects()
  {
    return null; // TBD !!
  }
  
	/**
	 * Returns the string representation of the URL of this file.  By
	 * standardising on URLs we bridge files to all other URL specified
	 * stream types.
	 */
	public String toString()
	{
    return "UIManager";
	}
	
	public int hashCode()
	{
		return 1234;
	}
	
	public boolean equals(Any a)
	{
		return (a instanceof AnyUIManager);
	}

  public Object clone() throws CloneNotSupportedException
	{
    // There is only ever one underlying (static) UIManager
		return this;
	}
	
	public Iter createIterator () {return DegenerateIter.i__;}
	
  public boolean isEmpty() { return false; }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}
  
  private Object unbox(Any v)
  {
    if (unbox_ == null)
      unbox_ = new Unbox();
    
    return unbox_.getObject(v);
  }
  
  // Turns an Any into a suitable object for the UIDefaults table
  static private class Unbox extends AbstractVisitor
  {
		Object o_;
		Any    a_;
		
		public Object getObject(Any a)
		{
      o_ = null;
		
      a.accept(this);

			Object ret = o_;
			o_ = null;
			return ret;
		}
		
    // Left the two-way stuff in all these, just in case...
		public void visitAnyBoolean (BooleanI b)
		{
      if (o_ == null)
        o_ = new Boolean(b.getValue());
      else
        b.setValue(((Boolean)o_).booleanValue());
		}

		public void visitAnyByte (ByteI b)
		{
      if (o_ == null)
        o_ = new Byte(b.getValue());
      else
        b.setValue(((Number)o_).byteValue());
		}

		public void visitAnyChar (CharI c)
		{
      if (o_ == null)
        o_ = new Character(c.getValue());
      else
        c.setValue(((Character)o_).charValue());
		}

		public void visitAnyInt (IntI i)
		{
      if (o_ == null)
        o_ = new Integer(i.getValue());
      else
        i.setValue(((Number)o_).intValue());
		}

		public void visitAnyShort (ShortI s)
		{
      if (o_ == null)
        o_ = new Short(s.getValue());
      else
        s.setValue(((Number)o_).shortValue());
		}

		public void visitAnyLong (LongI l)
		{
      if (o_ == null)
        o_ = new Long(l.getValue());
      else
        l.setValue(((Number)o_).longValue());
		}

		public void visitAnyFloat (FloatI f)
		{
      if (o_ == null)
        o_ = new Float(f.getValue());
      else
        f.setValue(((Number)o_).floatValue());
		}

		public void visitAnyDouble (DoubleI d)
		{
      if (o_ == null)
        o_ = new Double(d.getValue());
      else
        d.setValue(((Number)o_).doubleValue());
		}

		public void visitAnyString (StringI s)
		{
      if (o_ == null)
        o_ = s.getValue();
      else
        s.setValue(o_.toString());
		}

		public void visitAnyObject (ObjectI o)
		{
      if (o_ == null)
      {
        o_ = o.getValue();
        
        // Special handling for Color.  [Anything else? ...]
        if (o_ instanceof java.awt.Color)
          o_ = new javax.swing.plaf.ColorUIResource((java.awt.Color)o_);
      }
      else
        o.setValue(o_);
		}
  }
}

