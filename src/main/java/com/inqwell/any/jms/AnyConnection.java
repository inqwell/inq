/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.jms;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.DefaultPropertyAccessMap;
import com.inqwell.any.ExceptionHandler;
import com.inqwell.any.Globals;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;

public class AnyConnection extends    DefaultPropertyAccessMap
                           implements ConnectionI
{
  private Connection       conn_;
  private HandleExceptions handler_;
  
  public AnyConnection(Connection conn)
  {
    conn_ = conn;
  }

  @Override
  public void close()
  {
    try
    {
      conn_.close();
    }
    catch(JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public SessionI createSession(boolean transacted, int acknowledgeMode)
  {
    Session s = null;
    try
    {
      s = conn_.createSession(transacted, acknowledgeMode);
      
      return new AnySession(s);
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public Any getClientID()
  {
    try
    {
      return new AnyString(conn_.getClientID());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public Map getMetaData()
  {
    return mapifyMeta();
  }

  @Override
  public void setClientID(Any clientID)
  {
    try
    {
      conn_.setClientID(clientID.toString());
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public void setExceptionHandler(Any handler)
  {
    try
    {
      if (AnyNull.isNull(handler))
      {
        handler_ = null;
        conn_.setExceptionListener(null);
      }
      else
      {
        if (handler_ != null)
          throw new AnyRuntimeException("There is already a handler in place");
        
        if (!(handler instanceof AnyFuncHolder.FuncHolder))
          throw new AnyRuntimeException("Handler must be a function");
        
        AnyFuncHolder.FuncHolder f = (AnyFuncHolder.FuncHolder)handler;
        Process p = Globals.getProcessForCurrentThread();
        Any sync = p.getIfContains(Process.sync__);
        if (sync == null)
          throw new AnyRuntimeException("Not a synced process");
        handler_ = new HandleExceptions(f, p);
        conn_.setExceptionListener(handler_);
      }
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void start()
  {
    try
    {
      conn_.start();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  @Override
  public void stop()
  {
    try
    {
      conn_.stop();
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  protected void finalize() throws Throwable
  {
    try
    {
      conn_.close();
      super.finalize();
    }
    catch (JMSException e)
    {
    }
  }
  
  private Map mapifyMeta()
  {
    try
    {
      ConnectionMetaData m = conn_.getMetaData();
      Map ret = AbstractComposite.simpleMap();
      
      ret.add(JMS_MAJOR_VERSION, new ConstInt(m.getJMSMajorVersion()));
      ret.add(JMS_MINOR_VERSION, new ConstInt(m.getJMSMinorVersion()));
      ret.add(JMS_PROVIDER_NAME, new ConstString(m.getJMSProviderName()));
      ret.add(JMS_VERSION, new ConstInt(m.getJMSVersion()));
      
      Enumeration e = m.getJMSXPropertyNames();
      Set set = AbstractComposite.set();
      while (e.hasMoreElements())
      {
        String s = e.nextElement().toString();
        ConstString cs = new ConstString(s);
        set.add(cs);
      }
      ret.add(JMSX_PROPERTY_NAMES, set);
      
      ret.add(JMS_PROVIDER_MAJOR_VERSION, new ConstInt(m.getProviderMajorVersion()));
      ret.add(JMS_PROVIDER_MINOR_VERSION, new ConstInt(m.getProviderMinorVersion()));
      ret.add(JMS_PROVIDER_VERSION, new ConstInt(m.getProviderVersion()));
      
      return ret;
    }
    catch (JMSException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  static private class HandleExceptions implements ExceptionListener
  {
    AnyFuncHolder.FuncHolder handler_;
    
    // The invoking process needs to be captured
    Process                  process_;
    
    private HandleExceptions(AnyFuncHolder.FuncHolder handler,
                             Process                  process)
    {
      handler_ = handler;
      process_ = process;
    }
    
    public void onException(JMSException exception)
    {
      Any sync = process_.get(Process.sync__);
      synchronized(sync)
      {
        execFunc(handler_, exception);
      }
    }
    
    private void execFunc(AnyFuncHolder.FuncHolder f, JMSException e)
    {
      if (!process_.isAlive())
      {
        System.err.println("JMS exception in a dead hosting process");
        e.printStackTrace();
        return;
      }

      // Probably called from a JMS provider thread
      boolean haveProcessForThread = Globals.haveProcessForThread();
      
      if (!haveProcessForThread)
        Globals.setProcessForThread(Thread.currentThread(), process_);

      CharArrayWriter cw;
      PrintWriter pw = new PrintWriter(cw = new CharArrayWriter());
      e.printStackTrace(pw);

      Map args = AbstractComposite.simpleMap();
      args.add(ExceptionHandler.msgArg__, new AnyString(e.getMessage()));
      args.add(ExceptionHandler.stackTraceArg__, new AnyString(cw.toString()));
      try
      {
        f.doFunc(process_.getTransaction(), args, process_.getRoot());
      }
      catch(Throwable t)
      {
        System.err.println("JMS exception handler incurred exception");
        t.printStackTrace();
      }
      finally
      {
        // Tidy the process
        process_.setContext(null);
        process_.setContextPath(null);
  
        if (!haveProcessForThread)
          Globals.removeProcessForThread(Thread.currentThread(), process_);
      }
    }
  }
}
