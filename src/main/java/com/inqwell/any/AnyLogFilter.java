/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.inqwell.any.client.AnyComponent;

public class AnyLogFilter extends    DefaultPropertyAccessMap
                          implements Cloneable
{
  // Inq log filter function argument names
  private static Any level__      = AbstractValue.flyweightString("level");
  private static Any loggerName__ = AbstractValue.flyweightString("loggerName");
  private static Any message__    = AbstractValue.flyweightString("message");
  private static Any time__       = AbstractValue.flyweightString("atTime");
  private static Any params__     = AbstractValue.flyweightString("params");
  private static Any seq__        = AbstractValue.flyweightString("seq");
  private static Any sourceUrl__  = AbstractValue.flyweightString("sourceUrl");
  private static Any sourceFunc__ = AbstractValue.flyweightString("sourceFunc");
  private static Any procName__   = AbstractValue.flyweightString("procName");

  private Filter filter_;
  
  /**
   * Construct the default log filter.
   * <p>
   * Creates a filter wrapping a FuncFilter. The FuncFilter can later be
   * configured from Inq script using property access. 
   */
  public AnyLogFilter()
  {
    filter_ = new FuncFilter();
  }
  
  public AnyLogFilter(Filter f)
  {
    filter_ = f;
  }
    
  public Filter getFilter()
  {
    return filter_;
  }
  
  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // See AnyFile.accept also
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (!(a instanceof AnyLogFilter))
        throw new IllegalArgumentException();
      
      AnyLogFilter i = (AnyLogFilter)a;
      this.filter_ = i.filter_;
    }
    return this;
  }
  
  public Object getPropertyOwner(Any property)
  {
    return filter_;
  }

  public Object getPropertyBean()
  {
    return filter_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public boolean equals(Any a)
  {
    return (a instanceof AnyLogFilter) &&
         (((AnyLogFilter)a).filter_.equals(filter_));
  }
  
  /**
   * An implementation of {@link java.util.logging.Filter} that supports
   * a log level and an Inq call statement to determine whether logging
   * will take place.
   * <p>
   * The level is used first to cheaply determine whether logging could take
   * place. If it can (the level is ALL or the log record is at or above
   * this filter's level) the Inq call statement is invoked. The Inq script
   * can do anything it likes however its return value is converted to a boolean
   * to decide whether the record will be logged. By default a Filter's
   * level is <code>Level.SEVERE</code>.
   * <p>
   * This functionality is intended to be used to add arbitrary processing
   * on a log event, such as sending an email. The level (if any) would typically
   * be a more severe value that that of the associated Handler or Logger,
   * so that the Filter handles exceptional events. In such usage the Inq function
   * would always return <code>true</code>, however it may veto normal logging of
   * the record by returning <code>false</code>.
   *
   * @author tom
   */
  static public class FuncFilter implements Filter
  {
    private Call  logCall_;
    
    public FuncFilter() {}
    
    public FuncFilter(Call logCall)
    {
      logCall_ = logCall;
    }
    
    public void setLogFunc(Any filterF)
    {
      Call filter = AbstractFunc.verifyCall(filterF);
      logCall_ = filter;
    }

    public Any getLogFunc()
    {
      return logCall_;
    }

    public boolean isLoggable(LogRecord record)
    {
      return callLogFunc(record);
    }
    
    private boolean callLogFunc(LogRecord record)
    {
      if (logCall_ == null)
        return true;
      
      Any     ret    = null;
      boolean b      = false;
      Map     fnArgs = AbstractComposite.simpleMap();

      fnArgs.add(level__, AnyLogManager.toAny(record.getLevel()));
      fnArgs.add(loggerName__, AbstractValue.flyweightString(record.getLoggerName()));
      fnArgs.add(message__, new ConstString(record.getMessage()));
      fnArgs.add(time__, new ConstDate(record.getMillis()));
      
      Object[] params;
      if ((params = record.getParameters()) != null)
      {
        Array p = AbstractComposite.array();
        for (int i = 0; i < params.length; i++)
          p.add((Any)params[i]);
        
        fnArgs.add(params__, p);
      }

      LongI seq = new ConstLong(record.getSequenceNumber());
      fnArgs.add(seq__, seq);
      
      fnArgs.add(sourceUrl__,  new ConstString(record.getSourceClassName()));
      fnArgs.add(sourceFunc__, new ConstString(record.getSourceMethodName()));

      Process p = Globals.getProcessForCurrentThread();
      Any pName = p.getIfContains(Process.processName__);
      if (pName != null)
        fnArgs.add(procName__, pName);

      Transaction t = p.getTransaction();
      Map context = p.getContext();

      try
      {
        Call logCall = (Call)logCall_.cloneAny();
        
        logCall.setArgs(fnArgs);
        logCall.setTransaction(t);
        ret = logCall.exec(context);
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      
      if (ret != null)
      {
        AnyBoolean ab = new AnyBoolean(ret);
        b = ab.getValue();
      }
      return b;
    }

  }
}
