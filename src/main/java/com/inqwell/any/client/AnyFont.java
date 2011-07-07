/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyFont.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

import java.awt.Font;

public class AnyFont extends    PropertyAccessMap
                     implements Cloneable
{
  private Font font_;
  private Map  propertyMap_;

  public AnyFont()
  {
  }
  
  public AnyFont(Font f)
  {
    font_ = f;
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

  public AnyFont getBold()
  {
    return new AnyFont(font_.deriveFont(Font.BOLD));
  }
  
  public AnyFont getItalic()
  {
    return new AnyFont(font_.deriveFont(Font.ITALIC));
  }
  
  public AnyFont getPlain()
  {
    return new AnyFont(font_.deriveFont(Font.PLAIN));
  }
  
  public FloatI getSize()
  {
    return new AnyFloat(font_.getSize2D());
  }
  
  public void setSize(FloatI size)
  {
    font_ = font_.deriveFont(font_.getStyle(), size.getValue());
  }
  
  public void setName(Any name)
  {
    // What ever else this font has, use that but try to
    // create a new one with the given name
    Font curFont = font_;
    font_ = new Font(name.toString(), curFont.getStyle(), curFont.getSize());
    font_ = font_.deriveFont(curFont.getStyle(), curFont.getSize2D());
    //System.err.println("New font " + font_ + " from name " + name);
  }
  
  public void setPropertyBean(Object bean)
  {
    if (!(bean instanceof Font))
      throw new IllegalArgumentException(bean.getClass().toString());
      
    font_ =  (Font)bean;
  }

  public Object getPropertyBean()
  {
    return font_;
  }

  public Any getName()
  {
    return new AnyString(font_.getName());
  }
  
	/**
	 * Returns the string representation of the font.
	 */
	public String toString()
	{
		if (font_ == null)
			return "AnyFont:null";
		
    return font_.toString();
	}
	
  public Any copyFrom (Any a)
  {
    if (!(a instanceof AnyFont))
      throw new AnyRuntimeException(a.getClass().toString() + " is not an AnyFont");
    
    AnyFont f = (AnyFont)a;
    font_ = f.font_;
    return this;
  }

	public int hashCode()
	{
		return (font_ == null) ? 0 : font_.hashCode();
	}
	
	public boolean equals(Any a)
	{
		return (a instanceof AnyFont) &&
	       (((AnyFont)a).getFont().equals(getFont()));
	}
	
	public Object clone() throws CloneNotSupportedException
	{
    // Underlying object remains shared because Font is immutable
		AnyFont a = (AnyFont)super.clone();
    a.propertyMap_ = null;
		return a;
	}
	
	public Iter createIterator () {return DegenerateIter.i__;}
	
  public Font getFont()
  {
    return font_;
  }
  
  public boolean isEmpty() { return false; }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}
}

