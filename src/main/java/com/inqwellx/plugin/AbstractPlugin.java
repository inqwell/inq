/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwellx/plugin/AbstractPlugin.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwellx.plugin;

import java.util.Iterator;
import java.util.Map;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Call;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.Globals;
import com.inqwell.any.Plugin;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;

/**
 * An implementation of {@link com.inqwell.any.Plugin} that provides
 * support for calling back to Inq scripted functions.
 * @author Tom
 *
 */
public abstract class AbstractPlugin extends    DefaultPropertyAccessMap
                                     implements Plugin
{
  private Any     context_;
  private Any     contextPath_;
  private Process process_;
  
  // For use when in debug mode (default ctor)
  private int counter_ = 1;

  private Any createF_;
  private Any modifyF_;
  private Any deleteF_;
  private Any beginF_;
  private Any endF_;

  // The (java) Map received at the plugin interface methods
  // is processed into this (inq) Map and passed to the inq
  // function create/modify
  private com.inqwell.any.Map item_;
  
  private BooleanI b_ = new AnyBoolean();
  
  // The arguments to the inq function. There is only one
  // argument and it is called "item".
  private com.inqwell.any.Map fArgs_ = AbstractComposite.simpleMap();

  // A working variable for a reusable key into the Inq map
  // we pass to the Inq functions. All 
  private StringI k_ = new AnyString();
  
  private boolean txnOpen_;
  private boolean abort_;
  
  static private Any item__ = AbstractValue.flyweightString("item");
  
  protected AbstractPlugin(Any process, Any createF, Any modifyF)
  {
    if (process != null)
    {
      process_     = (Process)process;
      context_     = process_.getRoot();
      contextPath_ = process_.getContextPath();
      createF_     = createF;
      modifyF_     = modifyF;
    }
  }

  protected AbstractPlugin(Any process)
  {
    if (process != null)
    {
      process_     = (Process)process;
      context_     = process_.getRoot();
      contextPath_ = process_.getContextPath();
    }
  }

  protected AbstractPlugin()
  {
  }

  final public void begin()
  {
    if (process_ == null)
    {
      System.out.println("[DEBUG] begin() called");
      return;
    }
    
    // This will not stop multiple threads in the external environment
    // trying to open a transaction however the second and subsequent
    // ones will fail. 
    
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        doBegin();
      }
    }
    else
      doBegin();
  }
  
  private void doBegin()
  {
    if (!process_.isAlive())
      throw new AnyRuntimeException("Hosting Inq process is dead!");
    
    if (txnOpen_)
    {
      abort_ = true;
      
      throw new AnyRuntimeException("Transaction already open");
    }
    
    if (beginF_ != null)
      callInq(beginF_, null, false);
    
    txnOpen_ = true;
  }
  
  final public String create(Map m)
  {
    if (process_ == null)
    {
      System.out.println("[DEBUG] create() called m:" + m);
      return "" + counter_++;
    }
    
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        return callCreate(m).toString();
      }
    }
    else
      return callCreate(m).toString();
  }

  final public void modify(Map m)
  {
    if (process_ == null)
    {
      System.out.println("[DEBUG] modify() called m: " + m);
      return;
    }
    
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        callModify(m);
      }
    }
    else
      callModify(m);
  }
  
  final public boolean delete(Map m)
  {
  	boolean b;
  	
    if (process_ == null)
    {
      System.out.println("[DEBUG] delete() called m: " + m);
      return true;
    }
    
    if (deleteF_ == null)
      throw new AnyRuntimeException("delete not supported by " + getClass().toString());
    
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        b_.copyFrom(callInq(deleteF_, m, false));
        b = b_.getValue();
      }
    }
    else
    {
      b_.copyFrom(callInq(deleteF_, m, false));
      b = b_.getValue();
    }
    
    return b;
  }
  
  final public void end(boolean commit)
  {
    if (process_ == null)
    {
      System.out.println("[DEBUG] end() called commit:" + commit);
      return;
    }
    
    Any sync = process_.getIfContains(Process.sync__);
    if (sync != null)
    {
      synchronized(sync)
      {
        doEnd(commit);
      }
    }
    else
      doEnd(commit);
  }

  private void doEnd(boolean commit)
  {
    if (txnOpen_)
    {
      if (!abort_)
        abort_ = !commit;
      
      if (endF_ != null)
        callInq(endF_, null, true);
    }
  }

  // Property access
  public void setCreate(Any f)
  {
    AbstractFunc.verifyCall(f);
    createF_ = f;
  }

  public void setModify(Any f)
  {
    AbstractFunc.verifyCall(f);
    modifyF_ = f;
  }

  public void setDelete(Any f)
  {
    AbstractFunc.verifyCall(f);
    deleteF_ = f;
  }

  public void setBegin(Any f)
  {
    AbstractFunc.verifyCall(f);
    beginF_ = f;
  }

  public void setEnd(Any f)
  {
    AbstractFunc.verifyCall(f);
    endF_ = f;
  }

  final protected Any callCreate(Map item)
  {
    return callInq(createF_, item, false);
  }
  
  final protected void callModify(Map item)
  {
    callInq(modifyF_, item, false);
  }
  
  private Any callInq(Any func, Map item, boolean closeTxn)
  {
    if (!process_.isAlive())
    {
      abort_ = true;
      throw new AnyRuntimeException("Hosting Inq process is dead!");
    }
    
    // Must be call statement or we throw
    Call c = AbstractFunc.verifyCall(func);
    
    if (item_ == null)
    {
      item_ = AbstractComposite.simpleMap();
      fArgs_.replaceItem(item__, item_);
    } 
    
    if (item != null)
    {
      item_.empty();   // if we want to be safe
      
      Iterator i = item.keySet().iterator();
      while (i.hasNext())
      {
        // Key
        Object k = i.next();
        k_.setValue(k.toString());
        
        // Value
        Object v = item.get(k);
        Any av = item_.getIfContains(k_);
        if (av == null)
        {
          if (v == null)
            av = AnyString.NULL;
          else
            av = new AnyString(v.toString());
          
          item_.add(k_.cloneAny(), av);
        }
        else
        {
          StringI s = (StringI)av;
          s.setValue(v.toString());
        }
      }
    }
    
    Any ret = null;
    Any sync = process_.getIfContains(Process.sync__);
    com.inqwell.any.Map itemArg = (item != null) ? item_ : null;
    if (sync != null)
    {
      synchronized(sync)
      {
        ret = execFunc(c, itemArg, closeTxn);
      }
    }
    else
      ret = execFunc(c, itemArg, closeTxn);
    
    return ret;
  }
  
  /**
   * A convenience method that applies the given key as
   * an {@link com.inqwell.any.Any}, as required for retrieval
   * from an Inq {@link com.inqwell.any.Map}.
   * <p>
   * <bold>Note:</bold> This method is not thread-safe
   * with respect to itself or any other method in this class.
   * @param map the map to query
   * @param key the key to be applied
   * @return a {@link java.lang.String} by calling the
   * value's <code>toString()</code> method, or <code>null</code>
   * if the key is not found.
   */
  protected String get(com.inqwell.any.Map map, String key)
  {
    k_.setValue(key);
    Any ret = map.getIfContains(k_);
    if (ret != null)
      return ret.toString();
    
    return null;
  }
  
  private Any execFunc(Call c, com.inqwell.any.Map itemArg, boolean closeTxn)
  {
    boolean haveProcessForThread = Globals.haveProcessForThread();
    
    if (!haveProcessForThread)
      Globals.setProcessForThread(Thread.currentThread(), process_);
    
    // Call the function
    
    // Fetch its arguments. Leave anything that might be there
    // already replacing only the "item" argument. If there are
    // no arguments defined then force the "item" argument
    com.inqwell.any.Map args = c.getArgs();
    if (args == null)
      args = fArgs_;
    else
      args.replaceItem(item__, itemArg);
    
    // We  only want to throw runtime exceptions however regretably
    // at the moment Inq defines a checked one. Insulate the caller
    // from this
    Any ret = null;
    Transaction t = process_.getTransaction();

    try
    {
      // Establish the process environment and pass the call
      // statement our transaction

      process_.setContext((com.inqwell.any.Map)context_);
      process_.setContextPath(contextPath_);
      c.setTransaction(t);
      
      // Execute the call statement
      ret = c.execFunc(context_);
    }
    catch(AnyException e)
    {
      // Ordinary exceptions from the Any framework
      // (includes ContainedException)
      // (wrapped in a ContainedException)
      e.fillInCallStack(t);
      t.getCallStack().empty();
      abort_ = true;
      throw new RuntimeContainedException(e);
    }
    catch (AnyRuntimeException e)
    {
      // Runtime exceptions from the Any framework
      //e.printStackTrace();
      e.fillInCallStack(t);
      t.getCallStack().empty();
      abort_ = true;
      throw e;
    }
    catch (Exception e)
    {
      // Handle uncaught JDK exceptions
      AnyRuntimeException ce = new RuntimeContainedException(e);
      ce.fillInCallStack(t);
      t.getCallStack().empty();
      abort_ = true;
      throw ce;
    }
    catch (StackOverflowError e)
    {
      AnyRuntimeException ce = new RuntimeContainedException(e);
      ce.topOfStack(t);
      t.getCallStack().empty();
      abort_ = true;
      throw ce;
    }
    catch (Error e)
    {
      // Serious errors, will cause thread to terminate
      AnyRuntimeException ce = new RuntimeContainedException(e);
      abort_ = true;
      throw ce;
    }
    finally
    {
      // Tidy the Call statement's arguments
      args.remove(item__);
      
      // Tidy the Call statement's transaction
      c.setTransaction(Transaction.NULL_TRANSACTION);
      
      try
      {
        if (!txnOpen_ || abort_ || closeTxn)
          closeTxn(t, abort_);
      }
      finally
      {
        if (closeTxn)
          txnOpen_ = false;
        // Tidy the process
        process_.setContext(null);
        process_.setContextPath(null);
  
        if (!haveProcessForThread)
          Globals.removeProcessForThread(Thread.currentThread(), process_);
      }      
    }
    
    return ret;
  }

  private void closeTxn(Transaction t, boolean abort)
  {
    abort_ = false;
    try
    {
      t.getCallStack().empty();
      if (abort)
      {
        // Unwind any transaction that may be in progress
        t.abort();
        process_.setContext(null);
        process_.setContextPath(null);
      }
      else
      {
        // All OK - commit transaction
        t.commit();
        process_.setContext(null);
        process_.setContextPath(null);
      }
    }
    catch(Exception e)
    {
      RuntimeContainedException ce = new RuntimeContainedException(e);
      ce.fillInCallStack(t);
      try { t.abort(); } catch(Exception ee) {ee.printStackTrace();}
      process_.setContext(null);
      process_.setContextPath(null);
      throw(ce);
    }
    finally
    {
      Any sync = process_.getIfContains(Process.sync__);
      if (sync != null)
      {
        synchronized(sync)
        {
          sync.notifyAll();
        }
      }
    }
  }
}
