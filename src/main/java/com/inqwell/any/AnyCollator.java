/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyCollator.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

public class AnyCollator extends    PropertyAccessMap
                         implements Cloneable
{
  private Collator collator_;
  private Map      cache_;    // A cache of StringI to AnyCollationKey 
  private Map      propertyMap_;
  
  // Strengths
  public static IntI PRIMARY = new ConstInt(Collator.PRIMARY);
  public static IntI SECONDARY = new ConstInt(Collator.SECONDARY);
  public static IntI TERTIARY = new ConstInt(Collator.TERTIARY);
  public static IntI IDENTICAL = new ConstInt(Collator.IDENTICAL);
  
  // Decompositions
  public static IntI NO_DECOMPOSITION = new ConstInt(Collator.NO_DECOMPOSITION);
  public static IntI CANONICAL_DECOMPOSITION = new ConstInt(Collator.CANONICAL_DECOMPOSITION);
  public static IntI FULL_DECOMPOSITION = new ConstInt(Collator.FULL_DECOMPOSITION);
  
	public AnyCollator()
	{
	  collator_ = Collator.getInstance();
	  // Note - a quick peek at the JDK source shows that this method
	  // always returns a new instance
	}
	
	public AnyCollator(Collator collator)
	{
		this.collator_ = collator;
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

  public void setPropertyBean(Object bean)
  {
    if (!(bean instanceof Collator))
      throw new IllegalArgumentException(bean.getClass().toString());
      
    collator_ = (Collator)bean;
  }

  public Object getPropertyBean()
  {
    return collator_;
  }

	/**
	 * Returns the string representation of the collator.
	 */
	public String toString()
	{
		if (collator_ == null)
			return "AnyCollator:null";
		
    return collator_.toString();
	}
	
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
	  	if (a instanceof StringI)
	  	{
	  		fromString(a.toString());
	  	}
	  	else if (a instanceof AnyCollator)
	  	{
		    AnyCollator c = (AnyCollator)a;
		    collator_ = c.collator_;
	  	}
	    else
	      throw new AnyRuntimeException(a.getClass().toString() + " is not a collator or a string");
    }
    
    return this;
  }

	public int hashCode()
	{
		return (collator_ == null) ? 0 : collator_.hashCode();
	}
	
	public boolean equals(Any a)
	{
		return (a instanceof AnyCollator) &&
	       (((AnyCollator)a).getCollator().equals(getCollator()));
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		// Clone the underlying object, as collators have properties that
		// can be changed. Of course, we are assuming the object supports
		// cloning...
		AnyCollator a = (AnyCollator)super.clone();
		collator_ = (Collator)collator_.clone();
    a.propertyMap_ = null;
		return a;
	}
	
	public Collator getCollator()
	{
		return collator_;
	}
	
	public Iter createIterator () {return DegenerateIter.i__;}

	/**
	 * Compares <code>source</code> and <code>target</code> using the
	 * underlying <code>Collator</code>. The arguments are converted
	 * to <code>String</code>s by calling <code>toString()</code>, so
	 * they don't have to be strings as such.
	 * @param source
	 * @param target
	 * @return -1, 0 or +1 for less-than, equal-to or greater-than 
	 */
	public int compare(Any source, Any target)
	{
    return collator_.compare(source.toString(), target.toString());
	}

	/**
	 * Compares <code>source</code> and <code>target</code> using the
	 * underlying <code>Collator</code>. The arguments are converted
	 * to <code>String</code>s by calling <code>toString()</code>, so
	 * they don't have to be strings as such.
	 * @param source
	 * @param target
	 * @return true or false, for equal-to or not-equal-to
	 */
	public boolean equals(Any source, Any target)
	{
    return collator_.equals(source.toString(), target.toString());
	}
	
	public AnyCollationKey getCollationKey(StringI s)
	{
    if (cache_ == null)
    	cache_ = AbstractComposite.simpleMap();
    
    if (cache_.contains(s))
    	return (AnyCollationKey)cache_.get(s);
    
    AnyCollationKey k = new AnyCollationKey(collator_.getCollationKey(s.toString()));
    cache_.add(s, k);
    
    return k;
	}
	
	public int getDecomposition()
	{
		return collator_.getDecomposition();
	}
	
	public void setDecomposition(int decompositionMode)
	{
		collator_.setDecomposition(decompositionMode);
	}
	
	public int getStrength()
	{
		return collator_.getStrength();
	}
	
	public void setStrength(int newStrength)
	{
		collator_.setStrength(newStrength);
	}
	
	public void setRules(StringI rules)
	{
		fromString(rules.toString());
	}
	
	public StringI getRules()
	{
		StringI ret = null;
		if (collator_ instanceof RuleBasedCollator)
		{
			RuleBasedCollator c = (RuleBasedCollator)collator_;
			ret = new AnyString(c.getRules());
		}
		return ret;
	}
	
	private void fromString(String rules)
	{
		// Make a new Collator with the supplied string as its rules.
		// Inherit the strength and decomposition properties from any
		// current.
		Collator c;
		try
		{
		  c = new RuleBasedCollator(rules);
		}
		catch (ParseException e)
		{
			throw new RuntimeContainedException(e);
		}
		if (collator_ != null)
		{
			c.setStrength(collator_.getStrength());
			c.setDecomposition(collator_.getDecomposition());
		}
		collator_ = c;
	}
}
