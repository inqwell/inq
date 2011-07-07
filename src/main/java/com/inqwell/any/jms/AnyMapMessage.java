/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.DuplicateChildException;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Visitor;

public class AnyMapMessage extends AnyMessage implements MapMessageI
{
  private MapConverter mc_;
  
  public AnyMapMessage(MapMessage m)
  {
    super(m);
  }

  public AnyMapMessage(Message m)
  {
    super(m);
    
    if (!(m instanceof MapMessage))
      throw new IllegalArgumentException("Not a MapMessage");
  }

  public Any get(Any name, Any type)
  {
    if (mc_ == null)
      mc_ = new MapConverter();
    
    mc_.setOp(name, Visitor.READ);
    type.accept(mc_);
    
    return type;
  }

  public Array getNames()
  {
    try
    {
      Array ret = AbstractComposite.array();
      Enumeration e = getMessage().getMapNames();
      while (e.hasMoreElements())
        ret.add(new AnyString(e.nextElement().toString()));
      
      return ret;
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public boolean itemExists(Any name)
  {
    try
    {
      return getMessage().itemExists(name.toString());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void set(Any name, Any type)
  {
    if (mc_ == null)
      mc_ = new MapConverter();
    
    mc_.setOp(name, Visitor.WRITE);
    type.accept(mc_);
  }
  
  // Inq Map methods
  
  /**
   * Returns the value as a StringI (throwing if the key is not
   * contained within the message).
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      return super.get(key);
    }
    else
    {
      if (!itemExists(key))
        handleNotExist(key); // throws
      
      return new MapMsgString(getKeyAsString(key), key);
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      return super.getIfContains(key);
    }
    else
    {
      Any ret = null;
      if (itemExists(key))
      {
        ret = new MapMsgString(getKeyAsString(key), key);
      }

      return ret;
    }
  }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return itemExists(key);
  }
  
  public void add(Any key, Any value)
  {
    handleDuplicates(key);
    set(key, value);
  }
  
  public void replaceItem(Any key, Any value)
  {
    // MapMessage cannot remove things, just add again
    set(key, value);
  }
  
  protected void handleDuplicates(Any key)
  {
    if (properties__.equals(key) ||
        contains(key))
    {
      throw new DuplicateChildException ("Adding key: " +
                                         key +
                                         " whose value is " +
                                         get(key));
    }
  }

  // End Inq Map methods

  private MapMessage getMessage()
  {
    return (MapMessage)msg_;
  }
  
  public Any getMessageType()
  {
    return map__;
  }

  private String getKeyAsString(Any key)
  {
    // We assume the value can be fetched as a string and that
    // Inq will be used to do the conversion. JMS says this
    // will work for everything except byte[]. In that case
    // we will croak.
    try
    {
      return getMessage().getString(key.toString());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }

  }

  private class MapConverter extends AbstractVisitor
  {
    private Any name_;
    private Any op_;
    
    private void setOp(Any name, Any op)
    {
      name_ = name;
      op_   = op;
    }
    
    public void visitAnyBoolean(BooleanI b)
    {
      try
      {
        if (op_ == Visitor.READ)
          b.setValue(getMessage().getBoolean(name_.toString()));
        else
          getMessage().setBoolean(name_.toString(), b.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyByte(ByteI b)
    {
      try
      {
        if (op_ == Visitor.READ)
          b.setValue(getMessage().getByte(name_.toString()));
        else
          getMessage().setByte(name_.toString(), b.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyDate(DateI d)
    {
      try
      {
        // Perhaps a slightly dubious conversion to Date from long...
        if (op_ == Visitor.READ)
          d.setTime(getMessage().getLong(name_.toString()));
        else
          getMessage().setLong(name_.toString(), d.getTime());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyDouble(DoubleI d)
    {
      try
      {
        if (op_ == Visitor.READ)
          d.setValue(getMessage().getDouble(name_.toString()));
        else
          getMessage().setDouble(name_.toString(), d.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyFloat(FloatI f)
    {
      try
      {
        if (op_ == Visitor.READ)
          f.setValue(getMessage().getFloat(name_.toString()));
        else
          getMessage().setFloat(name_.toString(), f.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyInt(IntI i)
    {
      try
      {
        if (op_ == Visitor.READ)
          i.setValue(getMessage().getInt(name_.toString()));
        else
          getMessage().setInt(name_.toString(), i.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyLong(LongI l)
    {
      try
      {
        if (op_ == Visitor.READ)
          l.setValue(getMessage().getLong(name_.toString()));
        else
          getMessage().setLong(name_.toString(), l.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyObject(ObjectI o)
    {
      try
      {
        if (op_ == Visitor.READ)
          o.setValue(getMessage().getObject(name_.toString()));
        else
          getMessage().setObject(name_.toString(), o.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyShort(ShortI s)
    {
      try
      {
        if (op_ == Visitor.READ)
          s.setValue(getMessage().getShort(name_.toString()));
        else
          getMessage().setShort(name_.toString(), s.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyString(StringI s)
    {
      try
      {
        if (op_ == Visitor.READ)
          s.setValue(getMessage().getString(name_.toString()));
        else
          getMessage().setString(name_.toString(), s.getValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitDecimal(Decimal d)
    {
      try
      {
        if (op_ == Visitor.READ)
          d.fromString(String.valueOf(getMessage().getDouble(name_.toString())));
        else
          getMessage().setDouble(name_.toString(), d.doubleValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
  
  // Inner class creates binding to the MapMessage it came from allowing copyFrom
  // to set existing content
  private class MapMsgString extends AnyString
  {
    private Any key_;
    
    private MapMsgString(String s, Any key)
    {
      super(s);
      key_ = key;
    }
    
    public Any copyFrom(Any a)
    {
      super.copyFrom(a);
      
      AnyMapMessage.this.set(key_, a);
      
      return this;
    }
  }
}
