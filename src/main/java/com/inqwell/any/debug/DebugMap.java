/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any.debug;

import com.inqwell.any.AbstractAny;
import java.util.*;


public class DebugMap implements Map
{
	private Map m_;

  private DebugMap(Map m)
  {
  	m_ = m;
  }

  static public Map decorate(Map m)
  {
  	if (m instanceof DebugMap)
  	  throw new IllegalArgumentException("Already decorated");

  	return new DebugMap(m);
  }

  public void clear()
  {
  	dumpIt();
  	m_.clear();
  }

  public boolean containsKey(Object key)
  {
  	dumpIt();
  	return m_.containsKey(key);
  }

  public boolean containsValue(Object value)
  {
  	dumpIt();
  	return m_.containsValue(value);
  }

  public Set entrySet()
  {
  	dumpIt();
  	return m_.entrySet();
  }

  public boolean equals(Object o)
  {
  	//dumpIt();
  	return m_.equals(o);
  }

  public Object get(Object key)
  {
  	dumpIt();
  	return m_.get(key);
  }

  public int hashCode()
  {
  	//dumpIt();
  	return m_.hashCode();
  }

  public boolean isEmpty()
  {
  	dumpIt();
  	return m_.isEmpty();
  }

  public Set keySet()
  {
  	dumpIt();
  	return m_.keySet();
  }

  public Object put(Object key, Object value)
  {
  	dumpIt();
  	return m_.put(key, value);
  }

  public void putAll(Map t)
  {
  	dumpIt();
  	m_.putAll(t);
  }

  public Object remove(Object key)
  {
  	dumpIt();
  	return m_.remove(key);
  }

  public int size()
  {
  	dumpIt();
  	return m_.size();
  }

  public Collection values()
  {
  	dumpIt();
  	return m_.values();
  }

  private void dumpIt()
  {
  	System.out.println(Thread.currentThread().getName());
  	AbstractAny.stackTrace();
  }
}
