/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


/*
 * $Archive: /src/com/inqwell/any/beans/EventSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;  
import com.inqwell.any.*;
import java.lang.reflect.Method;
import java.beans.*;

/**
 * This class when given a java bean will allow easy
 * access to various bean info
 */
public class EventSet extends AbstractAny
{
  private Map    set_;
  private Array  eventTypes_;
  
  public EventSet(Object bean)
  {
    this(bean.getClass());
  }
  
  public EventSet(Class beanClass)
  {
    set_        = AbstractComposite.map();
    eventTypes_ = AbstractComposite.array();
    
    
    try
    {
      BeanInfo bi = Introspector.getBeanInfo(beanClass);
      EventSetDescriptor[] evSet = bi.getEventSetDescriptors();
      for (int i =0; i< evSet.length; i++)
      {
        Method add = evSet[i].getAddListenerMethod();
        Method remove = evSet[i].getRemoveListenerMethod();
        String name = evSet[i].getName();
        
        
        EventSetInfo esi = new EventSetInfo(name, add, remove);
        
        set_.add(new ConstString(name), esi);
        
        eventTypes_.add(new ConstString(name));
      }
			//System.out.println ("Event Names: " + eventTypes_);
    }
    catch (IntrospectionException inX)
    {
			throw new RuntimeContainedException(inX);
    }
  }
  
  public Method getAddMethod(Any eventCategory)
  {
    Method m = null;
    if (!set_.contains(eventCategory))
      return null;
      
		EventSetInfo esi = (EventSetInfo)set_.get(eventCategory);
		m = esi.getAddMethod();
    return m;
  }

  public Method getRemoveMethod(Any eventCategory)
  {
    Method m = null;
		EventSetInfo esi = (EventSetInfo)set_.get(eventCategory);
		m = esi.getRemoveMethod();
    return m;
  }

	public Array getEventTypes()
	{
		return eventTypes_;
	}
	
  private class EventSetInfo extends AbstractAny
  {
    String name_;
    Method add_;
    Method remove_;
    
    public EventSetInfo(String eventName, Method add, Method remove)
    {
      name_ = eventName;
      add_ = add;
      remove_ = remove;
    }
    
    public String getName() { return name_; }
    public Method getAddMethod() { return add_; }
    public Method getRemoveMethod() { return remove_; }
  }
}
