/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MemoryStats.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.ObjectStreamException;

/**
 * A map inmplementation for various figures
 * from <code>java.lang.Runtime</code>.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MemoryStats extends AbstractMap
{
  static public Any freeMemory__          = new ConstString("freeMemory");
  static public Any maxMemory__           = new ConstString("maxMemory");
  static public Any totalMemory__         = new ConstString("totalMemory");
  static public Any availableProcessors__ = new ConstString("availableProcessors");
  static public Any systemTime__          = new ConstString("systemTime");
  
  static private Set keys__;
  
  static
  {
    keys__ = AbstractComposite.set();
    keys__.add(freeMemory__);
    keys__.add(maxMemory__);
    keys__.add(totalMemory__);
    keys__.add(availableProcessors__);
    keys__.add(systemTime__);
  }
  
	public MemoryStats()
	{
	}

  public Any get (Any key)
  {
    if (!keys__.contains(key))
      throw new FieldNotFoundException(key.toString());

    return getWithKey(key);
  }

  public Any getIfContains (Any key)
  {
    return getWithKey(key);
  }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  public boolean isEmpty()
  {
    return false;
  }

  public boolean contains (Any key)
  {
		return keys__.contains(key);
  }

	public int entries()
	{
		return keys__.entries();
	}
	
  public Iter createKeysIterator ()
  {
		return keys__.createIterator();
  }

  public Iter createIterator ()
  {
		return new StatsIter();
  }
  
  public String toString()
  {
    Map m = AbstractComposite.simpleMap();
    Iter i = createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      m.add(k, get(k));
    }
    return m.toString();
  }

	protected Object writeReplace() throws ObjectStreamException
	{
    // If we serialise class instances then the result is a
    // simple map with the current values in it.
    Map m = AbstractComposite.simpleMap();
    Iter i = createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      m.add(k, get(k));
    }
    
    return m;
	}
  
  private Any getWithKey(Any key)
  {
    Runtime r = Runtime.getRuntime();
    
    Any ret = null;
    
    if (key.equals(freeMemory__))
      ret = new ConstLong(r.freeMemory());
    else if (key.equals(maxMemory__))
      ret = new ConstLong(r.maxMemory());
    else if (key.equals(totalMemory__))
      ret = new ConstLong(r.totalMemory());
    else if (key.equals(availableProcessors__))
      ret = new ConstLong(r.availableProcessors());
    else if (key.equals(systemTime__))
      ret = new AnyDate();
    
    return ret;
  }
	
  private class StatsIter extends AbstractIter
  {
    Iter i_ = createKeysIterator();
    
    public boolean hasNext()
    {
      return i_.hasNext();
    }
    
    public Any next()
    {
      return get(i_.next());
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }
}
