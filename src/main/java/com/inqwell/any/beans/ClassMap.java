/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/ClassMap.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;

/**
 * This class is used by the various Factories for creating
 * and accessing entities based on class hierarchy.  This class
 * does not conform to the <code>Map</code> interface but it
 * is only used in a number of special cases, generally when
 * interfacing the <code>Any</code> framework to external
 * sub-system hierarchies, like Swing.
 * <p>
 * Items are added to the map using their class (or super-class)
 * as the key.  A lookup will later succeed if there is an entry
 * for the given class or its super-class.  Hence, adding an entry
 * for the class <code>javax.swing.AbstractButton</code> means
 * that it can be subsequetly looked up by <code>javax.swing.JButton</code>
 * <p>
 * Searches always start at the leaf of the class hierarchy and
 * proceed upwards towards the root.
 */
public class ClassMap extends    AbstractAny
                      implements Cloneable
{
  private Map map_;
  
  private AnyObject ao_ = new AnyObject();   // avoid object creation so sync.

  public ClassMap()
  {
    map_ = AbstractComposite.map();
  }

  public void add(ObjectI ao, Any a)
  {
		map_.add(ao, a);
  }

  public void add(Class c, Any a)
  {
    ObjectI ao = new AnyObject(c);
		this.add(ao, a);
  }

  public Any get(Object o)
  {
		return this.get(o.getClass());
  }

  public synchronized Any get(Class c)
  {
    ao_.setValue(c);
    Any ret = this.get(ao_);
    ao_.setValue(null);
    return ret;
  }
  
  public Any get(ObjectI ao)
  {
    Any got = null;
    Class c = (Class)ao.getValue();
    if (map_.contains(ao))
    {
			got = map_.get(ao);
		}
		else
		{
      Class[] classes = c.getInterfaces();
      got = doInterfaces(classes);
      if (got != null)
        return got;

			Class superClass = c.getSuperclass();
			while (superClass != null)
			{
				ao.setValue(superClass);

				if (map_.contains(ao))
				{
					got = map_.get(ao);
					break;
				}

        classes = superClass.getInterfaces();
        got = doInterfaces(classes);
        if (got != null)
          return got;

				superClass = superClass.getSuperclass();
			}
		}

    if (got == null && !c.isInterface())
    {
      // We've exhausted the classes, try interfaces
      Class[] classes = c.getInterfaces();

      for (int i = 0; i < classes.length; i++)
      {
        got = get(classes[i]);
        if (got != null)
          return got;
      }
    }
    
    if (got == null && c.isInterface())
    {
      // try interface hierarchy
      Class[] classes = c.getInterfaces();
      return doInterfaces(classes);
    }
    return got;
  }

	public String toString()
	{
		return map_.toString();
	}
  
  public Object clone () throws CloneNotSupportedException
  {
    ClassMap c = (ClassMap)super.clone();
    
    c.map_     = (Map)map_.cloneAny();
    
    return c;
  }
	
	private Any doInterfaces(Class[] classes)
	{
    Any got = null;
    for (int i = 0; i < classes.length; i++)
    {
      got = get(classes[i]);
      if (got != null)
        break;
      
      Class[] supers = classes[i].getInterfaces();
      got = doInterfaces(supers);
      if (got != null)
        break;
    }
    return got;
  }
}

