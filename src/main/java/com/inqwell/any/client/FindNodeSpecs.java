/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.BreadthFirstIter;
import com.inqwell.any.ByteI;
import com.inqwell.any.CharI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.EventGenerator;
import com.inqwell.any.FieldSet;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ObjectI;
import com.inqwell.any.Set;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.Visitor;

/**
 * 
 * @author Tom
 *
 */
public class FindNodeSpecs implements Visitor
{
  private Map res_;
  private Any contextNode_;
  
  public static Map find(Any expression, Transaction t)
  {
    Map res = AbstractComposite.simpleMap();
    FindNodeSpecs f = new FindNodeSpecs(res, null);
    f.setTransaction(t);
    
    if (expression instanceof Locate)
    {
      expression.accept(f);
    }
    else
    {
      // Walk the expression finding all the NodeSpecifications
      // held within the LocateNodes it holds.
      // (I think this is OK even for arrays as the most common
      // use is GUI views checking on sorted values during
      // node event dispatching).
      BreadthFirstIter i = new BreadthFirstIter(expression);

      while (i.hasNext())
      {
        Any a = i.next();
        if (a != null)
          a.accept(f);
      }
    }

    return res;
  }

  FindNodeSpecs(Map res, Any contextNode)
  {
    res_         = res;
    contextNode_ = contextNode;
  }

  public void visitMap (Map m)
  {
  }

  public void visitArray (Array a)
  {
  }

  public void visitSet (Set s)
  {
  }

  public void visitUnknown(Any u)
  {
  }

  public void visitAnyBoolean (BooleanI b)
  {
  }

  public void visitAnyByte (ByteI b)
  {
  }

  public void visitAnyChar (CharI c)
  {
  }

  public void visitAnyInt (IntI i)
  {
  }

  public void visitAnyShort (ShortI s)
  {
  }

  public void visitAnyLong (LongI l)
  {
  }

  public void visitAnyFloat (FloatI f)
  {
  }

  public void visitAnyDouble (DoubleI d)
  {
  }

  public void visitDecimal (Decimal d)
  {
  }

  public void visitAnyString (StringI s)
  {
  }

  public void visitAnyDate (DateI d)
  {
  }

  public void visitFunc (Func f)
  {
    // Check if its a LocateNode and if so, fetch its NodeSpecification.
    if (f instanceof LocateNode)
    {
      LocateNode l = (LocateNode)f;
      NodeSpecification ns = l.getNodePath();
      
      // If it is a path that starts with a special prefix
      // then ignore it since we cannot receive events
      // via these routes.
      Any a = ns.getFirst();
      if (NodeSpecification.prefices__.contains(a))
        return;
        
      ns = ns.resolveIndirections(contextNode_, Globals.process__.getTransaction());

      NodeSpecification eventNs = (NodeSpecification)ns.cloneAny();
      
      // The last element is assumed to be the field we are
      // locating.  The event is raised on the Map parent which
      // will be the end of the NS built up by event propagation.
      // [This is, of course, an assumption.  Revisit if it proves
      // to be an invalid one for significant cases.]
      // Knock off the last element and use instead in fields list
      // for this spec.
      Any field = eventNs.getLast();
      eventNs.removeLast();
      if (!res_.contains(eventNs))
      {
        // If contextNode_ was supplied step down eventNs and if successful
        // set the txn flag of the ultimate map.
        if (contextNode_ != null)
        {
          Iter    i = eventNs.createPathItemsIter();
          Map     n = (Map)contextNode_;
          boolean b = true;
          while (i.hasNext())
          {
            if (n == null)
            {
              b = false;
              break;
            }
            
            Any k = i.next();
            if (!n.contains(k))
            {
              b = false;
              break;
            }
            Any c = n.get(k);
            if (!(c instanceof Map))
            {
              b = false;
              break;
            }
            n = (Map)c;
          }
          // If we were successful in navigating all the way down
          // the path then set the txn flag (since we are binding to
          // a variable).  If the parent node is the context node
          // then ignore, because its not allowed to have variables
          // in the context node.  This might happen when code
          // references $this.TypeDef (i.e.the whole object) and the
          // assumption above about fields is invalid
          if (b &&
              (n != contextNode_) &&
              (n instanceof EventGenerator))
          {
            n.setTransactional(true);
          }
        }
        
        Set fs = AbstractComposite.fieldSet();
        fs.add(field);
        res_.add(eventNs, fs);
      }
      else
      {
        FieldSet fs = (FieldSet)res_.get(eventNs);
        if (!fs.contains(field))
          fs.add(field);
      }
    }
  }

  public void visitAnyObject (ObjectI o)
  {
  }

  public void setTransaction(Transaction t)
  {
    }

  public Transaction getTransaction()
  {
    return Transaction.NULL_TRANSACTION;
  }
}
