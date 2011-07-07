/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractMap.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.identity.AnyMapDecor;

public abstract class AbstractMap extends    AbstractComposite
                                  implements Map
{
  static Any P_READ;
  static Any P_WRITE;
  static Any P_ADD;
  static Any P_REMOVE;
  
  static Map defaultPrivileges__;
  
  static
  {
    P_READ   = new ConstShort(Map.P_READ);
    P_WRITE  = new ConstShort(Map.P_WRITE);
    P_ADD    = new ConstShort(Map.P_ADD);
    P_REMOVE = new ConstShort(Map.P_REMOVE);

    // May be we should use MAXIMUM_PRIVILEGE by default for
    // write/add/remove?  See also getPrivilegeLevelImpl below.
    Any s = new ConstShort(Process.MINIMUM_PRIVILEGE);
    defaultPrivileges__ = AbstractComposite.simpleMap();
    defaultPrivileges__.add(P_READ, s);
    defaultPrivileges__.add(P_WRITE, s);
    defaultPrivileges__.add(P_ADD, s);
    defaultPrivileges__.add(P_REMOVE, s);
  }
  
  private boolean isTransactional_ = false;
  private Any     uniqueKey_       = null;
  private Map     privileges_      = null;
  
  public void add (Any key, Any value)
  {
		throw new UnsupportedOperationException();
  }

  public void add (StringI keyAndValue)
  {
		throw new UnsupportedOperationException();
  }

  public Any remove (Any key)
  {
		throw new UnsupportedOperationException();
  }

  public void replaceItem (Any key, Any item)
  {
		throw new UnsupportedOperationException();
  }

  public void replaceValue (Any key, Any value)
  {
		throw new UnsupportedOperationException();
  }

  public Any get (Any key)
  {
		throw new UnsupportedOperationException();
  }

  public Any getIfContains(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

  public java.util.Map getMap ()
  {
		throw new UnsupportedOperationException();
  }

  public boolean contains (Any key)
  {
		throw new UnsupportedOperationException();
  }

  public boolean containsValue (Any value)
  {
		throw new UnsupportedOperationException();
  }

  public boolean hasKeys (Array keys)
  {
		throw new UnsupportedOperationException();
  }
  
  public Array keys ()
  {
		throw new UnsupportedOperationException();
  }
  
  public Any getMapKey(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public Map shallowCopy()
  {
		throw new UnsupportedOperationException();
	}

  public Composite shallowCopyOf()
  {
    return shallowCopy();
  }

  public Iter createKeysIterator ()
  {
		throw new UnsupportedOperationException();
  }
  
  public Iter createConcurrentSafeKeysIterator()
  {
    throw new UnsupportedOperationException();
  }
  
  public Descriptor getDescriptor()
  {
		return Descriptor.degenerateDescriptor__;
  }

  public void setDescriptor(Descriptor d)
  {
		throw new UnsupportedOperationException();
  }
  
  public Any getUniqueKey()
  {
//    if (uniqueKey_ == null)
//      throw new AnyRuntimeException("Unique key is not initialised");
    
    return uniqueKey_;
  }
  
  public void setUniqueKey(Any keyVal)
  {
		uniqueKey_ = keyVal;
  }

  public void setTransactional(boolean isTransactional)
  {
		isTransactional_ = isTransactional;
  }
  
  public boolean isTransactional()
  {
		return isTransactional_;
  }
  
  public void setContext(Any context)
  {
		throw new UnsupportedOperationException();
	}
	
	public int entries()
	{
		throw new UnsupportedOperationException();
	}
	
  public Map bestowIdentity()
	{
		return new AnyMapDecor (this);
	}

  public void setAux(Any aux)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getAux()
  {
    throw new UnsupportedOperationException();
  }
  
// If we have transactional maps at two levels (like client vars, vars.filter)
// then this causes problems passing vars.filter as a call arg. The txn status
// of vars causes the map to become const...
//  public Any bestowConstness()
//  {
//    return new AnyMapConstDecor(this);
//  }

  public void setPropertyBean(Object bean)
  {
    throw new UnsupportedOperationException();
  }
  
  public Object getPropertyBean()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Get the privilege level that is the minimum required for
   * the specified access to the child given by the key.
   * <p>
   * The default implementation applied one privilege level
   * for all children of <code>this</code>.
   * @return The privilege level
   */
  public short getPrivilegeLevel(Any access, Any key)
  {
    return getPrivilegeLevelImpl(access, key);
  }

  protected short getPrivilegeLevelImpl(Any access, Any key)
  {
    Map privileges = (privileges_ == null) ? defaultPrivileges__
                                           : privileges_;
    
    short ret = Process.MINIMUM_PRIVILEGE;
    
    if (privileges.contains(access))
    {
      ShortI s = (ShortI)privileges.get(access);
      ret = s.getValue();
    }
    
    return ret;      
  }
  
  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    // Tidy the map and make it exclusive to us in the process.
    
    Map m = AbstractComposite.simpleMap();
    
    Iter i = defaultPrivileges__.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      if (levels.contains(k))
      {
        m.add(k, levels.get(k).cloneAny());
      }
      else
      {
        if (privileges_ != null && merge)
        {
          if (privileges_.contains(k))
            m.add(k, privileges_.get(k));
        }
      }
    }
    
    privileges_ = m;
  }
  
  /**
   * Determine whether this node has privilege information.
   * 
   * @return <code>true</code> if this node has privilege information,
   * <code>false</code> if it doesn't.
   */
  protected boolean definesPrivileges()
  {
    return privileges_ != null;
  }
  
	protected abstract boolean beforeAdd(Any key, Any value);
	protected abstract void afterAdd(Any key, Any value);
	protected abstract void beforeRemove(Any key);
	protected abstract void afterRemove(Any key, Any value);
	protected abstract void emptying();
	
	protected void handleNotExist(Any key)
	{
    if (!contains(key))
      throw new FieldNotFoundException ("Key: " + key);
	}

  public Object clone() throws CloneNotSupportedException
  {
    AbstractMap m = (AbstractMap)super.clone();
    
    //m.isTransactional_ = false;
    //m.uniqueKey_       = null;
    
    return m;
  }
}
