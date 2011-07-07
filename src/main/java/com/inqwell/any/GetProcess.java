/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetProcess.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return the process with the given id or null if no process can
 * be found.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetProcess extends    AbstractFunc
                        implements Cloneable
{
  
  private Any procId_;
  
  public static Process getProcess(Any procId)
  {
    Process ret = null;

    Map catalog = Catalog.instance().getCatalog();
    
    Map processes = (Map)catalog.getIfContains(Process.PROCESSES);
    if (processes != null)
    {
      AnyInt ai = new AnyInt(procId);

      synchronized(Catalog.class)
      {
        BreadthFirstIter i = new BreadthFirstIter(processes);
        while (i.hasNext())
        {
          Any p = i.next();
          if (p instanceof Process)
          {
            if (p.hashCode() == ai.getValue())
            {
              ret = (Process)p;
              break;
            }
          }
        }
      }
    }
    
    return ret;
  }

  public GetProcess(Any any)
  {
    procId_ = any;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Any procId = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   procId_);
    
    if (procId == null)
      nullOperand(procId_);
    
    return getProcess(procId);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    GetProcess c = (GetProcess)super.clone();
    c.procId_ = procId_.cloneAny();
    return c;
  }
  
}
