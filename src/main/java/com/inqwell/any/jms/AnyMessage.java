/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractPropertyBinding;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyByteArray;
import com.inqwell.any.AnyString;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.IntI;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.ObjectI;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.PropertyBinding;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Visitor;

public class AnyMessage extends DefaultPropertyAccessMap implements MessageI
{
  // Magic key for client properties, as opposed to beans properties
  static public Any client__ = AbstractValue.flyweightString("client");
  
  // Return values for the various message types, should script need
  // to test for type it has received
  static public Any message__ = AbstractValue.flyweightString("MESSAGE");
  static public Any bytes__   = AbstractValue.flyweightString("BYTES");
  static public Any map__     = AbstractValue.flyweightString("MAP");
  static public Any object__  = AbstractValue.flyweightString("OBJECT");
  static public Any stream__  = AbstractValue.flyweightString("STREAM");
  static public Any text__    = AbstractValue.flyweightString("TEXT");

  protected Message msg_;
  
  private Map     clientProperties_;
  private StringI propVal_;
  
  // Really must get around to making visitors and funcs reentrant.
  // As well, this makes AnyMessage instances non-reentrant
  private PropertyConverter pc_;
  
//  static
//  {
//    wrapProperties__ = AbstractComposite.set();
//    
//  }
  
  /**
   * Make the wrapper appropriate to the message 
   */
  public static AnyMessage makeMessage(Message m)
  {
    AnyMessage ret = null;

    if (m != null)
    {
      // TODO use a map? May be
  
      if (m instanceof BytesMessage)
        ret = new AnyBytesMessage(m);
      else if (m instanceof MapMessage)
        ret = new AnyMapMessage(m);
      else if (m instanceof ObjectMessage)
        ret = new AnyObjectMessage(m);
      else if (m instanceof StreamMessage)
        ret = new AnyStreamMessage(m);
      else if (m instanceof TextMessage)
        ret = new AnyTextMessage(m);
      else
        ret = new AnyMessage(m);
    }
    
    return ret;
  }

  public AnyMessage(Message m)
  {
    msg_ = m;
  }
  
  public void acknowledge()
  {
    try
    {
      msg_.acknowledge();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void clearBody()
  {
    try
    {
      msg_.clearBody();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void clearProperties()
  {
    try
    {
      msg_.clearProperties();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public String getJMSCorrelationID()
  {
    try
    {
      return msg_.getJMSCorrelationID();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getJMSCorrelationIDAsBytes()
  {
    try
    {
      return new AnyByteArray(msg_.getJMSCorrelationIDAsBytes());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public int getJMSDeliveryMode()
  {
    try
    {
      return msg_.getJMSDeliveryMode();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getJMSDestination()
  {
    try
    {
      return new AnyDestination(msg_.getJMSDestination());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public long getJMSExpiration()
  {
    try
    {
      return msg_.getJMSExpiration();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public String getJMSMessageID()
  {
    try
    {
      return msg_.getJMSMessageID();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public Message getJMSMessage()
  {
    return msg_;
  }

  public int getJMSPriority()
  {
    try
    {
      return msg_.getJMSPriority();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public boolean getJMSRedelivered()
  {
    try
    {
      return msg_.getJMSRedelivered();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getJMSReplyTo()
  {
    try
    {
      return new AnyDestination(msg_.getJMSReplyTo());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public long getJMSTimestamp()
  {
    try
    {
      return msg_.getJMSTimestamp();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getJMSType()
  {
    try
    {
      return new AnyString(msg_.getJMSType());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public Any getMessageType()
  {
    return message__;
  }

  public Map getClientProperties()
  {
    Map m = null;
    try
    {
      Enumeration e = msg_.getPropertyNames();
      
      if (e.hasMoreElements())
        m = AbstractComposite.simpleMap();
      
      while (e.hasMoreElements())
      {
        Object o = e.nextElement();
        Any    k = AbstractValue.flyweightString(o.toString());
        Any    v = AbstractValue.flyweightString(msg_.getStringProperty(o.toString()));
        m.add(k, v);
      }
      
      return m;
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any getProperty(Any name, Any type)
  {
    if (pc_ == null)
      pc_ = new PropertyConverter();
    
    pc_.setOp(name, Visitor.READ);
    type.accept(pc_);
    
    return type;
  }

  public void setProperty(Any name, Any type)
  {
    if (pc_ == null)
      pc_ = new PropertyConverter();
    
    pc_.setOp(name, Visitor.WRITE);
    type.accept(pc_);
  }

  public boolean propertyExists(Any name)
  {
    try
    {
      return msg_.propertyExists(name.toString());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSCorrelationID(String correlationID)
  {
    try
    {
      msg_.setJMSCorrelationID(correlationID);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSCorrelationIDAsBytes(Any correlationID)
  {
    try
    {
      if (!(correlationID instanceof AnyByteArray))
        throw new IllegalArgumentException("Not a AnyByteArray");
      
      msg_.setJMSCorrelationIDAsBytes(((AnyByteArray)correlationID).getValue());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSDeliveryMode(int deliveryMode)
  {
    try
    {
      msg_.setJMSDeliveryMode(deliveryMode);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSDestination(Any destination)
  {
    try
    {
      msg_.setJMSDestination(((AnyDestination)destination).getDestination());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSExpiration(long expiration)
  {
    try
    {
      msg_.setJMSExpiration(expiration);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSMessageID(String id)
  {
    try
    {
      msg_.setJMSMessageID(id);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSPriority(int priority)
  {
    try
    {
      msg_.setJMSPriority(priority);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSRedelivered(boolean redelivered)
  {
    try
    {
      msg_.setJMSRedelivered(redelivered);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSReplyTo(Any replyTo)
  {
    try
    {
      msg_.setJMSReplyTo(((AnyDestination)replyTo).getDestination());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSTimestamp(long timestamp)
  {
    try
    {
      msg_.setJMSTimestamp(timestamp);
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void setJMSDatestamp(DateI timestamp)
  {
    setJMSTimestamp(timestamp.getTime());
  }
  
  public void setJMSType(Any type)
  {
    try
    {
      msg_.setJMSType(type.toString());
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public String toString()
  {
    return msg_.toString();
  }

  public Any get(Any key)
  {
    if (key.equals(client__))
    {
      if (clientProperties_ == null)
      {
        clientProperties_ = new MessagePropertyMap(this);
      }

      return clientProperties_;
    }
    else
      return super.get(key);
  }

  public Any getIfContains(Any key)
  {
    if (key.equals(client__))
    {
      if (clientProperties_ == null)
      {
        clientProperties_ = new MessagePropertyMap(this);
      }

      return clientProperties_;
    }
    else
    {
      return super.getIfContains(key);
    }
  }

  public boolean contains (Any key)
  {
    if (key.equals(clientProperties_))
      return true;

    return super.contains(key);
  }

  public Any copyFrom (Any a)
  {
    if (!(a instanceof AnyMessage))
      throw new IllegalArgumentException("Not a AnyMessage");
    
    AnyMessage m = (AnyMessage)a;
    
    if (!this.getMessageType().equals(m.getMessageType()))
      throw new IllegalArgumentException("Not the same message type");

    msg_ = m.msg_;
    
    return this;
  }
  
  static public class MessagePropertyMap extends PropertyMap
  {
    
    public MessagePropertyMap(PropertyAccessMap m)
    {
      super(m);
    }
    
    protected PropertyBinding makePropertyBinding(Any key)
    {
      // Establish an arbitrary property in this message and make a
      // binding that can do that.
      return ((AnyMessage)getOuter()).new MessagePropertyBinding(key);
    }
  }
  
  private class MessagePropertyBinding extends AbstractPropertyBinding
  {
    private Any name_;
    
    private MessagePropertyBinding(Any name)
    {
      name_ = name;
    }

    public Any getProperty()
    {
      // When reading the property we have no Any on which to
      // base the typed read, so we use a string, which the JMS
      // docs say is compatible with all types the property could have
      // been written as. It would then be up to script to
      // do the property conversion by assignment, for example:
      //    int i = myMessage.client.myProperty;
      if (propVal_ == null)
        propVal_ = new AnyString();
      return AnyMessage.this.getProperty(name_, propVal_);
    }

    public void setProperty(Any value)
    {
      AnyMessage.this.setProperty(name_, value);
    }

    public void setPropertyInfo(Any info)
    {
      // no-operation
    }
  }

  // In general Visitor classes need to be made reentrant. We will extend the
  // visitXXX methods with Transaction and an additional argument. This additional
  // argument can be a generic.
  private class PropertyConverter extends AbstractVisitor
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
          b.setValue(msg_.getBooleanProperty(name_.toString()));
        else
          msg_.setBooleanProperty(name_.toString(), b.getValue());
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
          b.setValue(msg_.getByteProperty(name_.toString()));
        else
          msg_.setByteProperty(name_.toString(), b.getValue());
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
          d.setTime(msg_.getLongProperty(name_.toString()));
        else
          msg_.setLongProperty(name_.toString(), d.getTime());
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
          d.setValue(msg_.getDoubleProperty(name_.toString()));
        else
          msg_.setDoubleProperty(name_.toString(), d.getValue());
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
          f.setValue(msg_.getFloatProperty(name_.toString()));
        else
          msg_.setFloatProperty(name_.toString(), f.getValue());
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
          i.setValue(msg_.getIntProperty(name_.toString()));
        else
          msg_.setIntProperty(name_.toString(), i.getValue());
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
          l.setValue(msg_.getLongProperty(name_.toString()));
        else
          msg_.setLongProperty(name_.toString(), l.getValue());
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
          o.setValue(msg_.getObjectProperty(name_.toString()));
        else
          msg_.setObjectProperty(name_.toString(), o.getValue());
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
          s.setValue(msg_.getShortProperty(name_.toString()));
        else
          msg_.setShortProperty(name_.toString(), s.getValue());
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
          s.setValue(msg_.getStringProperty(name_.toString()));
        else
          msg_.setStringProperty(name_.toString(), s.getValue());
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
          d.fromString(String.valueOf(msg_.getDoubleProperty(name_.toString())));
        else
          msg_.setDoubleProperty(name_.toString(), d.doubleValue());
      }
      catch(JMSException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }
}
