/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyColor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Color;

import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.Assign;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Equals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.NotEquals;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.StringI;
import com.inqwell.any.Visitor;

public class AnyColor extends    DefaultPropertyAccessMap
											implements Cloneable
{
	public static AnyColor null__ = new AnyColor((Color)null);
	
  private Color color_;
	
	/**
	 * Construct to wrap a pre-loaded Color
	 */
	public AnyColor(Color c)
	{
		color_ = c;
	}
	
	/**
	 * Construct around the possibilities that <code>source</code> is
	 * either a URL or a byte array
	 */
	public AnyColor(Any source)
	{
		processColor(source.toString());
	}
	
	public AnyColor(String source)
	{
		processColor(source);
	}
	
	public AnyColor()
  {
	  color_ = Color.BLACK;
  }

	public Color getColor()
	{
		return color_;
	}
	
  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // See AnyFile.accept also
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (a instanceof StringI)
      {
        processColor(a.toString());
        return this;
      }
      else if (a instanceof Array)
      {
        // Assume 0-255 values
        processColor((Array)a);
        return this;
      }
      
			if (!(a instanceof AnyColor))
				throw new IllegalArgumentException("AnyColor.copyFrom()");
			
			AnyColor i = (AnyColor)a;
			this.color_ = i.color_;
		}
    return this;
  }
  
  // Properties

  public AnyColor getDarker()
  {
    return new AnyColor(color_.darker());
  }
  
  public AnyColor getBrighter()
  {
    return new AnyColor(color_.brighter());
  }
  
  public void setAlpha(Any alpha)
  {
    if (!(alpha instanceof IntI))
      throw new AnyRuntimeException("Only integer alpha supported");
    
    IntI i = (IntI)alpha;
    
    color_ = new Color(color_.getRed(),
                       color_.getGreen(),
                       color_.getBlue(),
                       i.getValue());
  }
  
  public Any getAlpha()
  {
    return new AnyInt(color_.getAlpha());
  }

  public void setAdjustAlpha(Any alpha)
  {
    if (!(alpha instanceof IntI))
      throw new AnyRuntimeException("Only integer alpha supported");
    
    IntI i = (IntI)alpha;
    
    color_ = new Color(color_.getRed(),
                       color_.getGreen(),
                       color_.getBlue(),
                       color_.getAlpha() + i.getValue());
    
  }
  
  public Object getPropertyBean()
  {
    return color_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }


  public boolean equals(Any a)
  {
    return (a instanceof AnyColor) &&
         (((AnyColor)a).color_.equals(color_));
  }

	private void processColor(String s)
	{
		try
		{
  		Color c = Color.decode(s);

		  color_ = c;
		}
		
		catch (NumberFormatException e)
		{
			throw new RuntimeContainedException(e);
		}
	}

	private void processColor(Array ar)
	{
    if(ar.entries() != 3 && ar.entries() != 4)
      throw new IllegalArgumentException("Expected 3 or 4 items in the array");

    IntI i = new AnyInt();
    int r, g, b, a = 255;
    
    Any v = ar.get(0);
    i.copyFrom(v);
    r = i.getValue();
    
    v = ar.get(1);
    i.copyFrom(v);
    g = i.getValue();

    v = ar.get(2);
    i.copyFrom(v);
    b = i.getValue();
    
    if (ar.entries() == 4)
    {
      v = ar.get(2);
      i.copyFrom(v);
      a = i.getValue();
    }
    
    Color c =  new Color(r, g, b, a);
    
    color_ = c;
  }
}
