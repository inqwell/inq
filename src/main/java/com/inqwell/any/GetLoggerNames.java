/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.util.Enumeration;
import java.util.logging.LogManager;

/**
 * Return a list of logger names available
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetLoggerNames extends    AbstractFunc
                            implements Cloneable
{
  public GetLoggerNames()
  {
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    LogManager lm = LogManager.getLogManager();
    
    Array ret = null;
    
    Enumeration e = lm.getLoggerNames();
    if (e != null)
    {
      ret = AbstractComposite.array();
      
      while (e.hasMoreElements())
      {
        Any n = new AnyString(e.nextElement().toString());
        ret.add(n);
      }
    }
        
    return ret;
  }
  
  public Iter createIterator ()
  {
    return DegenerateIter.i__;
  }

  public Object clone () throws CloneNotSupportedException
  {
    GetLoggerNames g = (GetLoggerNames)super.clone();
    
    return g;
  }
}
