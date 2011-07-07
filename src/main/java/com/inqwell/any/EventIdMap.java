/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventIdMap.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * 
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class EventIdMap extends    SimpleMap
												implements Map,
												Cloneable
{
	public static Map makeNodeEventType(Any        baseType,
																			Any        descriptor,
																			Any        path,
																			Any        fields)
	{
		Map m = AbstractComposite.eventIdMap();
		m.add(EventConstants.EVENT_TYPE, baseType);
		m.add(Descriptor.descriptor__, descriptor);
		if (fields != null)
			m.add(EventConstants.EVENT_FIELDS, fields);
		if (path != null)
			m.add(EventConstants.EVENT_PATH, path);
	  if (baseType.equals(EventConstants.NODE_REMOVED) ||
        baseType.equals(EventConstants.NODE_ADDED) ||
        baseType.equals(EventConstants.NODE_REPLACED))
    {
      m.add(EventConstants.EVENT_VECTOR, AnyAlwaysEquals.instance());
      //m.add(EventConstants.EVENT_PARENT, AnyAlwaysEquals.instance());
    }
			
		return m;
	}
	
	public static Map makeNodeEventType(Any        baseType,
																			Any        descriptor,
																			Any        path)
	{
		return EventIdMap.makeNodeEventType(baseType, descriptor, path, null);
	}
	
	public static Map makeNodeEventType(Any        baseType,
																			Any        descriptor)
	{
		return EventIdMap.makeNodeEventType(baseType, descriptor, null, null);
	}
	
  public Object clone() throws CloneNotSupportedException
  {
		return super.clone();
	}
	
	public int hashCode()
	{
		return 5;
	}
}
