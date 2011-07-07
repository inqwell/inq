/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/sql/SqlManager.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.io.sql;

import com.inqwell.any.*;
import com.inqwell.any.io.IoConstants;

/*
 * Manages instances of AnySql so that they can be allocated to consumer
 * threads who sometime later put them back.  AnySql instances are not
 * reentrant yet represent the limited resource of connections to an SQL
 * server.
 * <p>
 * 
 * $Archive: /src/com/inqwell/any/io/sql/SqlManager.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:23 $
 */
public class SqlManager extends    AbstractResourceAllocator
												implements ResourceAllocator
{
	private static SqlManager theInstance__ = null;
	
	public static SqlManager instance()
	{
		if (theInstance__ == null)
		{
			synchronized (SqlManager.class)
			{
				if (theInstance__ == null)
					theInstance__ = new SqlManager();
			}
		}
		return theInstance__;
	}
	
	public static void addSqlServer(Any id, Any spec, IntI limit)
	{
		//System.out.println ("inside addSqlServer " + limit + " " + id + " " + spec);
		SqlManager.instance().addSpec(id, spec, limit);
		//System.out.println ("leaving addSqlServer");
	}

	protected Any makeNewResource(Any id, Any spec, int made) throws AnyException
	{
		// SQL Servers are keyed on a Map comprising the user/passwd/JDBC-URL
    //System.out.println(id + " " + spec);
    
		Map m = (Map)spec;
		Any user   = m.get(IoConstants.user__);
		Any passwd = m.get(IoConstants.passwd__);
		Any url    = m.get(IoConstants.url__);
		
		Array initStmts = (Array)m.getIfContains(IoConstants.initStmts__);
    
		AnySql sql = new AnySql (url.toString(),
														 user.toString(),
														 passwd.toString(),
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
}
