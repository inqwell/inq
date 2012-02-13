/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/PropertySet.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-20 22:09:17 $
 */

package com.inqwell.any.beans;  
import java.awt.Color;
import java.awt.Font;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractPropertyBinding;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyByte;
import com.inqwell.any.AnyChar;
import com.inqwell.any.AnyDate;
import com.inqwell.any.AnyDouble;
import com.inqwell.any.AnyFloat;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyLong;
import com.inqwell.any.AnyObject;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyShort;
import com.inqwell.any.AnyString;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.CharI;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.client.AnyAttributeSet;
import com.inqwell.any.client.AnyColor;
import com.inqwell.any.client.AnyFont;
import com.inqwell.any.client.AnyIcon;

/**
 * This class when given a java bean will allow easy
 * access to various property bean info.  At present, only write
 * access to an object's properties is supported.
 */
public class PropertySet extends AbstractAny
{
  private static final long serialVersionUID = 1L;
  private Map    set_;
  private Class  owningClass_;
  
  static private ClassMap propertyClassMap__;
  static private ConvertPrimitive convertPrimitive__;
  static public  Any anyClass__;
  //static private Any anyPropertyBinding__;
  
  static
  {
		convertPrimitive__ = new ConvertPrimitive();
		
		anyClass__           = new AnyObject(Any.class);
		//anyPropertyBinding__ = new AnyObject(PropertySet.PropertyBinding.class);
		
		// Initialise the propertyClassMap__ with the Any types
		// to be used for each supported Java Beans property type
		propertyClassMap__ = new ClassMap();
		
		propertyClassMap__.add(String.class, new AnyString());
		propertyClassMap__.add(Icon.class, new AnyIcon((ImageIcon)null));
		propertyClassMap__.add(Color.class, new AnyColor((Color)null));
		propertyClassMap__.add(Font.class, new AnyFont((Font)null));
		propertyClassMap__.add(AttributeSet.class, new AnyAttributeSet((MutableAttributeSet)null));
		propertyClassMap__.add(boolean.class, new AnyBoolean());
		propertyClassMap__.add(int.class, new AnyInt());
		propertyClassMap__.add(char.class, new AnyChar());
		propertyClassMap__.add(byte.class, new AnyByte());
		propertyClassMap__.add(short.class, new AnyShort());
		propertyClassMap__.add(long.class, new AnyLong());
		propertyClassMap__.add(float.class, new AnyFloat());
		propertyClassMap__.add(double.class, new AnyDouble());
		propertyClassMap__.add(java.util.Date.class, new AnyDate());
		propertyClassMap__.add(Any.class, anyClass__);
		propertyClassMap__.add(Object.class, anyClass__);
		//propertyClassMap__.add(PropertySet.PropertyBinding.class, anyPropertyBinding__);
		
		// add more classes here.
  }
  
  public PropertySet(Object bean)
  {
    set_        = AbstractComposite.map();
    
    Class beanClass = bean.getClass();
    
    try
    {
      BeanInfo bi = Introspector.getBeanInfo(beanClass);
      PropertyDescriptor[] propSet = bi.getPropertyDescriptors();
      for (int i =0; i < propSet.length; i++)
      {
        Method write = propSet[i].getWriteMethod();
        Method read  = propSet[i].getReadMethod();
        Class  type  = propSet[i].getPropertyType();
        String name  = propSet[i].getName();
        
        PropertyInfo pi = new PropertyInfo(name, read, write, type);
        
        set_.add(AbstractValue.flyweightString(name), pi);
				//System.out.println ("Property Name: " + name + " type " + type);
      }
    }
    catch (IntrospectionException inX)
    {
			throw new RuntimeContainedException(inX);
    }
    owningClass_ = beanClass;
  }
  
  public Method getWriteMethod(Any property)
  {
    Method m = null;
		PropertyInfo pi = (PropertyInfo)set_.get(property);
		m = pi.getWriteMethod();
    return m;
  }

  public Method getReadMethod(Any property)
  {
    Method m = null;
		PropertyInfo pi = (PropertyInfo)set_.get(property);
		m = pi.getReadMethod();
    return m;
  }

  public Class getType(Any property)
  {
    Class        c  = null;
		if (!set_.contains(property))
		{
			throw new IllegalArgumentException("Unknown property " + property);
		}

		PropertyInfo pi = (PropertyInfo)set_.get(property);
		
		c = pi.getType();
    return c;
  }

  public Class getOwningClass()
  {
    return owningClass_;
  }
    
	public Any getTargetAnyValue(Any property)
	{
		Class c = getType(property);
		Any a = propertyClassMap__.get(c);
		
		if (a == null)
		{
			throw new IllegalArgumentException("Unsupported property type: " + c);
		}

		return a;
	}
		
	public Object getTargetObjValue(Any property, Any value, Any source)
	{
    // Get a temporary for the current thread
		value = Globals.getProcessForCurrentThread().getTransaction().getTemporary(value);
		value.copyFrom(source);
		
		return convertPrimitive__.getObject(value);
	}
	
	public BeansPropertyBinding makePropertyBinding(Object o, Any property)
	{
    return makePropertyBinding(o, property, null);
	}
	
	public BeansPropertyBinding makePropertyBinding(Object o, Any property, BeansPropertyBinding b)
	{
		if (!set_.contains(property))
		{
		  //return null;
		  
		  // TODO Choose between above and below
			throw new IllegalArgumentException("Unsupported property " +
			                                   property +
			                                   " on "
			                                   + o.getClass());
		}

    PropertyInfo pi = (PropertyInfo)set_.get(property);
    
    if (b != null)
    {
      // When we are given a ready-made binding just put in the
      // PropertyInfo and return same
      b.setPropertyInfo(pi);
      return b;
    }
    
    return new BeansPropertyBinding(pi, o);
	}
	
  static private class PropertyInfo extends AbstractAny
  {
    private static final long serialVersionUID = 1L;
    String name_;
    Method read_;
    Method write_;
    Class  type_;
    
    public PropertyInfo(String propertyName, Method read, Method write, Class type)
    {
      name_  = propertyName;
      read_  = read;
      write_ = write;
      type_  = type;
    }
    
    public String getName() { return name_; }
    public Method getWriteMethod() { return write_; }
    public Method getReadMethod()  { return read_; }
    public Class  getType() { return type_; }
    
    public String toString()
    {
      return name_;
    }
  }
  
  // Convert between Java objects to and their corresponding
  // Anys.  If o_ is non-null before accept then convert to Any.
  // Otherwise convert from to Java object and leave in o_
  static private class ConvertPrimitive extends AbstractVisitor
  {
    private static final long serialVersionUID = 1L;
    Object o_;
		Any    a_;
		
		public synchronized Object getObject(Any a)
		{
      // if there's no primitive to convert to because the
      // property type is Any then just return the mapping
      // as a no-op
      if (a == anyClass__)
        return a;
        
      o_ = null;
		
      a.accept(this);

			Object ret = o_;
			o_ = null;
			return ret;
		}
		
		public synchronized Any getAny(Any a, Object o)
		{
      a_ = a;
      o_ = o;
      a.accept(this);
      a = a_.cloneAny();
      a_ = null;
      o_ = null;
      return a;
    }
		
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
      {
        // For strings Inq null translates to hard null 
        if (!s.isNull())
          o_ = s.getValue();
      }
      else
        s.setValue(o_.toString());
    }

		public void visitMap (Map m)
    {
      // We assume from a PropertyAccessMap, in which case we hope
      // the derived has implemented get/setPropertyBean
      if (o_ == null)
        o_ = m.getPropertyBean();
      else
        m.setPropertyBean(o_);
    }
    
		public void visitAnyObject (ObjectI o)
		{
      if (o_ == null)
        o_ = o.getValue();
      else
        o.setValue(o_);
		}
  }
  
  // Support read/write actions on given property of given
  // object.
  static public class BeansPropertyBinding extends AbstractPropertyBinding
  {
    private static final long serialVersionUID = 1L;
    private PropertyInfo i_;
    private Object       o_;
    private Object[] propertyArgs_ = new Object[1];
    private static Object[] readArgs_ = new Object[0];
    
    public BeansPropertyBinding (PropertyInfo i, Object o)
    {
      i_ = i;
      o_ = o;
    }
    
    public void setPropertyInfo(Any pi)
    {
      i_ = (PropertyInfo)pi;
    }
    
    public void setProperty(Any value)
    {
      Any propertyValueAny = propertyClassMap__.get(i_.getType());
      if (propertyValueAny == null)
      {
        throw new IllegalArgumentException("Unsupported property type: " + i_.getType());
      }
      
      if (value instanceof PropertySet.BeansPropertyBinding)
      {
        // Setting this property from another property binding.
        // Read that property and try again
        PropertySet.BeansPropertyBinding b = (PropertySet.BeansPropertyBinding)value;
        value = b.getProperty();
        setProperty(value);
        return;
      }

      if (propertyValueAny == anyClass__)
      {
        // If the property type is Any then take value as is
        propertyArgs_[0] = value;
      }
      else
      {
        // Get a temporary for this thread
        propertyValueAny =
          Globals.getProcessForCurrentThread().getTransaction().getTemporary(propertyValueAny);
        
        propertyValueAny.copyFrom(value);

        Object propertyValueObject = convertPrimitive__.getObject(propertyValueAny);
        propertyArgs_[0] = propertyValueObject;
      }

      //System.out.println ("PropertyBinding.setProperty object is " + value);
      //System.out.println ("PropertyBinding.setProperty method is " + i_.getWriteMethod());
      //System.out.println ("PropertyBinding.setProperty target is " + o_);
      
      doWriteProperty(o_, propertyArgs_, i_.getWriteMethod());
    }
    
    public Any getProperty()
    {
      Any propertyValueAny = propertyClassMap__.get(i_.getType());
      if (propertyValueAny == null)
      {
        throw new IllegalArgumentException("Unsupported property type: " + i_.getType());
      }

      Object o = doReadProperty(o_, i_.getReadMethod());
      //System.out.println("Got Property Object " + o.getClass() + " " + o);
      //System.out.println("propertyValueAny is  " + propertyValueAny);
      Any ret = null;
      if (propertyValueAny == anyClass__)
        return (Any)o;
      else
      {
        // Get a temporary for the current thread
        propertyValueAny =
          Globals.getProcessForCurrentThread().getTransaction().getTemporary(propertyValueAny);
        ret = convertPrimitive__.getAny(propertyValueAny, o);
      }
        
      return ret;
    }
    
    protected String getName()
    {
      return i_.getName();
    }
    
    protected void doWriteProperty(Object o, Object[] args, Method m)
    {
      try
      {
        //System.out.println("doWriteProperty " + o + " " + args + " " + m);
        if (m == null)
          throw new AnyRuntimeException("No write method for " + getName());

        m.invoke(o, args);
      }
			catch (InvocationTargetException itx)
			{
				throw new RuntimeContainedException(itx.getTargetException());
			}
			catch (Exception e)
			{
				throw new RuntimeContainedException(e);
			}
    }

    protected Object doReadProperty(Object o, Method m)
    {
      Object ro = null;
      try
      {
        ro = m.invoke(o, readArgs_);
      }
			catch (InvocationTargetException itx)
			{
				throw new RuntimeContainedException(itx.getTargetException());
			}
			catch (Exception e)
			{
				throw new RuntimeContainedException(e);
			}
			return ro;
    }
  }
}
