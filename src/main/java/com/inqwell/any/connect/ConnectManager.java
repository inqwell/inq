/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/sql/SqlManager.java $
 * $Author: sanderst $
 * $Revision: 1.1 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.connect;

import com.inqwell.any.*;
import com.inqwell.any.io.IoConstants;

/*
 * Manages instances of ServerConnect so that they can be allocated
 * to consumer threads who sometime later put them back.
 * ServerConnect instances are not reentrant yet represent the
 * limited resource of connections to an Inq server server.
 * <p>
 * 
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.1 $
 * $Date: 2011-04-07 22:18:23 $
 */
public class ConnectManager extends    AbstractResourceAllocator
                            implements ResourceAllocator

{
  private static ConnectManager theInstance__ = null;
  
  public static ConnectManager instance()
  {
    if (theInstance__ == null)
    {
      synchronized (ConnectManager.class)
      {
        if (theInstance__ == null)
          theInstance__ = new ConnectManager();
      }
    }
    return theInstance__;
  }
  
  public static void addInqServer(Any id, Any spec, IntI limit)
  {
    ConnectManager.instance().addSpec(id, spec, limit);
  }
/*
  protected Any makeNewResource(Any id, Any spec, int made) throws AnyException
  {
    // The id is of the form <package>.<identifier>. Really should
    // be <package>:<ID>. Anyway, 
    Map m = (Map)spec;
    Any user   = m.get(IoConstants.user__);
    Any passwd = m.get(IoConstants.passwd__);
    Any url    = m.get(IoConstants.url__);
    //Any pkg    = m.get(IoConstants.url__);
    
    ServerConnect sc = new ServerConnect (url,
                             user,
                             passwd,
                             initStmts);

    if (m.contains(IoConstants.null__))
      sql.setNullStrings((Map)m.get(IoConstants.null__));
      
    if (m.contains(IoConstants.delim__))
      sql.setDelimitersMap((Map)m.get(IoConstants.delim__));
    
    if (m.contains(IoConstants.dateAsTime__))
      sql.setDateAsTime(((BooleanI)m.get(IoConstants.dateAsTime__)).getValue());
      
    return sql;
  }
  
  protected  void afterAcquire(Any resource)
  {
  }
  
  protected boolean beforeAcquire(Any resource)
  {
    // Ping the connection to see if it is still alive. Well, there's
    // no ping as such, so this might not be reliable. Anyway, if
    // we find the connection is not viable return false
    
    boolean ok = true;
    
    AnySql sql = (AnySql)resource;

    try
    {
       ok = !sql.isClosed();
       
       // Toggle autocommit as our attempt to ping the connection
       if (ok)
       {
         boolean autoCommit = sql.isAutoCommit();
         sql.setAutoCommit(!autoCommit);
         sql.setAutoCommit(autoCommit);
       }
    }
    catch (Exception e)
    {
      ok = false;
    }
    
    return ok;
  }
  
  protected  boolean beforeRelease(Any resource,
                                   Any arg,
                                   ExceptionContainer e)
  {
    boolean   retain = true;
    Exception exc    = null;  // commit exception - connection is OK
    Exception ex     = null;  // some other exception - not OK
    
    AnySql sql = (AnySql)resource;
    try
    {
      sql.cleanUp();

      if (sql.isClosed())
        retain = false;
      else
      {
        if (!sql.isAutoCommit())
        {
          try
          {
            // If the commit fails then passup the exception but leave
            // the connection viable
            if (arg != null)
              sql.rollback();
            else
              sql.commit();
          }
          catch(Exception ee)
          {
            exc = ee;
          }
        }
        sql.setAutoCommit(true);
      }
    }
    catch (Exception ee)
    {
      // Any other exception then release the connection from the pool
      ex = ee;
      retain = false;
    }
    
//    if (e != null)
//      throw e;
    
    // If we have an exception from one or other causes (commit or
    // cleanup) then place it in any exception container. Give
    // priority to the cleanup one.
    if (e != null)
    {
      if (ex != null)
        e.setThrowable(ex);
      else if (exc != null)
        e.setThrowable(exc);
    }
    
    return retain;
  }

  protected void disposeResource(Any resource)
  {
    AnySql sql = (AnySql)resource;
    sql.close();
  }
  */

  @Override
  protected void afterAcquire(Any resource)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected boolean beforeAcquire(Any resource)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected boolean beforeRelease(Any resource, Any arg, ExceptionContainer e)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  protected void disposeResource(Any resource)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected Any makeNewResource(Any id, Any spec, int made) throws AnyException
  {
    // TODO Auto-generated method stub
    return null;
  }
}
