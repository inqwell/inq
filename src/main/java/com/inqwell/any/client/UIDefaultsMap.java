/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/UIDefaultsMap.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.Enumeration;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.inqwell.any.Any;
import com.inqwell.any.AnyArray;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyMap;
import com.inqwell.any.AnyNull;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;

/**
 * A Map interface on to the UIDefaults for the current PLAF.
 * The implementation never contains any children itself, instead
 * it just queries UIDefaults and returns the current value.
 * In this way the items returned are always those for the
 * prevailing L&F.
 * <p>
 * A typical UIDefault key is something like <code>Button.font</code>.
 * This is mapped to a two-level node structure
 *
 */
public class UIDefaultsMap extends AnyMap
{
  // The currently prevailing UIDefaults
  static private UIDefaults uiDefaults__;

  // There is only ever one UIDefaultsMap.
  // It contains the subset of the key values within UIDefaults that we
  // support at the that scripts can use.
  static private UIDefaultsMap uiDefaultsMap__;

  // Hide the default ctor
  private UIDefaultsMap() {}

  protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) { throw new UnsupportedOperationException(); }
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  static public Map getUIDefaults()
  {
    synchronized(UIDefaultsMap.class)
    {
      if (uiDefaults__ == null ||
          uiDefaults__ != UIManager.getLookAndFeelDefaults())
      {
        uiDefaultsMap__ = new UIDefaultsMap();

        uiDefaults__ = UIManager.getLookAndFeelDefaults();
        Enumeration e = uiDefaults__.keys();
        while (e.hasMoreElements())
        {
          Object key = e.nextElement();
          Any k = generateKey(key);
          //System.out.println(key.toString() + " " + key.getClass() + " " + k);
          if (k != null && !uiDefaultsMap__.contains(k))
            uiDefaultsMap__.add(k, uiDefaultsMap__.createUIDefaultMap(k));
        }
      }
    }
    return uiDefaultsMap__;
  }

  public Iter createIterator ()
  {
    return new UIDefaultsIter(Map.I_VALUES);
  }

  public Iter createKeysIterator ()
  {
    return new UIDefaultsIter(Map.I_KEYS);
  }

  private Map createUIDefaultMap(Any k)
  {
    return new UIDefaultMap(k);
  }

  static private Any generateKey(Object key)
  {
    // Creates a string for the component name of the UIDefault
    // entry.
    // Component name is found before the first "."

    String componentName = key.toString();

    if (uiDefaults__.get(componentName) == null)
      return null;

		if (componentName.startsWith("class") ||
        componentName.startsWith("javax"))
			return null;

    int pos = componentName.indexOf( "." );

    // Throw away the UI defaults. These are classes and of no use to
    // scripts. Those that don't have a component prefix are the
    // system colours, so we make one for them.
    if (pos == -1)
      if (componentName.endsWith( "UI" ) )
        return null;
      else
        componentName = "SystemColors";
    else
      componentName = componentName.substring(0, pos);

    //  Fix inconsistency
    if (componentName.equals("Checkbox"))
      componentName = "CheckBox";

    return new ConstString(componentName);
  }

  // Class to support the individual component UIDefault values.
  // Lazily create the values as we request them.
  private class UIDefaultMap extends AnyMap
  {
    private Any parentKey_;

    public UIDefaultMap(Any parentKey)
    {
      parentKey_ = parentKey;
    }

    public Iter createIterator ()
    {
      // For keys and values, we'll iterate over whatever is there at the moment,
      // not everything that could be there.
      return new UIDefaultsIter(Map.I_VALUES);
    }

    public Iter createKeysIterator ()
    {
      return new UIDefaultsIter(Map.I_KEYS);
    }
    
    public boolean contains(Any key)
    {
      String s = parentKey_.toString() + "." + key.toString();
      return uiDefaults__.containsKey(s);
    }

    public Any get (Any key)
    {
      // Make a key and see if its in uiDefaults__. If it is
      // and its a supported type make the Any equivalent.
      // Once made, cache in the map.
      if (!super.contains(key))
      {
        Any val = cacheDefault(key);
        if (val == null)
          val = AnyNull.instance(); //handleNotExist(key);   // throws
        
        return val;
      }

      return super.get(key);
    }

    public Any getIfContains(Any key)
    {
      Any ret = super.getIfContains(key);
      if (ret == null)
      {
        ret = cacheDefault(key);
      }
      return ret;
    }

    protected boolean beforeAdd(Any key, Any value) { return true; }
  	protected void afterAdd(Any key, Any value) {}
  	protected void beforeRemove(Any key) { throw new UnsupportedOperationException(); }
  	protected void afterRemove(Any key, Any value) {}
  	protected void emptying() {}
    
    private Any cacheDefault(Any key)
    {
      Any ret = null;
      
      String s = parentKey_.toString() + "." + key.toString();
      if (!uiDefaults__.containsKey(s))
        return AnyNull.instance(); //handleNotExist(key);   // throws

      Object o = uiDefaults__.get(s);

      synchronized(this)
      {
        if (o instanceof Color)
        {
          Color color = (Color)o;
          this.add(key, ret = new UIColor(color, s));
        }
        else if (o instanceof Font)
        {
          Font font = (Font)o;
          this.add(key, ret = new UIFont(font, s));
        }
        else if (o instanceof Icon)
        {
          Icon icon = (Icon)o;
          this.add(key, ret = new UIIcon(icon, s));
        }
        else if (o instanceof Insets)
        {
          Insets insets = (Insets)o;
          this.add(key, ret = new UIInsets(insets, s));
        }
        else if (o instanceof Integer)
        {
          Integer i = (Integer)o;
          this.add(key, ret = new UIInt(i, s));
        }
      }
      return ret;
    }
  }
  
  // We don't allow removal from UIDefaults
	private class UIDefaultsIter extends AnyMapIter
	{
	  public UIDefaultsIter(int mode)
	  {
	    super(mode);
	  }

		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

	private class UIColor extends AnyColor
	{
	  private String key_;

	  public UIColor(Color c, String key)
	  {
	    super(c);
	    setUIDefaultsKey(key);
	  }

    public Any copyFrom (Any a)
    {
      super.copyFrom(a);

      // If we know the key, set the UIDefault
      if (key_ != null)
        uiDefaults__.put(key_, getColor());

      return this;
    }

    private void setUIDefaultsKey(String key)
    {
      key_ = key;
    }
	}

	private class UIFont extends AnyFont
	{
	  private String key_;

	  public UIFont(Font f, String key)
	  {
	    super(f);
	    setUIDefaultsKey(key);
	  }

    public Any copyFrom (Any a)
    {
      super.copyFrom(a);

      // If we know the key, set the UIDefault
      if (key_ != null)
        uiDefaults__.put(key_, getFont());

      return this;
    }

    private void setUIDefaultsKey(String key)
    {
      key_ = key;
    }
	}

	private class UIIcon extends AnyIcon
	{
	  private String key_;

	  public UIIcon(Icon i, String key)
	  {
	    super(i);
	    setUIDefaultsKey(key);
	  }

    public Any copyFrom (Any a)
    {
      super.copyFrom(a);

      // If we know the key, set the UIDefault
      if (key_ != null)
        uiDefaults__.put(key_, getIcon());

      return this;
    }

    private void setUIDefaultsKey(String key)
    {
      key_ = key;
    }
	}

	private class UIInt extends AnyInt
	{
	  private String key_;

	  public UIInt(Integer i, String key)
	  {
	    super(i.intValue());
	    setUIDefaultsKey(key);
	  }

    public Any copyFrom (Any a)
    {
      super.copyFrom(a);

      // If we know the key, set the UIDefault
      if (key_ != null)
        uiDefaults__.put(key_, new Integer(getValue()));

      return this;
    }

    private void setUIDefaultsKey(String key)
    {
      key_ = key;
    }
	}
  
	private class UIInsets extends AnyArray
	{
	  private String key_;

	  public UIInsets(Insets insets, String key)
	  {
      super(4);
	    this.add(new ConstInt(insets.top));
	    this.add(new ConstInt(insets.left));
	    this.add(new ConstInt(insets.bottom));
	    this.add(new ConstInt(insets.right));
	    setUIDefaultsKey(key);
	  }

    public Any copyFrom (Any a)
    {
      super.copyFrom(a);

      // If we know the key, set the UIDefault
      if (key_ != null)
      {
        int top    = ((IntI)this.get(0)).getValue();
        int left   = ((IntI)this.get(1)).getValue();
        int bottom = ((IntI)this.get(2)).getValue();
        int right  = ((IntI)this.get(3)).getValue();
        Insets i = new Insets(top, left, bottom, right);
        uiDefaults__.put(key_, i);
      }

      return this;
    }

    private void setUIDefaultsKey(String key)
    {
      key_ = key;
    }
	}
}
