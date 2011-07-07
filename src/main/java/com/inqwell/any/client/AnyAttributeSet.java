/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyAttributeSet.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.util.HashMap;

/**
 * Encapsulate the Java <code>AttributeSet</code> in the <code>Any</code>
 * framework. This class acts as a wrapper for <code>MutableAttributeSet</code>
 * implementations.  As a <code>Map</code> it supports named children but these
 * children must themselves be <code>AnyAttributeSet</code>s.
 * <p>
 * The enclosed Java <code>AttributeSet</code> does not know about children
 * but does support a link to a parent style, through which it attempts to
 * resolve attribute requests that it itself does not define.
 * When <code>AnyAttributeSet</code> children are added to
 * an <code>AnyAttributeSet</code> the underlying Java object in the child
 * has its resolve parent set to that in <code>this</code>, so the Any
 * hierarchy is used as a way to create the style hierarchy.
 * By establishing an Any hierarchy of <code>AnyAttributeSet</code>
 * instances then, assuming a naming convention where ever these
 * styles are used, the root of the tree represents a style context.
 * <p>
 * Although a <code>Map</code> the <code>contains(key)</code>
 * and <code>get(key)</code> methods do not enjoy their usual
 * relationship - <code>contains(key)</code> returns <code>true</code>
 * if either <code>this</code> or the underlying attribute set contains
 * the given key.  Special keys are converted to their JDK equivalent
 * name/value pairs in the attribute set - <code>contains(key)</code>
 * doesn't check for this so will always return <code>false</code>
 * in this case.
 */
public class AnyAttributeSet extends    AnyPMap
										         implements Cloneable
{
	public static AnyAttributeSet null__ = new AnyAttributeSet((MutableAttributeSet)null);
	
  // Ready-made key to add child styles on the fly - see
  // table handling in parser.
	public static Any childKey__ = new ConstString("childStyle__");
	
	private MutableAttributeSet attrSet_;

  /**
   * Construct an empty attribute set.  The underlying object is <code>null</code>.
   */
	public AnyAttributeSet()
	{
		super(null);
	}

	/**
	 * Construct to wrap an existing <code>AttributeSet</code>
	 */
	public AnyAttributeSet(MutableAttributeSet s)
	{
		super(null);
		attrSet_ = s;
	}

	/**
	 * Constructs a <code>MutableAttributeSet</code> from the given Map using the Inq
	 * defined well-known values for the attribute keys.
	 */
	public AnyAttributeSet(Map m)
	{
		super(null);
		processMap(m);
	}

	public MutableAttributeSet getAttributeSet()
	{
    if (attrSet_ == null)
      attrSet_ = new SimpleAttributeSet();
      
		return attrSet_;
	}
	
	public void setAttributeSet(MutableAttributeSet as)
	{
		attrSet_ = as;
	}
	
	/**
   * Establish a new <code>AttributeSet</code>.  A number of possibilities are
   * acceptable for the argument:
   * <BL>
   * <LI><B>AnyAttributeSet</B>: The <code>AttributeSet</code> enclosed
   * in <code>a</code> becomes the <code>AttributeSet</code> of <code>this</code>.
   * If the implementation of that <code>AttributeSet</code> is clonable
   * then it will be cloned, otherwise it will be shared between the
   * two <code>AnyAttributeSet</code>s.  If the <code>AttributeSet</code> is
   * cloned and the Any parent of <code>this</code> is
   * a <code>AnyAttributeSet</code> then the <code>AttributeSet</code>
   * resolve parent is set to the object contained in the
   * parent <code>AnyAttributeSet</code>.
   * </LI>
   * <LI><B>Map</B>: The map is processed according to the well-known key values
   * for style attribute keys.  See <code>AnyDocument</code>.
   * </LI>
   * </BL>
   */
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
    	if (a instanceof Map)
    	{
    		Map m = (Map)a;
    		processMap(m);
        if (this.getParentAny() instanceof AnyAttributeSet)
        {
          AnyAttributeSet parent = (AnyAttributeSet)this.getParentAny();
          parent.establishResolveParentInChild(this);
        }
    		return this;
    	}
    		
			if (!(a instanceof AnyAttributeSet))
				throw new IllegalArgumentException("AnyAttributeSet.copyFrom()");

			AnyAttributeSet as = (AnyAttributeSet)a;
			if (as.attrSet_ instanceof SimpleAttributeSet)
			{
        SimpleAttributeSet sa = (SimpleAttributeSet)as.attrSet_;
        attrSet_ = (MutableAttributeSet)sa.clone();
        
        if (this.getParentAny() instanceof AnyAttributeSet)
        {
          AnyAttributeSet parent = (AnyAttributeSet)this.getParentAny();
          parent.establishResolveParentInChild(this);
        }
      }
      else
      {
        // If not clonable then share the attribute set.  Bit unsafe!
        attrSet_ = as.attrSet_;
      }
		}
    return this;
  }

  /**
   * Returns true if the underlying attribute set contains a value
   * by the given key or if the superclass map contains a child style by
   * the key. Note - "true" attributes are not keyed by Any types, so
   * cannot be retrieved once converted and added to the style. 
   */
  public boolean contains (Any key)
  {
    if (attrSet_ != null && (attrSet_.getAttribute(key) != null))
      return true;
    else
    {
      if (getMap() == null)
        setMap(new HashMap());

      return super.contains(key);
    }
  }

  public Any getIfContains (Any key)
  {
    Object o = null;
    // If the key is contained within the attribute set then we return
    // it only if it is an Any
    if (attrSet_ != null &&
        ((o = attrSet_.getAttribute(key)) != null) &&
        o instanceof Any)
      return (Any)o;
    
    // Yes, but it's not an Any
    if (o != null)
      return null;
    
    if (getMap() == null)
      setMap(new HashMap());

    return super.getIfContains(key);    
  }

  public Any get(Any key)
  {
    Object o = null;
    // If the key is contained within the attribute set then we return
    // it only if it is an Any
    if (attrSet_ != null &&
        ((o = attrSet_.getAttribute(key)) != null) &&
        o instanceof Any)
      return (Any)o;
    
    if (o != null)
      handleNotExist(key);
    
    if (getMap() == null)
      setMap(new HashMap());

    return super.get(key);    
  }
  
  /**
   * The <code>Any</code> hierarchy is cloned and the enclosed Java attribute set
   * is cloned also.  This means, respectively, (normally) a deep copy and a
   * shallow copy.
   * <p>
   * In general, it is expensive to clone the root of a style hierarchy, but
   * this is an unlikely thing to do.  Cloning a style leaf is more likely, especially
   * to add auxiliary information, and its OK to do this.
   */
  public Object clone() throws CloneNotSupportedException
  {
    AnyAttributeSet as = (AnyAttributeSet)super.clone();
    if (attrSet_ != null)
    {
      if (attrSet_ instanceof SimpleAttributeSet)
        as.attrSet_ = (MutableAttributeSet)((SimpleAttributeSet)attrSet_).clone();
      else
        as.attrSet_ = null;
    }
    return as;
  }

  public String toString()
  {
  	if (attrSet_ == null) return "null";
  	
  	return attrSet_.toString();
  }
  
	protected boolean beforeAdd(Any key, Any value)
	{
    if (value.getClass() == this.getClass())
    {
      if (getMap() == null)
        setMap(new HashMap());
    
      return true && super.beforeAdd(key, value);
    }
    
    // If the child is not an AnyAttributeSet then add it to
    // the style, not as a child style
    if (attrSet_ == null)
      attrSet_ = new SimpleAttributeSet();
    
    try
    {
      AnyDocument.convertAttribute(key, value, attrSet_);
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    //attrSet_.addAttribute(key, value);

    return false;
  }

	protected void afterAdd(Any key, Any value)
	{
    super.afterAdd(key, value);
    
    // See beforeAdd() - this is always OK
    AnyAttributeSet as = (AnyAttributeSet)value;
    if (as.attrSet_ == null)
      as.attrSet_ = new SimpleAttributeSet();
    establishResolveParentInChild(as);
  }
  
	protected void beforeRemove(Any key)
	{
    if (attrSet_ != null && (attrSet_.getAttribute(key) != null))
      attrSet_.removeAttribute(key);
    else
    {
      AnyAttributeSet child = (AnyAttributeSet)get(key);
      undoResolveParentInChild(child);
      super.beforeRemove(key);
    }
  }
	
	private void processMap(Map m)
	{
		try
		{
	    MutableAttributeSet s = AnyDocument.getRenderStyle(m);
		  attrSet_ = s;
		}
		catch(AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
	}
	
	private void establishResolveParentInChild(AnyAttributeSet child)
	{
    // Set up our attribute set as the resolving parent of the child being added
    if (attrSet_ == null)
      attrSet_ = new SimpleAttributeSet();
      
    child.attrSet_.setResolveParent(attrSet_);
	}
  
	private void undoResolveParentInChild(AnyAttributeSet child)
	{
    //child.attrSet_.setResolveParent(null); // throws!
    child.attrSet_.removeAttribute(StyleConstants.ResolveAttribute);
	}
}
