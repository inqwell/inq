/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:15:58 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

public abstract class AnySortView extends AnyComponent
{
  public static AnyComparator makeComparator(Any paths, Any contextNode)
  {
    Array   p     = null;
    boolean croak = true;
    OrderBy ob    = null;

    if (paths instanceof AnyFuncHolder.FuncHolder)
    {
      AnyFuncHolder.FuncHolder fh = (AnyFuncHolder.FuncHolder)paths;
      Func f = fh.getFunc();
      if (f instanceof OrderBy)
      {
        ob = (OrderBy)f;
        p = ob.getOrderBy();
        croak = false;
      }
    }

    if (paths instanceof Array)
    {
      p = (Array)paths;
      p = p.shallowCopy();
      croak = false;
    }

    if (croak)
      throw new AnyRuntimeException("modelSort property expects Array or Sort");

    for (int i = 0; i < p.entries(); i++)
    {
      Any a = p.get(i);
      if (a instanceof NodeSpecification)
      {
        NodeSpecification n = (NodeSpecification)a;
        n = n.resolveIndirections(contextNode, Globals.process__.getTransaction());
        p.replaceItem(i, new LocateNode(n));
      }
      else if (a instanceof Locate)
      {
        Locate l = (Locate)a;
        NodeSpecification n = l.getNodePath();
        n = n.resolveIndirections(contextNode, Globals.process__.getTransaction());
        l.setNodePath(n);
      }
      else if (a instanceof MakePath)
      {
        // Really, MakePath now replaces NodeSpecification because
        // of parser changes...
        MakePath m = (MakePath)a.cloneAny();
        m.setTransaction(Globals.process__.getTransaction());
        try
        {
          NodeSpecification n = (NodeSpecification)m.exec(contextNode);
          p.replaceItem(i, new LocateNode(n));
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    }
    
    AnyComparator ac;
    if (ob != null)
    {
      try
      {
        ac = (AnyComparator)ob.makeComparator(contextNode, null, Globals.process__.getTransaction());
        ac.setOrderBy(p);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    else
      ac = new AnyComparator(p);
    
    return ac;
  }

  public void setModelSort(Any paths)
  {
    if (!AnyNull.isNullInstance(paths))
    {
      AnyComparator ac = makeComparator(paths, getContextNode());
      applySort(ac);
    }
    else
      applySort(null);
  }

  protected abstract void applySort(AnyComparator c);
}
