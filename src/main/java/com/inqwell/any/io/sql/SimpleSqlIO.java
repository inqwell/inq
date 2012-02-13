/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/io/sql/SimpleSqlIO.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-05-02 20:33:12 $
 */

package com.inqwell.any.io.sql;

import java.io.InputStream;
import java.io.OutputStream;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyMessageFormat;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Catalog;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.ExceptionContainer;
import com.inqwell.any.IntI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.IoConstants;
import com.inqwell.any.io.PhysicalIO;
import com.inqwell.any.io.UnexpectedCardinalityException;

/**
 * Perform i/o to an sql source.
 * <p>
 * Instances of this class to not represent database connections
 * directly, instead they acquire the required connection from
 * the environment's <code>SqlManager</code>, which has been separately
 * configured.
 * <p>
 * This class is thread-safe.
 */
public class SimpleSqlIO extends    AbstractAny
												 implements PhysicalIO,
                                    Cloneable
{
	private static Any READ_SQL    = new ConstString("read-sql");
	private static Any WRITE_SQL   = new ConstString("write-sql");
	private static Any DELETE_SQL  = new ConstString("delete-sql");
	private static Any READ_ORDER  = new ConstString("read-order");
	private static Any WRITE_ORDER = new ConstString("write-order");
	private static Any RESULT_MAP  = new ConstString("result-map");
	private static Any PREPARED    = new ConstString("prepared");
  private static Any STPROC      = new ConstString("stored-proc");
	
  private Any readSql_;     // skeletal retrieval SQL statement
  private Any writeSql_;    // skeletal update/insert SQL statement
  private Any deleteSql_;   // skeletal delete SQL statement
  
  // Note that write and delete can be null if this is intended to be
  // for read-only use
  
	// Result set column map.  There doesn't have to be one of these but
	// if there is it will be used to map SQL result column names into
	// the output proto keys.  Otherwise the default name map from the
	// result set is used which must therefore be satisfied by the output
	// proto.  If a map is supplied it offers independence between the
	// result set and the output proto at the expense of not trapping
	// uninitialised fields.  Read only as may be shared with other
	// instances for the same output proto.  Order not significant.
  private Map       resultMap_;
  
  // An array which may be used if the readSQL statement requires
  // ordered input parameters.  This is the case if the statement is a
  // stored procedure.  Contains the keys of the supplied map.
  private Array     readOrder_;
  
  // Bit like readOrder_ but didn't want to make this compulsory
  // when the key fields can already define the order (so is readOrder_
  // still of any real use?  Yes!! Now we have the keyFields_ we
  // might need to repeat them in prepared statements) when using
  // prepared statements
  private Array     keyFields_;
  
  // Similar to the above but if we are using prepared statements
  // we need to know the order for writing all the fields
  private Array     fieldOrder_;
  
  // As above except for writing.
  private Array     writeOrder_;
  
  // Contains the sql server identity that should
  // be used for this instance.
  private Any       sqlLogin_;
  
  private ExceptionContainer e_ = new RuntimeContainedException(null);
  
	// Internal.  The resultMap is 'compiled' into this index map on
	// first use.
  private Map       indexMap_;
  
  // If true then cardinality will be tested when writing objects.
  // Set to false if the underlying SQL engine returns a rowcount
  // of two when writing using statements like 'replace' or 'merge'.
  private BooleanI cardinality_ = new AnyBoolean(true);
  
  // Whether to use a prepared statement or a stored procedure call.
  // If both are false then a simple sql string parameterised by
  // a formatter will be used.  Otherwise one of st proc or a prepared
  // statement is used.  If both are true (?) then st proc is assumed.
  private boolean prepared_ = false;
  private boolean stproc_   = false;

	/** 
	 * No args constructor
	 */
  public SimpleSqlIO ()
  {
  }

	/**
	 * No operation.
	 */
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		return true;
	}

  public int read (Map   ioKey,
                   Map   outputProto,
                   Array outputComposite,
                   int   maxCount) throws AnyException
  {
    boolean retry     = false;
    boolean statement = false;
    int     rowCount = 0;
    do
    {
      retry     = false;
      statement = false;
      AnySql sql = acquireSql(sqlLogin_, null);
      try
      {
        doSql(sql, ioKey, maxCount);

        // We've done the statement, so set the flag. Our exception
        // handling is slightly different if it occurs during the
        // result set phase.
        statement = true;
        Map result = (Map)outputProto.cloneAny();
  
        while (sql.getNextRow(result) && (maxCount == 0 || rowCount <= maxCount))
        {
          outputComposite.add (result.cloneAny());
          rowCount++;
        }
        
        if (maxCount == 0)
          rowCount = sql.rowCount();
        
        if (sql.isDebugEnabled())
          System.out.println ("Rows read: " + rowCount);
      }
      
      catch (ContainedException ce)
      {
        // Assume its an SQL cause (!)
        ce.printStackTrace();
        
        if (statement)
        {
          // We've done the statement so the error is to do with
          // result set handling. May be some sort of truncation
          // occurred or a column value could not be converted
          // to the Java type by jdbc. Just throw the exception
          // in this case.
          throw ce;
        }
        else
        {
          // If the connection we are using has worked before then don't
          // bother to retry. By closing the connection we ensure it
          // doesn't go back into the pool when released.
          // The result of this is that if (say) the sql server has
          // gone down or been rebooted we will run through all the
          // available connections and close them. We'll then make a
          // new one and if that fails we eventually give up (because
          // it hasn't done any statements successfully).
          retry = (sql.getStatementsDone() > 0);
          sql.close();
          if (!retry)
            throw ce;
        }
      }
      catch (AnyException e)
      {
        // Some other cause.  May not be necessary to close the
        // SQL connection?
        sql.close();
        throw e;
      }
      
      finally
      {
        e_.setThrowable(null);
        SqlManager.instance().release(sqlLogin_, sql, null, e_);
      }
    }
    while(retry);
    
    if (e_.getThrowable() != null)
      throw new RuntimeContainedException(e_.getThrowable());
    
		return rowCount;
  }

  public Any read () throws AnyException
  {
    throw new UnsupportedOperationException (getClass() + " read()");
	}
	
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
		//System.out.println ("SimpleSqlIO.read " + sqlLogin_);
    boolean retry           = false;
    boolean statement       = false;
    boolean badCardinality  = false;
    Map     result          = null;
    do
    {
      retry     = false;
      statement = false;
      AnySql sql = acquireSql(sqlLogin_, null);
        
      try
      {
        doSql(sql, ioKey, 0);
        
        statement = true;
  
        result = (Map)outputProto.cloneAny();
        if (!sql.getNextRow(result))
          result = null;
  
  //			System.out.println ("SimpleSqlIO.read result is " + result);
        badCardinality = sql.nextRow(); // && cardinality_.getValue();
      }

      catch (ContainedException ce)
      {
        // Assume its an SQL cause (!)
        
        if (statement)
          throw ce;
        else
        {
          retry = (sql.getStatementsDone() > 0);
          sql.close();
          if (!retry)
            throw ce;
        }
      }
      catch (AnyException e)
      {
        sql.close();
        throw e;
      }
      
      finally
      {
        e_.setThrowable(null);
        SqlManager.instance().release(sqlLogin_, sql, null, e_);
      }
    }
    while(retry);
		
		if (badCardinality)
			throw new UnexpectedCardinalityException(getClass() + "read()");
			
    if (e_.getThrowable() != null)
      throw new RuntimeContainedException(e_.getThrowable());

    return result;
	}

	public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
	{
		return write(outputItem, t);
	}
	
  public void flush()
  {
  }
  
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException("writeln " + getClass());
	}
	
  public boolean write (Any outputItem, Transaction t) throws AnyException
  {
		Map m = (Map)outputItem;
		boolean written = false;
		
		if (writeSql_ != null)
		{
			int updated       = 0;
      boolean retry     = false;
      boolean statement = false;
      do
      {
        retry     = false;
        statement = false;
        
        AnySql sql = acquireSql(sqlLogin_, t);
        
        boolean badCardinality = false;
        
        try
        {
          // In the SQL implementation we don't require a separate key as
          // we assume that the key and non-key fields are held in the
          // output item.
          //sql.executeSql (writeSql_.toString(), m, writeOrder_);
          if (prepared_ || stproc_)
          {
            if (stproc_)
            {
              sql.executeStoredProc(writeSql_.toString(),
                                    m,
                                    writeOrder_ == null ? fieldOrder_
                                                        : writeOrder_);
            }
            else
            {
              sql.executePrepared(writeSql_.toString(),
                                  m,
                                  writeOrder_ == null ? fieldOrder_
                                                      : writeOrder_);
            }
          }
          else
          {
            sql.executeSql (writeSql_.toString(),
                            m,
                            writeOrder_ == null ? fieldOrder_
                                                : writeOrder_);
          }
          statement = true;
          do;
          while (sql.resultsAvailable() != AnySql.ResUpdateCount);
        
          updated = sql.getUpdateCount();
          if (updated > 1)
            badCardinality = true && cardinality_.getValue();
        
          if ((updated == 1) || (updated > 1 && !cardinality_.getValue()))
            written = true;
        }
        catch (ContainedException ce)
        {
          if (statement)
          {
            if (!t.isAutoCommit())
              sql.setDueRollback();
            
            throw ce;
          }
          else
          {
            retry = t.isAutoCommit() && (sql.getStatementsDone() > 0);
            sql.close();
            if (!retry)
              throw ce;
          }
        }
      
        catch (AnyException e)
        {
          sql.close();
          throw e;
        }
      
        finally
        {
          if (t == null || t.isAutoCommit())
          {
            e_.setThrowable(null);
            SqlManager.instance().release(sqlLogin_, sql, null, e_);
          }
        }
        
        if (badCardinality)
          throw (new UnexpectedCardinalityException(getClass() + "write()"));
        
        if (e_.getThrowable() != null)
          throw new RuntimeContainedException(e_.getThrowable());
      }
      while(retry);
		}

		return written;
	}
	
  // Just for reading
	private void doSql(AnySql sql, Map ioKey, int maxCount) throws AnyException
	{
		//System.out.println ("SimpleSqlIO.read() " + ioKey + " " + readOrder_);
    
    if (prepared_ || stproc_)
    {
      if (stproc_)
      {
        sql.executeStoredProc(readSql_.toString(),
                              ioKey,
                              readOrder_ == null ? keyFields_
                                                 : readOrder_);
      }
      else
      {
        sql.executePrepared(readSql_.toString(),
                            ioKey,
                            readOrder_ == null ? keyFields_
                                               : readOrder_);
      }
    }
    else
    {
      sql.executeSql (readSql_.toString(), ioKey, readOrder_);
    }

    if (sql.prepareNextResultSet())
    {
			if (indexMap_ == null)
			{
				synchronized(this)
				{
					indexMap_ = (resultMap_ != null)
						 ? sql.setColumnNameMap (resultMap_)
						 : sql.setDefaultNameMap();
				}
			}
			else
			{
				sql.setColumnIndexMap (indexMap_);
			}
    }
	}
	
  private AnySql acquireSql(Any spec, Transaction t) throws AnyException
  {
    AnySql sql;
    if (t != null && !t.isAutoCommit())
    {
      // Ask the transaction for the sql resource
      sql = (AnySql)t.acquireResource(spec, SqlManager.instance(), -1);
      sql.setAutoCommit(false);
    }
    else
    {
      sql = (AnySql)SqlManager.instance().acquire(spec);
      sql.setAutoCommit(true);
    }
    
    return sql;
  }
  
  public boolean delete (Map ioKey,
												 Map outputItem,
												 Transaction t) throws AnyException
	{
		return delete(outputItem, t);
	}
	
	public boolean delete (Map outputItem,
                         Transaction t) throws AnyException
	{

		boolean written = false;
		
		if (deleteSql_ != null)
		{
			int     updated   = 0;
      boolean retry     = false;
      boolean statement = false;
      
      boolean badCardinality = false;
      
      do
      {
        retry     = false;
        statement = false;
        
        AnySql sql = acquireSql(sqlLogin_, t);
        
        
        try
        {
          // In the SQL implementation we don't require a separate key as
          // we assume that the key and non-key fields are held in the
          // output item.
          
          // re-use read order here.  We assume that the key fields for delete
          // are in the same order as they are for read.
          //sql.executeSql (deleteSql_.toString(), outputItem, readOrder_);
          if (prepared_ || stproc_)
          {
            if (stproc_)
            {
              sql.executeStoredProc(deleteSql_.toString(), outputItem, keyFields_);
            }
            else
            {
              sql.executePrepared(deleteSql_.toString(), outputItem, keyFields_);
            }
          }
          else
          {
            sql.executeSql (deleteSql_.toString(), outputItem, readOrder_);
          }

          statement = true;
          
          do;
          while (sql.resultsAvailable() != AnySql.ResUpdateCount);
        
          updated = sql.getUpdateCount();
          if (updated > 1)
            badCardinality = true;
        
          if (updated == 1)
            written = true;
        }
        
        catch (ContainedException ce)
        {
          if (statement)
          {
            if (!t.isAutoCommit())
              sql.setDueRollback();
            
            throw ce;
          }
          else
          {
            retry = t.isAutoCommit() && (sql.getStatementsDone() > 0);
            sql.close();
            if (!retry)
              throw ce;
          }
        }
        catch (AnyException e)
        {
          sql.close();
          throw e;
        }
      
        finally
        {
          if (t == null || t.isAutoCommit())
          {
            e_.setThrowable(null);
            SqlManager.instance().release(sqlLogin_, sql, null, e_);
          }
        }
      }
      while(retry);
			
			if (badCardinality)
				throw (new UnexpectedCardinalityException(getClass() + "delete()"));
      
      if (e_.getThrowable() != null)
        throw new RuntimeContainedException(e_.getThrowable());
    }
		return written;
	}
	
	public void useSqlServer(Any a) throws AnyException
	{
		sqlLogin_     = EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
																					 Catalog.instance().getCatalog(),
																					 a);
    
    Map m = (Map)SqlManager.instance().getSpec(sqlLogin_);
    if (m.contains(IoConstants.cardinality__))
    {
      cardinality_.copyFrom(m.get(IoConstants.cardinality__));
    }
	}
	
  // Called when we are being used by typedefs for their io bindings.
	public void setAuxInfo (Any a, Any subs)
	{
    if (a != null)
    {
      //System.out.println ("setAuxInfo: " + a);
      
      if (!(a instanceof Map))
        throw new AnyRuntimeException("SimpleSQLIO aux info must be a map");
      
      Map m = (Map)a;
      Map sm = (Map)subs;
      
      if (m.contains(READ_SQL))
      {
        // Do format substitutions on m from sm (to reuse select
        // statements etc
        Any readSql = m.get(READ_SQL);
        
        
        readSql_ = new ConstString(AnyMessageFormat.format(readSql.toString(),
                                                         sm));
      }
        
      if (m.contains(WRITE_SQL))
        writeSql_ = m.get(WRITE_SQL);
        
      if (m.contains(DELETE_SQL))
        deleteSql_ = m.get(DELETE_SQL);
        
      if (m.contains(READ_ORDER))
        readOrder_ = (Array)m.get(READ_ORDER);
        
      if (m.contains(WRITE_ORDER))
        writeOrder_ = (Array)m.get(WRITE_ORDER);
        
      if (m.contains(RESULT_MAP))
        resultMap_ = (Map)m.get(RESULT_MAP);
        
      if (m.contains(PREPARED))
        prepared_ = (((BooleanI)m.get(PREPARED)).getValue());

      if (m.contains(STPROC))
        stproc_ = (((BooleanI)m.get(STPROC)).getValue());
      
      keyFields_ = (Array)m.get(KEY_FIELDS);
      
      fieldOrder_ = (Array)m.get(Descriptor.fieldOrder__);
    }
		//System.out.println ("setAuxInfo: toString() " + toString());
	}

  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
		throw new UnsupportedOperationException("setStreams " + getClass());
  }
  

	/**
	 * No operation.
	 */
	public void close()
	{
	}
	
	public String toString()
	{
		return "SimpleSqlIO " + 
					 ((readSql_ != null) ? readSql_.toString() : "null") + 
					 ((writeSql_ != null) ? writeSql_.toString() : "null") + 
					 ((deleteSql_ != null) ? deleteSql_.toString() : "null") + 
					 ((sqlLogin_ != null) ? sqlLogin_.toString() : "null");
	}

  public Object clone() throws CloneNotSupportedException
  {
    SimpleSqlIO s = (SimpleSqlIO)super.clone();
    
    // only needs shallow copy as all members are read-only
    return s;
  }
}
