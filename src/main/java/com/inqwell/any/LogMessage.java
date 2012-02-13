/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LogMessage.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Log a message
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class LogMessage extends    AbstractFunc
                        implements Cloneable
{
  private Any   logger_;
  private Any   level_;
  private Any   message_;
  private Array parameters_;

  public LogMessage(Any logger, Any level, Any message, Array params)
  {
    logger_     = logger;
    level_      = level;
    message_    = message;
    parameters_ = params;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any logger = EvalExpr.evalFunc(t,
                                   a,
                                   logger_);
    
    if (logger == null)
      nullOperand(logger_);
    
    Any level = EvalExpr.evalFunc(t,
                                  a,
                                  level_);

    if (level == null)
      nullOperand(level_);

    // Check if this logger would log at the given level before
    // taking the trouble to evaluate the message.
    
    LogManager lm = LogManager.getLogManager();
    
    Logger l;
    
    if (logger instanceof Logger)
      l = (Logger)logger;
    else
      l = lm.getLogger(logger.toString());
    
    if (l == null)
      throw new AnyException("No logger named " + logger);
    
    Level logLevel = AnyLogManager.toLevel(level);
    
    if (l.isLoggable(logLevel))
    {
      
      Any message = EvalExpr.evalFunc(t,
                                      a,
                                      message_);
      
      if (message == null)
        nullOperand(message_);
    
      if (parameters_ != null)
        l.log(logLevel, message.toString(), evalParams(parameters_, a, t));
      else
        l.log(logLevel, message.toString());
      
      return message;
    }
    
    return null;
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(logger_);
    a.add(level_);
    a.add(message_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    LogMessage n = (LogMessage)super.clone();
    
    n.logger_     = logger_.cloneAny();
    n.level_      = level_.cloneAny();
    n.message_    = message_.cloneAny();
    n.parameters_ = (Array)AbstractAny.cloneOrNull(parameters_);
        
    return n;
  }

  // Evaluate any log parameters and replace in the given array.
  // Return as a Java array.
  private Object[] evalParams(Array params, Any a, Transaction t) throws AnyException
  {
    int j = params.entries();
    
    Object ret[] = new Object[j];
    
    for (int i = 0; i < j; i++)
    {
      Any param = params.get(i);
      
      Any p = EvalExpr.evalFunc(t,
                                a,
                                param);
      
      // Allow log msg args to be null
//      if (p == null)
//        nullOperand(param);
      
      ret[i] = p;
    }
    
    return ret;
  }
}
