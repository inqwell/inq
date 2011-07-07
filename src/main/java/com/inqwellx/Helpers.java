/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwellx;

import java.util.Iterator;
import java.util.Map;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.StringI;

public class Helpers
{
  // DB Keys
  static public Any USER     = AbstractValue.flyweightString("user");
  static public Any PASSWORD = AbstractValue.flyweightString("password");
  static public Any URL      = AbstractValue.flyweightString("url");

  static public com.inqwell.any.Map makeInqMap()
  {
    return AbstractComposite.simpleMap();
  }
  
  static public com.inqwell.any.Map convertToInqMap(Map in)
  {
    return convertToInqMap(in, AbstractComposite.simpleMap());
  }

  static public com.inqwell.any.Map convertToInqMap(Map in, com.inqwell.any.Map out)
  {
    AnyString ak = new AnyString();
    
    Iterator i = in.keySet().iterator();
    while (i.hasNext())
    {
      // Key
      Object ok = i.next();
      if (ok == null)
        continue;
      
      ak.setValue(ok.toString());
      
      // Value
      Object ov = in.get(ok);
      
      // Check if the output map already contains the key
      Any av = out.getIfContains(ak);
      if (av == null)
      {
        // No. Put it in. If we've been given null then pass Inq null
        if (ov == null)
          av = AnyString.NULL;
        else
          av = new AnyString(ov.toString());
        
        out.add(ak.cloneAny(), av);
      }
      else
      {
        // Yes. Set the value
        StringI s = (StringI)av;
        s.setValue(ov.toString());
      }
    }
    
    return out;
  }

  /**
   * A convenience method that applies the given key as
   * an {@link com.inqwell.any.Any}, as required for retrieval
   * from an Inq {@link com.inqwell.any.Map}.
   * <p>
   * @param map the map to query
   * @param key the key to be applied
   * @return a {@link java.lang.String} by calling the
   * value's <code>toString()</code> method, or <code>null</code>
   * if the key is not found.
   */
  static public String get(com.inqwell.any.Map map, String key)
  {
    Any k = new ConstString(key);
    Any ret = map.getIfContains(k);
    if (ret != null)
      return ret.toString();
    
    return null;
  }
  
}
