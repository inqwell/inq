/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyIcon.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyURL;
import com.inqwell.any.Assign;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Equals;
import com.inqwell.any.Iter;
import com.inqwell.any.NotEquals;
import com.inqwell.any.Value;
import com.inqwell.any.Visitor;

public class AnyIcon extends    DefaultPropertyAccessMap
										 implements Value,
                                Cloneable
{
	public static AnyIcon null__ = new AnyIcon((ImageIcon)null);

	private Icon imageIcon_;

	public AnyIcon()
	{
    imageIcon_ = null;
	}

	/**
	 * Construct to wrap a pre-loaded ImageIcon
	 */
	public AnyIcon(ImageIcon i)
	{
    imageIcon_ = i;
	}

	/**
	 * Construct around the possibility that <code>source</code> is
	 * a URL.
	 */
	public AnyIcon(String base, String source)
	{
		processImageSource(base, source);
	}

	public AnyIcon(URL u)
	{
	  imageIcon_ = new ImageIcon(u);
	}

	public AnyIcon(Icon i)
	{
	  imageIcon_ = i;
	}

	public Icon getIcon()
	{
		return imageIcon_;
	}

	public Image getImage()
	{
    Image ret = null;
    
    if (imageIcon_ instanceof ImageIcon)
      ret = ((ImageIcon)imageIcon_).getImage();
    
		return ret;
	}

	public Icon getImageIcon()
	{
		return imageIcon_;
	}
  
  public boolean isNull()
  {
    return imageIcon_ == null;
  }
  
  public void setNull()
  {
    imageIcon_ = null;
  }

  // These intended for property access
  public Any getHeight()
  {
    if (imageIcon_ == null)
      return AnyNull.instance();
    
    return new AnyInt(imageIcon_.getIconHeight());
  }

  public Any getWidth()
  {
    if (imageIcon_ == null)
      return AnyNull.instance();
    
    return new AnyInt(imageIcon_.getIconWidth());
  }
  
  public Any getDescription()
  {
    Any ret = AnyNull.instance();

    if (imageIcon_ != null)
    {
      if (imageIcon_ instanceof ImageIcon)
        ret = new AnyString(((ImageIcon)imageIcon_).getDescription());
    }
    
    return ret;
  }
  // End properties
  
  public Object getPropertyBean()
  {
    return imageIcon_;
  }

  public void setPropertyBean(Object o)
  {
    imageIcon_ = (Icon)o;
  }

  public Any copyFrom (Any a)
  {
    if (a == null || AnyNull.isNullInstance(a))
    {
      setNull();
      return this;
    }
    
    if (a != this)
    {
			if (!(a instanceof AnyIcon))
				throw new IllegalArgumentException("Cannot copy from " + a.getClass());

			AnyIcon i = (AnyIcon)a;
      imageIcon_ = i.imageIcon_;
		}
    return this;
  }

  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // Bit messy but oh well
    // Actually need a better way, must add a method to Visitor...
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  public boolean equals(Any a)
  {
    if (a instanceof AnyIcon)
    {
      AnyIcon i = (AnyIcon)a;
      
      if (i.imageIcon_ == null && imageIcon_ == null)
        return true;
      
      if (imageIcon_ != null)
        return imageIcon_.equals(i.imageIcon_);
      else
        return false;
    }
    
    return false;
  }

  public int hashCode()
  {
    if (imageIcon_ == null)
      return 0;
    
    return imageIcon_.hashCode();
  }
  
  public String toString()
  {
    if (imageIcon_ == null)
      return "icon:null";
    else
      return imageIcon_.toString();
  }
  
	public void processImageSource(String base, String s)
	{
		AnyURL source = new AnyURL(s);

  	URL url = source.getURL(new AnyURL(base));

    imageIcon_ = new ImageIcon(url);
	}
}
