/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/SetterFactory.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-18 21:45:00 $
 */

package com.inqwell.any.client;

import java.awt.Component;
import java.awt.FontMetrics;
import java.text.Format;
import java.util.Arrays;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyString;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ConstByte;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ObjectI;
import com.inqwell.any.Value;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.beans.Setter;
import com.inqwell.any.client.swing.JTextPane;
import com.inqwell.any.client.swing.SwingInvoker;

//import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This class maintains a register of the Setters we can handle
 * keyed on class name.
 * 
 * *** Being deprecated ***
 */
public class SetterFactory extends AbstractAny
{
  private static ClassMap  factoryMap__;
  public static  Any       doNothing__ = new ConstByte();

  static
  {
    factoryMap__ = new ClassMap();
    factoryMap__.add(javax.swing.JComboBox.class, new JComboBoxSetter());
  }


	public static Setter getSetter(Object o)
	{
    Setter s = (Setter)factoryMap__.get(o);
    if (s == null)
      throw new IllegalArgumentException(o.getClass().toString());
      
	  return (Setter)s.cloneAny();
	}

	public static Any getText(Any a)
	{
		if (a == null)
			return AnyString.EMPTY;

		if (a instanceof ObjectI)
			return new AnyString(a.toString());  // temporary - look at combo box rendering and setting again soon!

		if (!(a instanceof Map))
			return a;

		Map m = (Map)a;

		return (m.contains(NodeSpecification.atTxt__))
							? m.get(NodeSpecification.atTxt__)
							: null;
	}

	public static AnyIcon getIcon(Any a)
	{
		if (a == null)
			return null;

		if (a instanceof AnyIcon)
			return (AnyIcon)a;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyIcon)(m.contains(NodeSpecification.ico__)
											 ? m.get(NodeSpecification.ico__)
											 : null);
	}

	public static AnyIcon getTreeLeafIcon(Any a)
	{
		if (a == null)
			return null;

		if (a instanceof AnyIcon)
			return (AnyIcon)a;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyIcon)(m.contains(NodeSpecification.icoLeaf__)
											 ? m.get(NodeSpecification.icoLeaf__)
											 : null);
	}

	public static AnyIcon getTreeOpenIcon(Any a)
	{
		if (a == null)
			return null;

		if (a instanceof AnyIcon)
			return (AnyIcon)a;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyIcon)(m.contains(NodeSpecification.icoOpen__)
											 ? m.get(NodeSpecification.icoOpen__)
											 : null);
	}

	public static AnyIcon getTreeClosedIcon(Any a)
	{
		if (a == null)
			return null;

		if (a instanceof AnyIcon)
			return (AnyIcon)a;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyIcon)(m.contains(NodeSpecification.icoClosed__)
											 ? m.get(NodeSpecification.icoClosed__)
											 : null);
	}

	public static AnyColor getBgCol(Any a)
	{
		if (a == null)
			return null;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyColor)(m.contains(NodeSpecification.bg__)
											 ? m.get(NodeSpecification.bg__)
											 : null);
	}

	public static AnyColor getFgCol(Any a)
	{
		if (a == null)
			return null;

		if (!(a instanceof Map))
			return null;

		Map m = (Map)a;

		return (AnyColor)(m.contains(NodeSpecification.fg__)
											 ? m.get(NodeSpecification.fg__)
											 : null);
	}

  public static String formatText(Format f, Any a)
  {
		if (a == null)
			return AnyString.EMPTY.toString();

		if (f != null)
			return f.format(a);
		else
			return a.toString();
  }
}

abstract class AbstractSetter extends AbstractAny implements Setter
{
	public void setFormat(Format f) {}

	public Any getDefaultValue(RenderInfo r, Component c)
	{
    return SetterFactory.doNothing__;
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}

class JComboBoxSetter extends AbstractSetter implements Setter
{
	private SetItem setItem_;
	private GetItem getItem_;

  public void set(Any val, Object o)
  {
    javax.swing.JComboBox cb = (javax.swing.JComboBox)o;

//		if (val != null)
//		{
			if (setItem_ == null)
				setItem_ = new SetItem(cb, val);
			else
				setItem_.setVal(val);

			setItem_.maybeSync();
//		}
  }

	public Any get(Object o)
	{
    javax.swing.JComboBox cb = (javax.swing.JComboBox)o;

    if (getItem_ == null)
			getItem_ = new GetItem(cb);

		getItem_.maybeSync();

		return getItem_.getVal();
	}

	public void setFormat(Format f) {}

  private class SetItem extends SwingInvoker
  {
		javax.swing.JComboBox c_;
		Any val_;

		SetItem (javax.swing.JComboBox c, Any val)
		{
			c_   = c;
			val_ = val;
		}

		public void setVal(Any val)
		{
			val_ = val;
		}

		protected void doSwing()
		{
      //System.out.println("JComboBoxSetter " + val_);
      //System.out.println("Model " + c_.getModel());
      //c_.getModel().setSelectedItem(val_);
      if ((val_ instanceof Value) && ((Value)val_).isNull())
        c_.setSelectedItem(null);
      else
        c_.setSelectedItem(val_);

			val_ = null;
		}
  }

  private class GetItem extends SwingInvoker
  {
		javax.swing.JComboBox c_;
		Any                   value_;

		GetItem (javax.swing.JComboBox c)
		{
			c_  = c;
		}

		protected void doSwing()
		{
			// According to the AnyListModel and associated classes we
			// can get a Map containing the internal and external
			// values from the AnyListModel.  Return the internal value
			// If the combo box is editable we can get a string.
      value_ = (Any)c_.getSelectedItem();
      if (value_ instanceof Map)
      {
				Map m = (Map)value_;
				//System.out.println ("JComboBoxSetter.get() : " + m);
				value_ = m.get(ListRenderInfo.internal__);
      }
		}

		public Any getVal()
		{
			Any value = value_;
			value_    = null;
			return value;
		}
  }
}
