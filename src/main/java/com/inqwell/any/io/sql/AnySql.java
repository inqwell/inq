/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/sql/AnySql.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:23 $
 */
package com.inqwell.any.io.sql;


import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBigDecimal;
import com.inqwell.any.AnyBlob;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyByte;
import com.inqwell.any.AnyDate;
import com.inqwell.any.AnyDouble;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFloat;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyLong;
import com.inqwell.any.AnyMessageFormat;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyShort;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.Catalog;
import com.inqwell.any.CharI;
import com.inqwell.any.ConstBoolean;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Value;

/**
 * Provides an interface to JDBC expressing result sets and database
 * meta-data as structures of Anys
 */
public class AnySql extends AbstractAny
{
  public static ResultType ResNoMore      = new ResultType
                                             (ResultType.RESULT_NOMORE);
  public static ResultType ResUpdateCount = new ResultType
                                             (ResultType.RESULT_UPDATECOUNT);
  public static ResultType ResRowsAvail   = new ResultType
                                             (ResultType.RESULT_ROWSAVAIL);

  public static MetaSpec MetaBasic = new MetaSpec(MetaSpec.META_BASIC);
  public static MetaSpec MetaFull  = new MetaSpec(MetaSpec.META_FULL);

  public static Any COLUMN_NAMES = new ConstString ("name");
  public static Any COLUMN_TYPES = new ConstString ("type");
  public static Any COLUMN_IS_AUTOINCREMENT =
                                         new ConstString ("isAutoIncrement");
  public static Any COLUMN_IS_NULLABLE =
                                         new ConstString ("isNullable");

  public static Any sqlDebug__   = new ConstString("sqlDebug");

  private Connection _conn;
  private Statement  _lastStatement;  // remember the last statement we used

  private PrepareStatement _prepareStatement = new PrepareStatement();

  private AnyMessageFormat   _sqlFormatter = new AnyMessageFormat("");
  private ResultSet          _resultSet;
  private ResultSetMetaData  _resultSetMetaData;
  private int                _updateCount;
  private int                _currentColumn;
  private int                _numCols;
  private int                _numRows;
  private Map                _columnNameStruct;
  private Map                _metaData;
  private Map                _columnIndexMap;
  private Array              _columnNames;
  private Array              _columnTypes;
  
  private Array              _stmts;
  private boolean            _dateAsTime;

  private boolean            _firstResult;
  private boolean            _beforeFirstRow;
  private BooleanI           _debug;
  
  private boolean            _dueRollback;

  // Whether this connection has ever been used successfully.
  // Helps in determining whether the underlying database is
  // running or that the connection has just timed out.
  private int                _statementsDone;
  
  private Array              _onException;

  private SQLWarning         _warnings;
  private SQLWarning         _currentWarning;

  private Map                _jdbcMappings;

	private static String NO_RESULT_SET = new String ("No current result set");
  private static String BAD_STATE = new String
                           ("Incorrect state for current operation");

	/**
	 * Construct myself to connect to the specified database
	 *
	 * There seems to be a problem with DriverManager and the JRun (v2.2)
	 * (servlet management software) default Security Manager when JRun has been setup
	 * for JDK1.2Beta4.
	 * <p>
	 * Basically, if you have the JRunSecurityManager then when a dynamically loaded
	 * JDBC Driver registers itself with DriverManager - DriverManager quietly fails.
	 * <p>
	 * So when you call DriverManager.getConnection(url, usr, pwd) it raises
	 * an Exception saying that there were no suitable drivers.
	 * <p>
	 * If you remove the JRunSecurityManager setting then the DriverManager correctly
	 * allows Driver registration but the JDK1.2beta4 java.exe console window no longer
	 * outputs System.out messages !! PISS!!!
	 * <p>
	 * I don't what will happen when JDK1.2 is officially released but for now
	 * we will keep the JRunSecurityManager (ie to get debug output) and
	 * (luckily) we can instantiate the Sybase Driver and call its connect() method.
	 * Please note the supplied JRE with JRun (ie 1.1 compliant) does NOT output messages
	 * to System.out even with the default JRunSecurityManager!
	 * <p>
	 * The commented out code is :-
	 * <pre>
	 *	static
	 *	{
	 *		// Load the jConnect driver in case not in system properties.
	 *		try
	 *		{
	 *			Class.forName ("com.sybase.jdbc.SybDriver");
	 *		}
	 *		catch (ClassNotFoundException e)
	 *		{
	 *			e.printStackTrace ();
	 *		}
	 *	}
	 *
	 * </pre>
	 * @param url The database url to connect to.
	 * @param user The database login user name
	 * @param passwd The database login password.
	 * @exception ContainedException If a database access error occurs.
	 */
	public AnySql (String url,
                 String user,
                 String passwd,
                 Array  initStmts) throws AnyException
	{
		init();
    setInitStatements(initStmts);
		open(url, user, passwd);
	}

	public AnySql ()
	{
		init();
		_conn = null;
	}

  /**
   * Creates a static Statement object.  The Statement object
   * is retained for use by the executeSql method and so should not
   * generally be accessed directly while an SQL statement is active.
   * <p>
   * Any existing cached statement and results are closed.
   * @return a Statement object containing the SQL.
   * @exception AnyException If a database access error occurs.
   */
  private Statement getStatement () throws AnyException
  {
    try
    {
      // Discard any previous statement as we don't know its exact type
      cleanUp();
      checkIsClosed();
      _lastStatement =  _conn.createStatement();
      return (_lastStatement);
    }
    catch (SQLException e)
    {
      throw (new ContainedException (e));
    }
  }

  public void executeSql (StringI sql) throws AnyException
  {
    executeSql(sql.toString());
  }

  /**
   * Executes a literal SQL statement.  The statement can be any valid
   * SQL and is not restricted to specifically SELECT.
   * @param sql The sql statement to execute.
   * @exception AnyException If a database access error occurs.
   */
  public void executeSql (String sql) throws AnyException
  {
    boolean done = false;
    try
    {
      // If _lastStatement is not null we assume that it is a Statement and
      // not one of the derived classes which don't accept the execute
      // method with a string parameter.  If not, an exception will be thrown
      // anyway.  If it is null we create a Statement object on behalf of
      // the caller.

      cleanUp();

      if (_lastStatement == null)
        getStatement();

      printSql(sql);
      // boolean isResultSet =
      _lastStatement.execute (sql);
      done = true;
      
//System.out.println("getQueryTimeout() returns " + _lastStatement.getQueryTimeout());
//System.out.println ("*** Query T/O is zero");
//_lastStatement.setQueryTimeout (0);

      // The semantics of obtaining results from Statement (or derived) objects
      // is messy as the first result is accounted for in the return value
      // of Statement.execute(String)
      //readyResults (isResultSet, true);
    }
    catch (SQLException e)
    {
      if (done)
        onException(e);
      
      throw (new ContainedException(e, sql));
    }
    _statementsDone++;
  }

  /**
   * Executes the supplied  SQL statement which may be either a JDBC stored
   * procedure call or a standard SQL statement.  The statement is examined
   * and, if a JDBC stored procedure call, the Array order is used to extract
   * the desired parameters from the map params by using the array values
   * as keys to the map.  If not a stored procedure call sql is assumed
   * to be a standard SQL statement and is subject to formatting using
   * com.inqwell.any.AnyMessageFormat using values found in params, and order is
   * not used.
   */
  public void executeSql (String sql, Map params, Array order)
                                                    throws AnyException
  {
    cleanUp();

    if (sql.startsWith("{call"))
    {
      // Its a JDBC stored procedure call
      executeStoredProc (sql, params, order);
    }
    else
    {
      // Its standard SQL
      _sqlFormatter.applyPattern (sql);
      executeSql (_sqlFormatter.format(params));
    }
  }

  /**
   * Can only be used for executing a stored procedure JDBC call, since
   * in this case, the parameters have an implicit order and can be carried
   * in an Array only
   */
  private void executeSql (String sql, Array params)  throws AnyException
  {
    Iter i = params.createIterator();
    int  paramIndex = 0;


    boolean done = false;
    try
    {
      cleanUp();
      checkIsClosed();
      _prepareStatement.setStatement(_conn.prepareCall(sql));
      _lastStatement = _prepareStatement._s;

      while (i.hasNext())
      {
        Any a = i.next();  // get the value

        // set the parameter into the callable statement
        _prepareStatement.setParameter(a, ++paramIndex);
      }

      // execute the callable statement
      printSql(sql);
      //boolean isResultSet =
      _prepareStatement._s.execute();
      done = true;
      //readyResults (isResultSet, true);
    }

    catch (SQLException e)
    {
      if (done)
        onException(e);
      
      throw (new ContainedException(e, sql, params));
    }
    _statementsDone++;
  }

  public void executeStoredProc (String sql, Map params, Array order)
                                                    throws AnyException
  {
    if (params.entries() == 0)
    {
      executeSql (sql, order);
    }
    else
    {
      Iter i = null;
      if (order == null || order.entries() == 0)
      {
        i = params.createKeysIterator();
      }
      else
      {
        i = order.createIterator();
      }

      int  paramIndex = 1;

      boolean done = false;
      try
      {
        checkIsClosed();
        _prepareStatement.setStatement(_conn.prepareCall(sql));
        _lastStatement = _prepareStatement._s;

        while (i.hasNext())
        {
          Any a = i.next();  // get the key

          a = params.get(a);

          // set the parameter into the callable statement
          paramIndex += _prepareStatement.setParameter(a, paramIndex);
        }

        // execute the callable statement
        //boolean isResultSet =
        _prepareStatement._s.execute();
        done = true;

        //readyResults (isResultSet, true);
      }

      catch (SQLException e)
      {
        if (done)
          onException(e);
        
        throw (new ContainedException(e));
      }
      _statementsDone++;
    }
  }

  /**
   * Executes an SQL statement as a JDBC PreparedStatement.  The parameters
   * for the statement are applied according to their ordinal position
   * in the Array.
   * @param pstmt The statement to execute.
   * @exception AnyException If a database access error occurs.
   */
  public void executePrepared(String sql, Map params, Array order) throws AnyException
  {
    if (params == null || params.entries() == 0)
    {
      // No args means bald sql
      executeSql(sql);
      return;
    }

    boolean done = false;
    try
    {
      cleanUp();
      checkIsClosed();
      printSql(sql);
      _prepareStatement.setStatement(_conn.prepareStatement(sql));
      _lastStatement = _prepareStatement._s;

      for(int paramIndex = 0; paramIndex < order.entries(); paramIndex++)
      {
        Any a = order.get(paramIndex);  // get the key
        a = params.get(a);

        // set the parameter into the callable statement
        _prepareStatement.setParameter(a, paramIndex+1);
        if (_debug.getValue())
        {
          // TODO :see StringI.isNull() and StringI.toString();
          if (a instanceof Value)
          {
            Value v = (Value)a;
            System.out.println("Setting " + order.get(paramIndex) + " as " + (v.isNull() ? "<null>" : v.toString()));
          }
          else
            System.out.println("Setting " + order.get(paramIndex) + " as " + a);
        }
      }

      // Execute the callable statement
      //boolean isResultSet = 
      _prepareStatement._s.execute();
      done = true;

      //readyResults (isResultSet, true);
    }

    catch (SQLException e)
    {
      if (done)
        onException(e);
      
      throw (new ContainedException(e, sql, params));
    }
    _statementsDone++;
  }

  /**
   * Check for any kind of results from the current SQL statement.
   * @return ResUpdateCount if an update count is available,
   * ResRowsAvail if rows are available or ResNoMore when there
   * are no further results.
   * <p>
   * This method gives the caller control over how available results
   * are to be processed.
   * @exception AnyException If a database access error occurs.
   */
  public ResultType resultsAvailable () throws AnyException
  {
    try
    {
      if (!_firstResult)
      {
        _lastStatement.getMoreResults();
      }
      _firstResult = false;

      _updateCount = _lastStatement.getUpdateCount();

      if (_updateCount >= 0)
        return ResUpdateCount;

      _resultSet = _lastStatement.getResultSet();

      if (_resultSet != null)
        return ResRowsAvail;

      return ResNoMore;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }


  /**
   * prepareNextResultSet attempts to locate the next result set from
   * the current SQL statement (by repeatedly calling resultsAvailable().
   * No rows are fetched by this method and any preceeding results of
   * type ResUpdateCount are discarded.
   * @return true if a result set is available; false if
   * no result set was found.
   */
  public boolean prepareNextResultSet() throws AnyException
  {
    ResultType resType;
    boolean b = false;

    reset();

    while (!b && (resType = resultsAvailable()) != ResNoMore)
    {
      if (resType == ResRowsAvail)
      {
        _numRows = 0;
        b = true;
        fetchMetaData();
        break;
      }
    }
    return b;
  }

  /**
   * Establish a map for the current result set.  The map keys are column
   * names which are expected to be present in the result set.  The values
   * are the keys of the map values into which the column values will be
   * put.
   * <p>
   * Note that this method requires that the column names in the underlying
   * result set are unique.  If they are not a run time exception will
   * be thrown.
   * <p>
   * The supplied map is transformed into an internal representation mapping
   * from column indices to map keys which is retained for subsequent
   * row processing.
   * @param map a struct mapping column names to struct field names.
   * @return the transform map allowing clients to use this directly for
   * future queries.
   * @exception AnyException If any column named in the map is not in the
   * result set or some other state error.
   */
  public Map setColumnNameMap (Map nameMap) throws AnyException
  {
    getNamesStruct ();

    Map indexMap = AbstractComposite.simpleMap();

    // Iterate over the keys in the name map to check that they are all in
    // the current result set's column names.  As we do this we build a
    // map which translates column indices to the target struct field names
    Iter iter = nameMap.createKeysIterator();

    while (iter.hasNext())
    {
      Any a = iter.next();
      if (!_columnNameStruct.contains(a))
        throw (new AnyException
                     ("column name " + a + " not in current result set"));

      indexMap.add (_columnNameStruct.get(a), // column index
                    nameMap.get(a));          // field name
    }

    _columnIndexMap = indexMap;

    return _columnIndexMap;
  }

  /**
   * Convenience routine which assumes that the struct names are the same
   * as the column names.  Therefore builds a name map based on the column
   * names and passes it to setColumnNameMap(Map).  The order of the columns
   * in the result set is still irrelevant
   */
  public Map setDefaultNameMap () throws AnyException
  {
    getNames ();

    Map defaultMap = AbstractComposite.simpleMap();

    Iter i = _columnNames.createIterator();
    while (i.hasNext())
    {
      Any a = i.next();
      defaultMap.add (a, a);
    }
    return setColumnNameMap(defaultMap);
  }

  /**
   * Establish a map for the current result set.  The map relates column
   * indices in the current result set to keys of the map values into
   * which the column values will be put.  Columns are numbered from zero.
   * <p>
   * The supplied map retained for subsequent processing of the result set
   * and so should not be altered by the caller while this is in progress.
   * A suitable map may have been returned by a previous call to
   * setColumnNameMap().
   * <p>
   * The supplied map is verified against the current result set to ensure
   * all column indices are in range.
   * @param indexMap a mapping of column names to struct field names.
   * @return the transform map allowing clients to use this directly for
   * future queries.
   * @exception AnyException If any column named in the map is not in the
   * result set or some other state error.
   */
  public void setColumnIndexMap (Map indexMap) throws AnyException
  {
    getColumnRange();

    IntI column;
    Iter iter = indexMap.createKeysIterator();

    while (iter.hasNext())
    {
      column = (IntI)iter.next();
      if (column.getValue() < 0 ||
          column.getValue() >= _numCols)
      {
        throw (new AnyException ("Column index out of bounds: " + column));
      }
    }
    _columnIndexMap = indexMap;
  }

  /**
   * Fetch the next row from the current result set and place the columns
   * into the supplied map.  There must be a column index map
   * in effect when this method is called which is relevant to the supplied
   * structure although it is not an error if the entire
   * Map is not described.
   * @return TRUE if a row was fetched, FALSE when all rows in the
   * current result set have been returned.
   */
  public boolean getNextRow (Map s) throws AnyException
  {
		if (_columnIndexMap == null)
		{
			throw (new AnyException (BAD_STATE));
		}

		boolean hasMoreRows = nextRow();
		if (hasMoreRows)
		{
			Iter iter = _columnIndexMap.createKeysIterator();
			while (iter.hasNext())
			{
				IntI columnNumber = (IntI)iter.next();
				// Put the column value in the caller's struct.  If the types are
				// incompatible a runtime exception occurs.
				Any k = _columnIndexMap.get(columnNumber);
//        if (_debug.getValue())
//				  System.out.println("Mapping column " + k);
				if (s.contains(k))
        {
          Any v = s.get(k);
          s.replaceValue(k,
                         readColumn(columnNumber.getValue(), v));
        }
			}
			_numRows++;
		}
		return hasMoreRows;
  }

  /**
   * Fetch the next row from the current result set as an anonymous
   * array containing the column values.  There must be a result set
   * available by having previously called prepareNextResultSet
   * The array elements will have Any types
   * appropriate to the database column types of the result set.
   * @return an Array containing the column values if a row was fetched
   * or null when there are no more rows in the result set.
   */
  public Array getNextRow () throws AnyException
  {
		if (_numCols < 0)
		{
			throw (new AnyException (BAD_STATE));
		}

		Array a = null;

		boolean hasMoreRows = nextRow();
		if (hasMoreRows)
		{
			a = AbstractComposite.array (_numCols);
			int i;
			for (i = 0; i < _numCols; i++)
			{
				Any columnValue = readColumn(i, null);
				a.add (columnValue.cloneAny());
			}
			_numRows++;
		}
		return a;
  }

  /**
   * Fetch the next row from the current result set into the given
   * array.  There must be a result set available by having previously
   * called prepareNextResultSet.  The
   * array is expected to hold sufficient elements with Any types
   * appropriate or type compatible to the database column types of the
   * result set.  An example of such an array is that returned by
   * getNextRow().
   * @return TRUE if a row was fetched, FALSE when all rows in the
   * current result set have been returned.
   */
  public boolean getNextRow (Array a) throws AnyException
  {
		if (_numCols < 0)
		{
			throw (new AnyException (BAD_STATE));
		}

		boolean hasMoreRows = nextRow();
		if (hasMoreRows)
		{
			int i;
			for (i = 0; i < _numCols; i++)
			{
        Any v = a.get(i);
				Any columnValue = readColumn(i, v);
				a.replaceValue (i, columnValue);
			}
			_numRows++;
		}
		return hasMoreRows;
  }

  public boolean discardRow() throws AnyException
  {
		if (_numCols < 0)
		{
			return false;
		}

		return nextRow();
  }

  public int getUpdateCount()
  {
    return _updateCount;
  }

  /**
   * Returns the number of rows were affected by the last SQL statement.
   * For statements returning rows this value will be correct after any
   * of the getNextRow() method returns no more rows.
   * @return row count
   */
  public int rowCount()
  {
    return _numRows;
  }

  /**
   * Returns the numebr of columns in the current result set.
   */
  public int columnCount() throws AnyException
  {
    return getColumnRange();
  }

  /**
   * Returns a suitable Map for the current result set with column names for
   * the keys and values of an Any type appropriate to the column type.
   * Result set must therefore be available and have unique column names.
  public Map getResultMap() throws AnyException
  {
    Map metaData = getMetaData(MetaBasic);
    Map resultMap = AbstractComposite.simpleMap();

    // Iterate over names and column types to make the map

    Iter iNames = _columnNames.createIterator();

    int i = 0;

    while (iNames.hasNext())
    {
      resultMap.add (iNames.next(), readColumn(i++).cloneAny());
    }
    return resultMap;
  }
   */

  /**
   * Returns some meta data about the current result set.
   * The amount of meta-data extracted is determined by the metaExtent
   * argument.
   * @param metaExtent AnySql.MetaBasic to return column names and types
   * only,  AnySql.MetaFull to return all available meta-data.
   *
   */
  public Map getMetaData(MetaSpec metaExtent) throws AnyException
  {
    // Pre-condition - we must have a result set object available
    // to extract meta data from, otherwise there is no meta data

    if (_resultSet == null)
    {
      throw (new AnyException (NO_RESULT_SET));
    }

    fetchMetaData();

    // the root of the meta data is a struct

    Map s = AbstractComposite.simpleMap();

    // We always fetch the names and column types of the result set - this
    // is the basic minimum...

    s.add (COLUMN_NAMES, getNames ());
    s.add (COLUMN_TYPES, getTypes ());

    // when asked for all meta data we add the set of data defined below

    if (metaExtent == MetaFull)
    {
      s.add (COLUMN_IS_AUTOINCREMENT, getAutoIncrement ());
      s.add (COLUMN_IS_NULLABLE, getNullable ());

      // there are other candidates like isCurrency etc which
      // could always be added...
    }

    return s;
  }

  public Array getColumnNames () throws AnyException
  {
    return getNames();
  }

  /**
   * getNextResultRows generates an Any structure containing all the
   * rows from the next available result set.  The return value
   * is the root node of the structure shown below:
   * <pre>
   *
   *                           struct
   *                              |
   *                ----------------------------
   *               |                            |
   *            "names"                      "rows"
   *             array                        array
   *               |                            |
   *        ---------------            --------------------
   *       |      |       |            |      |            |
   *    "tom"  "dick"  "harry"        row    row          row
   *                                 array  array  ....  array
   *                                  |
   *                              ----------
   *                             |    |     |
   *                            col  col   col
   *                            val  val   val
   *
   *</pre>
   * The "names" array contains the column names of all the result set's
   * columns in left to right order.  Each set or row values is stored
   * within a row array, again in left to right column order.
   * <p>
   * The types of Any used for the column values are determined
   * from their corresponding JDBC types according to the current mapping...
   * <p>
   * This method may be used where the result set structure is not known
   * at compile time.
   * @return an AnyStruct containing the result set or null if there are
   * no more result sets.
   * @see java.sql.Types
   * @exception AnyException If a database access error occurs.
   */
  public Any getNextResultRows() throws AnyException
  {
    return null; // tbd
  }

  /**
   * getAllResultRows generates an Any structure containing all the
   * remaining result sets from the current SQL statement.  The root node is
   * an array composite where each element represents an individual
   * result set according to the structure given in getNextResultRows().
   * <P>
   * This method is intended for use by tools which generate reports,
   * screen tables etc from unknown results.
   * @return an Array containing the remaining result sets or null if
   * there are no more result sets.
   * @exception AnyException If a database access error occurs.
   */
  public Any getAllResultRows() throws AnyException
  {
    return null; // tbd
  }


  /**
   * getResultSet returns the next available result set
   * or null if there are no more result
   * sets or there is no active SQL statement.  If this method is called
   * after resultsAvailable has returned ResRowsAvail then that result
   * set is returned.  Otherwise resultsAvailable is called until either
   * ResRowsAvail or ResNoMore is returned.
   * <P>
   * This method is low-level and exposes the underlying JDBC result set
   * object for those that require it.  The methods getNextResultRows or
   * getAllResultRows are preferred.
   * @exception AnyException If a database access error occurs.
   */
  public ResultSet getResultSet() throws AnyException
  {
    return null; // tbd
  }

  public boolean getNextWarning(StringI msg,
                                IntI    errorCode,
                                StringI sqlState) throws AnyException
  {
    boolean result = false;

    if (_warnings == null)
      fetchWarnings();

    if (_currentWarning != null)
    {
      msg.setValue (_currentWarning.getMessage());
      errorCode.setValue (_currentWarning.getErrorCode());
      sqlState.setValue (_currentWarning.getSQLState());
      _currentWarning = _currentWarning.getNextWarning();
      result = true;
    }

    return result;
  }

  public void open(String url, String user, String passwd) throws AnyException
  {
	 	// load up required driver if not done already
		Driver 		driver;

		try
		{
      driver = DriverManager.getDriver(url);
		}
		catch (Exception eX)
		{
			throw new ContainedException(eX);
		}

    try
    {
      setDefaultJdbcMappings();
      reset();

      // Do not use DriverManager see javadoc comments
      // _conn = java.sql.DriverManager.getConnection(url, user, passwd);
			//
			// use the driver_ directly
			//
			java.util.Properties p = new java.util.Properties();
			p.put("user", user);
			p.put("password", passwd);
      p.put("jdbcCompliantTruncation","false");
      _conn = driver.connect(url, p);
      runInitStatements();
		}
		catch (SQLException e)
		{
			//e.printStackTrace();
      throw (new ContainedException(e));
		}
  }

  public void close()
  {
    if (_conn == null)
      return;

    try
    {
      _dueRollback = false;
      rollback();
      _conn.close();
    }
    catch (SQLException e)
    {
      // maybe its OK to do nothing here!
      //throw (new ContainedException(e));
    }
    catch(AnyException ae) {}
    finally
    {
      _conn = null;
      _statementsDone = 0;
    }
  }

  public boolean isClosed () throws AnyException
  {
		if (_conn == null)
			return true;

		try
		{
			if (_conn.isClosed())
			{
				_conn = null;
				return true;
			}
		}
		catch (SQLException e)
		{
			_conn = null;
      throw (new ContainedException(e));
		}

		if (_conn == null)
			return true;
		else
			return false;
	}

  public int getStatementsDone()
  {
    return _statementsDone;
  }

  public void commit()
  {
    boolean dueRollback = _dueRollback;
    _dueRollback = false;
    try
    {
      if (_conn != null && !_conn.getAutoCommit())
      {
        if (dueRollback)
          _conn.rollback();
        else
        {
          _conn.commit();
        }
      }
    }
    catch(SQLException e)
    {
      // If something went wrong with the commit should we close the
      // connection may be? Probably not as we've got this far it
      // likely means an application error
      throw new RuntimeContainedException(e);
    }
  }
  
  public void rollback() throws AnyException
  {
    try
    {
      if (_conn != null && !_conn.getAutoCommit())
      {
        //System.out.println("Rolling back " + this);
        _conn.rollback();
      }
    }
    catch(SQLException e)
    {
      throw new ContainedException(e);
    }
  }
  
  public void setAutoCommit(boolean autoCommit)
  {
    try
    {
      if (_conn != null)
        _conn.setAutoCommit(autoCommit);
    }
    catch(SQLException e)
    {
      // If we caught an exception then close the connection,
      // preventing it from being used further.
      close();
      throw new RuntimeContainedException(e);
    }
  }
  
  public void setDueRollback()
  {
    _dueRollback = true;
  }
  
  public boolean isAutoCommit()
  {
    try
    {
      return _conn.getAutoCommit();
    }
    catch(SQLException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public void setNullStrings(Map m)
  {
    _sqlFormatter.setNullStrings(m);
  }

  public void setDelimitersMap(Map delims)
  {
    _sqlFormatter.setDelimitersMap(delims);
  }

  public void setInitStatements(Array stmts)
  {
    _stmts = stmts;
  }
  
  public void setDateAsTime(boolean dateAsTime)
  {
    _dateAsTime = dateAsTime;
  }
  
  public void setOnException(Array stmts)
  {
    _onException = stmts;
  }

  private void init()
  {
		_conn  = null;
    Map argsMap = (Map)Catalog.instance().getCommandArgs();
    synchronized(argsMap)
    {
      if (argsMap.contains(sqlDebug__))
        _debug = (BooleanI)argsMap.get(sqlDebug__);
      else
        argsMap.add(sqlDebug__, (_debug = AnyBoolean.FALSE));
    }
	}

  public boolean isDebugEnabled()
  {
    return _debug.getValue();
  }

  private void checkIsClosed () throws AnyException
  {
    if (isClosed())
      throw (new AnyException ("Connection is closed!"));
  }

  private void fetchWarnings() throws AnyException
  {
    try
    {
//      _warnings = addWarnings(_conn.getWarnings());
      if (_lastStatement != null)
      {
        _warnings = addWarnings(_lastStatement.getWarnings());
        _lastStatement.clearWarnings();
      }
//      if (_resultSet != null)
//        _warnings = addWarnings(_resultSet.getWarnings());
    }
    catch (SQLException e)
    {
      _warnings = null;
      throw (new ContainedException(e));
    }

    _currentWarning = _warnings;
  }

  private SQLWarning addWarnings(SQLWarning w)
  {
    SQLWarning result;
    SQLWarning last;
    SQLWarning current;

    if (_warnings == null)
    {
      result = w;
    }
    else
    {
      result  = _warnings;
      if (w != null)
      {
        last    = _warnings;
        do
        {
          current = last;
        }
        while ((last = current.getNextWarning()) != null);
        current.setNextWarning(w);
      }
    }
    return result;
  }

  /**
   * Releases the internal Statement and any ResultSet object; reinitialises
   * the instance.
   * @exception AnyException If a database access error occurs.
   */
  public void cleanUp() throws AnyException
  {
    reset();
    try
    {
      if (_lastStatement != null)
        _lastStatement.close();

      if (_resultSet != null)
      {
        _resultSet.close();
      }
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
    finally
    {
      _resultSet = null;
      _lastStatement     = null;
      _firstResult       = true;
    }
  }

  /**
   * Closes the internal Statement and any ResultSet object; The internal
   * statement object is retained for furure use.
   */
  public void reset()
  {
    _numCols = -1;
    _resultSetMetaData = null;
    _currentColumn     = -1;
    _numRows           = 0;
    _columnNameStruct  = null;
    _metaData          = null;
    _columnIndexMap    = null;
    _columnNames       = null;
    _columnTypes       = null;
    _updateCount       = -1;
    _beforeFirstRow    = true;
    _warnings          = null;
    _currentWarning    = null;

    // Don't know if this is
    //if (_lastStatement != null)
    //{
      //if (_lastStatement.getClass() ==

  }

  private void runInitStatements() throws AnyException
  {
    if (_stmts != null)
    {
      for (int i = 0; i < _stmts.entries(); i++)
      {
        Any stmt = _stmts.get(i);
        executeSql(stmt.toString());
        do;
        while (resultsAvailable() != AnySql.ResUpdateCount);
      
        getUpdateCount();
      }
    }
  }
  
  // defunct
//  private void readyResults(boolean isResultSet,
//                            boolean first) throws AnyException
//  {
//    try
//    {
//      if (isResultSet)
//      {
//        if (first)
//          System.out.println ("first");
//        else
//          System.out.println ("subsequent");
//        _resultSet   = _lastStatement.getResultSet();
//        _updateCount = -1;
//      }
//      else
//      {
//        _updateCount = _lastStatement.getUpdateCount();
//        _resultSet   = null;
//      }
//      _firstResult = first;
//    }
//    catch (SQLException e)
//    {
//      throw (new ContainedException(e));
//    }
//  }

  private int getColumnRange () throws AnyException
  {
    try
    {
      if (_resultSetMetaData == null)
      {
        throw (new AnyException (BAD_STATE));
      }
      if (_numCols < 0)
        _numCols = _resultSetMetaData.getColumnCount();
      return _numCols;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private Array getNames () throws AnyException
  {
    try
    {
      fetchMetaData();

      if (_columnNames == null)
      {
        Array  a = AbstractComposite.array(_numCols);

        for (int i = 1; i <= _numCols; i++)
        {
          ConstString cName = new ConstString (_resultSetMetaData.getColumnLabel(i));
          a.add (cName);
        }
        _columnNames      = a;
      }

      return _columnNames;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private Map getNamesStruct () throws AnyException
  {
    try
    {
      fetchMetaData();

      if (_columnNameStruct == null)
      {
        Map s = AbstractComposite.simpleMap();

        for (int i = 1; i <= _numCols; i++)
        {
          ConstString cName = new ConstString (_resultSetMetaData.getColumnLabel(i));
          s.add (cName, new ConstInt(i - 1));
        }
        _columnNameStruct = s;
      }

      return _columnNameStruct;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private Array getTypes () throws AnyException
  {
    try
    {
      fetchMetaData();

      if (_columnTypes == null)
      {
        Array a = AbstractComposite.array(_numCols);

        for (int i = 1; i <= _numCols; i++)
        {
          a.add (new ConstInt (_resultSetMetaData.getColumnType(i)));
        }
        _columnTypes = a;
      }
      return _columnTypes;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private Array getAutoIncrement () throws AnyException
  {
    try
    {
      fetchMetaData();

      Array a = AbstractComposite.array(_numCols);

      for (int i = 1; i <= _numCols; i++)
      {
        a.add (new ConstBoolean (_resultSetMetaData.isAutoIncrement(i)));
      }
      return a;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private Array getNullable () throws AnyException
  {
    try
    {
      fetchMetaData();

      Array a = AbstractComposite.array(_numCols);

      for (int i = 1; i <= _numCols; i++)
      {
        a.add (new ConstInt (_resultSetMetaData.isNullable(i)));
      }
      return a;
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }
  }

  private void fetchMetaData() throws AnyException
  {
    if (_resultSet == null)
    {
      throw (new AnyException (NO_RESULT_SET));
    }

    try
    {
      if (_resultSetMetaData == null)
      {
        _resultSetMetaData = _resultSet.getMetaData();
      }
    }
    catch (SQLException e)
    {
      throw (new ContainedException(e));
    }

    getColumnRange();
  }

	private void printSql(String sql)
	{
		if (_debug.getValue())
			System.out.println (sql);
	}
	
	private Any onException(SQLException e)
	{
	  if (_onException == null)
	    return null;
	  
	  try
	  {
	    //cleanUp();
	    // TODO: run any onException sql statement and return its results
	  }
	  catch(Throwable  t)
	  {
	    // TODO: log
	  }
	  return null;
	}

  // Wrap up the execution of the Function classes to process the
  // returned column values.  Return the column value read.  Will be
  // read into v if supplied, otherwise an internal instance will
  // be returned and overwritten by the next row fetch.
  private Any readColumn (int columnNumber, Any v) throws AnyException
  {
    getTypes();

    // Set current column member variable read in the processColumnXXX fns
    _currentColumn      = columnNumber + 1;

    Any t = _columnTypes.get(columnNumber);
    if (!_jdbcMappings.contains(t))
      throw (new AnyException ("Unsupported JDBC type in column: " +
                                  _currentColumn +
                                  " type: " + t));

    Func readColumnF = (Func)_jdbcMappings.get(_columnTypes.get(columnNumber));

    Any columnValue = readColumnF.execFunc(v);
    return columnValue;
  }

	/**
	 * Check if there is another row available.  Note that this
	 * method disturbs the internal state such that further
	 * calls to <code>getNextRow()</code> will miss one row.
	 * Thus this method is only useful to test for more rows
	 * where to find one represents an error
	 */
  boolean nextRow() throws AnyException
  {
    _beforeFirstRow = false;
    try
    {
			return _resultSet.next();
		}
		catch (SQLException e)
		{
			return false;
		}
  }

	protected void finalize()
	{
		close();
	}

  //protected void processColumnBINARY (Any...);
  //protected void processColumnVARBINARY (Any...);
  //protected void processColumnLONGVARBINARY (Any...);
  //protected void processColumnBIT (AnyBoolean a);
  //protected void processColumnCHAR (AnyString a);
  //protected void processColumnLONGVARCHAR (AnyString a);
  //protected void processColumnNUMERIC (AnyDouble a);
  //protected void processColumnDECIMAL (AnyDouble a);
  protected void processColumnSMALLINT (ShortI a, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
      a.setValue(_resultSet.getShort(_currentColumn));
    if (_resultSet.wasNull())
      a.setNull();
  }

  protected void processColumnINTEGER (IntI a, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
      a.setValue(_resultSet.getInt(_currentColumn));
    if (_resultSet.wasNull())
      a.setNull();
  }

  protected void processColumnTINYINT (ByteI b, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
      b.setValue(_resultSet.getByte(_currentColumn));
    if (_resultSet.wasNull())
      b.setNull();
  }

  protected void processColumnBIGINT (LongI l, Any a) throws SQLException
  {
    if (!_beforeFirstRow)
      l.setValue (_resultSet.getLong(_currentColumn));
    if (_resultSet.wasNull())
      l.setNull();
  }

  protected void processColumnREAL (FloatI a, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
      a.setValue(_resultSet.getFloat(_currentColumn));
    if (_resultSet.wasNull())
      a.setNull();
  }

  protected void processColumnDOUBLE (DoubleI a, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
      a.setValue(_resultSet.getDouble(_currentColumn));
    if (_resultSet.wasNull())
      a.setNull();
  }

  protected void processColumnDECIMAL (Decimal a, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
    {
      // If the db column is NULL then the value returned by jdbc is
      // Java null.  In this case, we cannot determine the scale of the
      // db column and no error can be detected on loss of scale - no
      // big deal as the value is null anyway.
			BigDecimal col = null;
			col = _resultSet.getBigDecimal(_currentColumn);
      //System.out.println("FROM DB " + col);
			if (col != null)
      {
        a.setScale(col.scale());  // Otherwise destination will round.
				a.setValue(col);
        //System.out.println("TEMPORARY " + a);

      }
			else
				a.setNull();
		}
  }

  protected void processColumnCHAR (StringI s, Any v) throws SQLException
  {
    if (!_beforeFirstRow)
		{
			String col = _resultSet.getString(_currentColumn);
			if (col != null)
				s.setValue(col.trim());
			else
				s.setNull();
		}
  }

  protected void processColumnDATE (DateI d, Any v) throws SQLException
  {
    // JDBC DATE type.
    if (!_beforeFirstRow)
		{
      if (_dateAsTime)
      {
        // This is for the oracle problem of representing its DATE type, which
        // holds time information to 1 second accuracy, as jdbc type DATE but
        // for which the standard drivers from oracle flatten the time part to
        // zero. Thus we have the case that times go into the DB but do not
        // come out. This is as of oracle 10g. Apparently fixed in 11...
        // See links:
        // http://www.nabble.com/groovy.sql-doesn't-return-time-of-day-info-in-an-Oracle-db-td15814957.html
        // http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.html#08_01
        java.sql.Timestamp ts = _resultSet.getTimestamp(_currentColumn);
        
        if (ts != null)
          d.setTime(ts.getTime());
        else
          d.setNull();
      }
      else
      {
        java.sql.Date sd = _resultSet.getDate(_currentColumn);
        
        if (sd != null)
          d.setTime(sd.getTime());
        else
          d.setNull();
      }
		}
  }

  protected void processColumnTIME (DateI d, Any v) throws SQLException
  {
    // JDBC TIME type.
    if (!_beforeFirstRow)
		{
			java.sql.Time t = _resultSet.getTime(_currentColumn);
			if (t != null)
				d.setTime(t.getTime());
			else
				d.setNull();
		}
  }

  protected void processColumnTIMESTAMP (DateI d, Any v) throws SQLException
  {
    // JDBC TIMESTAMP type.  Forget the nano-seconds!
    if (!_beforeFirstRow)
		{
			java.sql.Timestamp ts = _resultSet.getTimestamp(_currentColumn);
			if (ts != null)
				d.setTime(ts.getTime());
			else
				d.setNull();
		}
  }

  protected void processColumnBLOB(Any a) throws SQLException
  {
    if (a == null)
      throw new AnyRuntimeException("Cannot read blobs anonymously");

    AnyBlob b = (AnyBlob)a;

    if (!_beforeFirstRow)
		{
			InputStream is = _resultSet.getBinaryStream(_currentColumn);
			if (is == null)
			  b.setValue(AnyNull.instance());
			else
      {
        b.fillBlob(is, -1);
        // TODO: Close the stream? Actually fillBlob does it.
      }
		}
  }

  protected void processColumnCLOB(Any a) throws SQLException
  {
    if (a == null)
      throw new AnyRuntimeException("Cannot read clobs anonymously");

    AnyBlob b = (AnyBlob)a;

    if (!_beforeFirstRow)
		{
			InputStream is = _resultSet.getAsciiStream(_currentColumn);
			if (is == null)
			  b.setValue(AnyNull.instance());
			else
        b.fillBlob(is, -1);
		}
  }

  private void setDefaultJdbcMappings()
  {
    _jdbcMappings = AbstractComposite.simpleMap();

    // Set up the JDBC to Any default mappings.  Those commented out are
    // either not implemented yet or not supported at all.

    _jdbcMappings.add (new ConstInt(java.sql.Types.INTEGER),
                       new ProcessColumnINTEGER (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.SMALLINT),
                       new ProcessColumnSMALLINT (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.BIGINT),
                       new ProcessColumnBIGINT (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.REAL),
                       new ProcessColumnREAL (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.FLOAT),
                       new ProcessColumnDOUBLE (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.DOUBLE),
                       new ProcessColumnDOUBLE (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.DECIMAL),
                       new ProcessColumnDECIMAL (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.NUMERIC),
                       new ProcessColumnDECIMAL (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.CHAR),
                       new ProcessColumnCHAR (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.VARCHAR),
                       new ProcessColumnCHAR (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.DATE),
                       new ProcessColumnDATE (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.TIME),
                       new ProcessColumnTIME (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.TIMESTAMP),
                       new ProcessColumnTIMESTAMP (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.BLOB),
                       new ProcessColumnBLOB (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.CLOB),
                       new ProcessColumnCLOB (this));

    _jdbcMappings.add (new ConstInt(java.sql.Types.LONGVARBINARY),
                       new ProcessColumnBLOB (this));
  }

  // Inner class to handle the setting of parameters into prepared and callable
  // statements.

  static class PrepareStatement extends AbstractVisitor
  {
    PreparedStatement  _s;
    int                _paramIndex;
    int                _used;
    ArrayList          _dates = new ArrayList(4);
    short              _dateIndex;

    public void setStatement (PreparedStatement s)
    {
      _s = s;
      _dateIndex = 0;
    }

    public int setParameter(Any a, int paramIndex)
    {
      _paramIndex = paramIndex;
      _used = 1;
      a.accept(this);
      return _used;
    }

//    public boolean execute() throws AnyException
//    {
//      try
//      {
//        return _s.execute();
//      }
//      catch (SQLException e)
//      {
//        throw (new ContainedException(e));
//      }
//    }

    public void visitAnyBoolean (BooleanI b)
    {
      try
      {
        _s.setBoolean (_paramIndex, b.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyByte (ByteI b)
    {
      try
      {
        if (b.isNull())
          _s.setNull(_paramIndex, Types.TINYINT);
        else
          _s.setByte (_paramIndex, b.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyChar (CharI c)
    {
      try
      {
        if (c.isNull())
          _s.setNull(_paramIndex, Types.CHAR);
        else
          _s.setByte (_paramIndex, (byte)c.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyInt (IntI i)
    {
      try
      {
        if (i.isNull())
          _s.setNull(_paramIndex, Types.INTEGER);
        else
          _s.setInt (_paramIndex, i.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyShort (ShortI s)
    {
      try
      {
        if (s.isNull())
          _s.setNull(_paramIndex, Types.SMALLINT);
        else
          _s.setShort (_paramIndex, s.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyLong (LongI l)
    {
      try
      {
        if (l.isNull())
          _s.setNull(_paramIndex, Types.INTEGER);
        else
          _s.setLong (_paramIndex, l.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyFloat (FloatI f)
    {
      try
      {
        if (f.isNull())
          _s.setNull(_paramIndex, Types.FLOAT);
        else
          _s.setFloat (_paramIndex, f.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      try
      {
        if (d.isNull())
          _s.setNull(_paramIndex, Types.DOUBLE);
        else
          _s.setDouble (_paramIndex, d.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitDecimal (Decimal d)
    {
      try
      {
        if (d.isNull())
          _s.setNull(_paramIndex, Types.DECIMAL);
        else
          _s.setBigDecimal (_paramIndex, d.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyString (StringI s)
    {
      try
      {
        if (s.isNull())
          _s.setNull(_paramIndex, Types.VARCHAR);
        else
          _s.setString (_paramIndex, s.getValue());
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyDate (DateI d)
    {
      try
      {
        if (d.isNull())
          _s.setNull(_paramIndex, Types.DATE);
        else
        {
          java.sql.Timestamp sqlDate;
          if (_dateIndex == _dates.size())
          {
            sqlDate = new java.sql.Timestamp(0);
            _dates.add(sqlDate);
            _dateIndex++;
          }
          else
          {
            sqlDate = (java.sql.Timestamp)_dates.get(_dateIndex++);
          }
          sqlDate.setTime (d.getTime());
          _s.setTimestamp (_paramIndex, sqlDate);
          //System.out.println("Date : " + System.identityHashCode(sqlDate) + " " + sqlDate);
        }
      }
      catch (SQLException e)
      {
        throw (new RuntimeContainedException(e));
      }
    }

    public void visitAnyObject (ObjectI o)
    {
      try
      {
        if (o.getValue() == null || o.getValue() == AnyNull.instance())
          _s.setNull(_paramIndex, Types.BLOB); // Can't tell blob/clob ?
        else
        {
          AnyBlob b = (AnyBlob)o;
          
//          InputStream is = b.sinkBlobStream();
//          _s.setBinaryStream (_paramIndex, is, is.available());

          // This is an alternative...
          byte[] ba = b.toByteArray();
          //_s.setObject(_paramIndex, ba);
          _s.setBytes(_paramIndex, ba);
        }
      }
//      catch(IOException i)
//      {
//        throw (new RuntimeContainedException(i));
//      }
      catch(SQLException s)
      {
        throw (new RuntimeContainedException(s));
      }
    }

		protected void unsupportedOperation (Any o)
		{
			_used = 0;
			//System.out.println("Skipping unknown param type " +
				//								 o.getClass().getName());
		}
  }

  static class ProcessColumnINTEGER extends AbstractFunc
  {
    private AnySql _anySql;
    private AnyInt _anyInt;
  
    ProcessColumnINTEGER (AnySql anySql)
    {
      _anySql = anySql;
      _anyInt = new AnyInt();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnINTEGER (_anyInt, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyInt);
    }
  }

  static class ProcessColumnSMALLINT extends AbstractFunc
  {
    private AnySql   _anySql;
    private ShortI _anyShort;
  
    ProcessColumnSMALLINT (AnySql anySql)
    {
      _anySql   = anySql;
      _anyShort = new AnyShort();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnSMALLINT (_anyShort, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyShort);
    }
  }

  static class ProcessColumnTINYINT extends AbstractFunc
  {
    private AnySql  _anySql;
    private ByteI   _anyByte;
  
    ProcessColumnTINYINT (AnySql anySql)
    {
      _anySql  = anySql;
      _anyByte = new AnyByte();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnTINYINT (_anyByte, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyByte);
    }
  }

  static class ProcessColumnBIGINT extends AbstractFunc
  {
    private AnySql  _anySql;
    private AnyLong _anyLong;
  
    ProcessColumnBIGINT (AnySql anySql)
    {
      _anySql = anySql;
      _anyLong = new AnyLong();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnBIGINT (_anyLong, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyLong);
    }
  }

  static class ProcessColumnREAL extends AbstractFunc
  {
    private AnySql   _anySql;
    private FloatI   _anyFloat;
  
    ProcessColumnREAL (AnySql anySql)
    {
      _anySql   = anySql;
      _anyFloat = new AnyFloat();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnREAL (_anyFloat, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyFloat);
    }
  }

  static class ProcessColumnDOUBLE extends AbstractFunc
  {
    private AnySql    _anySql;
    private AnyDouble _anyDouble;
  
    ProcessColumnDOUBLE (AnySql anySql)
    {
      _anySql    = anySql;
      _anyDouble = new AnyDouble();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnDOUBLE (_anyDouble, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyDouble);
    }
  }

  static class ProcessColumnDECIMAL extends AbstractFunc
  {
    private AnySql        _anySql;
    private Decimal       _anyBigDecimal;
  
    ProcessColumnDECIMAL (AnySql anySql)
    {
      _anySql        = anySql;
  
      // This is a working variable that is used to retrieve Decimal
      // columns.  The scale is arbitrary as it will be adjusted for
      // the column value in question.  Just set it now to put the
      // AnyBigDecimal into a suitable state (i.e. not the default value)
      _anyBigDecimal = new AnyBigDecimal("0.00");
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnDECIMAL (_anyBigDecimal, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyBigDecimal);
    }
  }

  static class ProcessColumnCHAR extends AbstractFunc
  {
    private AnySql    _anySql;
    private AnyString _anyString;
  
    ProcessColumnCHAR (AnySql anySql)
    {
      _anySql    = anySql;
      _anyString = new AnyString();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnCHAR (_anyString, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyString);
    }
  }

  static class ProcessColumnDATE extends AbstractFunc
  {
    private AnySql  _anySql;
    private DateI   _anyDate;
  
    ProcessColumnDATE (AnySql anySql)
    {
      _anySql  = anySql;
      _anyDate = new AnyDate();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnDATE (_anyDate, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyDate);
    }
  }

  static class ProcessColumnTIME extends AbstractFunc
  {
    private AnySql  _anySql;
    private DateI   _anyDate;
  
    ProcessColumnTIME (AnySql anySql)
    {
      _anySql  = anySql;
      _anyDate = new AnyDate();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnTIME (_anyDate, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyDate);
    }
  }

  static class ProcessColumnTIMESTAMP extends AbstractFunc
  {
    private AnySql  _anySql;
    private DateI   _anyDate;
  
    ProcessColumnTIMESTAMP (AnySql anySql)
    {
      _anySql  = anySql;
      _anyDate = new AnyDate();
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnTIMESTAMP (_anyDate, a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (_anyDate);
    }
  }

  static class ProcessColumnBLOB extends AbstractFunc
  {
    private AnySql  _anySql;
  
    ProcessColumnBLOB (AnySql anySql)
    {
      _anySql  = anySql;
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnBLOB(a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (a);
    }
  }

  static class ProcessColumnCLOB extends AbstractFunc
  {
    private AnySql  _anySql;
  
    ProcessColumnCLOB (AnySql anySql)
    {
      _anySql  = anySql;
    }
  
    public Any exec (Any a) throws AnyException
    {
      try
      {
        _anySql.processColumnCLOB(a);
      }
      catch (SQLException e)
      {
        throw (new ContainedException (e));
      }
      return (a);
    }
  }

  // Package classes for AnySql enumerated types
  static class ResultType
  {
    public static final int RESULT_NOMORE       = -1;
    public static final int RESULT_UPDATECOUNT  =  0;
    public static final int RESULT_ROWSAVAIL    =  1;
  
    int _resultType;
  
    ResultType (int resType) {_resultType = resType; }
  }

  static class MetaSpec
  {
    public static final int META_BASIC = 0;
    public static final int META_FULL  = 1;
  
    int _metaSpec;
  
    MetaSpec (int mSpec) {_metaSpec = mSpec; }
  }
}
