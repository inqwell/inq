/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Call.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:20 $
 * @version  $
 * @see 
 */

package com.inqwell.any;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.inqwell.any.util.CommandArgs;

/**
 * Call a target function passing optional arguments.  Manage the
 * call stack
 */
public class Call extends    AbstractFunc
									implements Cloneable
{
  private static final long serialVersionUID = 1L;
  private static final Any none__ = new ConstString("none");
  
  private Locate  func_;
  private Any     args_;
	private Event   event_;
  
  // Once we have located the function for the first time
  // hold on to it.  If the function gets reparsed then
  // we will automatically relocate.  Note therefore, that
  // this member is not cloned, of course.
  private AbstractInputFunc cachedFunc_;
  
  private Any callingURL_;
  
  static private Any    args__    = new ConstString("args");
  static private Any    arrays__  = new ConstString("arrays");
  //static private String none__  = new ConstString("none");

  private static     LogManager lm = LogManager.getLogManager();
  private static     Logger l = lm.getLogger("inq");
  
  private static     Map loggedPackages__;
  
  static
  {
  	synchronized(Call.class)
  	{
  		if (loggedPackages__ == null)
  			loggedPackages__ = AbstractComposite.simpleMap();
  	}
  }
  
  /**
   * Add a logged package. Packages are mapped to function or service
   * names. Note these are separate name spaces so a function and
   * a service with the same name will both be logged or not.
   * 
   * If the entity is AnyAlwaysEquals.instance() then all package
   * entities will be logged.
   * 
   * Adding something to the logged list causes input arguments
   * and return value to be logged to the "inq" logger.
   * 
   * @param pkg
   * @param entity
   */
  static public void addLoggedEntity(Any pkg, Any entity)
  {
  	synchronized(loggedPackages__)
  	{
  		if (AnyAlwaysEquals.isAlwaysEquals(pkg))
  		{
  			loggedPackages__.empty();
  			loggedPackages__.add(AnyAlwaysEquals.instance(), AnyAlwaysEquals.instance());
  			return;
  		}
  		
  		if (AnyAlwaysEquals.isAlwaysEquals(entity))
  			loggedPackages__.replaceItem(pkg, entity);
  		else
  		{
  			Any a = loggedPackages__.getIfContains(pkg);
  			Set s = null;
  			
  			if (a instanceof Set)
  				s = (Set)a;
  			else
  				loggedPackages__.remove(pkg);
  			
    		if (s == null)
    		{
    			s = AbstractComposite.set();
      		s.add(entity);
      		loggedPackages__.add(pkg, s);
    		}
    		else
    		{
    			if (!s.contains(entity))
    				s.add(entity);
    		}
  		}
  	}
  }
  
  static public void removeLoggedEntity(Any pkg, Any entity)
  {
  	synchronized(loggedPackages__)
  	{
  		if (AnyAlwaysEquals.isAlwaysEquals(pkg))
  			loggedPackages__.empty();
  		else
  		{
    		if (AnyAlwaysEquals.isAlwaysEquals(entity))
    			loggedPackages__.remove(pkg);
    		else
    		{
    			Any a = loggedPackages__.getIfContains(pkg);
    			Set s = null;
    			
    			if (a instanceof Set)
    				s = (Set)a;

      		if (s != null)
      			s.remove(entity);
      		else
      			loggedPackages__.remove(pkg);
    		}
  		}
   	}
  }
  
  static public boolean isLogged(Any pkg, Any entity)
  {
  	// Not bothering to sync on read...
  	Any e = loggedPackages__.getIfContains(pkg);
  	if (e == null)
  		return false;
  	
  	if (AnyAlwaysEquals.isAlwaysEquals(e))
  		return true;
  	
  	return (((Set)e).contains(entity));
  }

  static public Any call(Call c, Map args)
  {
    Any ret = null;
    try
    {
      Process p = Globals.getProcessForCurrentThread();
      if (args != null)
        c.setArgs(args);
      c.setTransaction(p.getTransaction());
      ret = c.exec(p.getContext());
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      if (args != null)
        c.setArgs(null);
      c.setTransaction(null);
    }
    return ret;
  }
  
  public static Any call (Func                     f,
													Map                      args,
													Any                      root,
													Transaction              t,
                          AbstractInputFunc        af) throws AnyException
	{
		return call(f, args, root, t, af, null);
	}
	
	public static Any call (Func                     f,
													Map                      args,
													Any                      root,
													Transaction              t,
													AbstractInputFunc        af,
                          AnyFuncHolder.FuncHolder fh) throws AnyException
	{
    Map stack      = null;
    boolean logged = false;
    
    // If there are no args and no scripted function being called then
    // there's no need to create a new stack frame.  This allows
    // scripted (c)func objects that carry immediate script to have
    // access to whatever is on the stack now.  See things like
    // Aggregate and GroupBy that use AnyFuncHolder.FuncHolder.doFunc().
    if (args != null || af != null)
      stack = t.pushStackFrame();
		
		if (args != null)
		{
			// 'push' the arguments
	    Iter i = args.createKeysIterator();
	    while (i.hasNext())
	    {
	      Any k = i.next();
	      Any a = args.get(k);
	      if (a != null)
	        stack.add (k, a);
	    }
		}
		
		Any ret = null;
		short currentPrivilegeLevel = t.getProcess().getEffectivePrivilegeLevel();
		
    int lineNumber = t.getLineNumber();
    int column     = t.getColumn();
    Any execURL    = t.getExecURL();
    
		try
		{
      // This puts the line number of the target function f into
      // the transaction as the current line number.  We've saved it
      // above so we can restore it after the call.
			f.setTransaction(t);
			if (af != null)
			{
        // Make a new stack entry. The top-most entry doesn't carry any
			  // line number until CallStackEntry.setLineNumber is called, for
        // example in ExceptionI.fillInCallStack()
			  t.getCallStack().push(new CallStackEntry(af.getBaseURL(), af.getFQName()));
			  t.getProcess().setEffectivePrivilegeLevel(af.getEffectivePrivilegeLevel());
        Any funcURL = af.getBaseURL();
        // Bit flakey but see LoginOK.init() for an explanation
        if (funcURL != null)
          t.setExecURL(funcURL);

        logged = isLogged(af.getPackage(), af.getName());
  	    if (logged)
  	    {
  	    	// Log args going in
  	    	l.log(Level.INFO, "Entering {0} args: {1}", new Object[] {af.getFQName(), args});
  	    }

      }
      else
      {
        // There will be a funcholder. Make a call stack entry for
        // an xfunc if the funcholder contains name and url.
        Any funcUrl  = fh.getUrl();
        Any funcName = fh.getName();
        if (funcName != null)
        {
          t.getCallStack().push(new CallStackEntry(funcUrl, funcName));
          
          if (funcUrl != null)
            t.setExecURL(funcUrl);
        }
      }
			  
			ret = f.execFunc(root);
      
      t.setLineNumber(lineNumber);
      t.setColumn(column);
      t.setExecURL(execURL);
      
			if (af != null || fh != null)
      {
			  t.getCallStack().pop();

			  if (logged)
  	    {
  	    	// Log return coming out
  	    	l.log(Level.INFO, "Leaving {0} ret: {1}", new Object[] {af.getFQName(), args});
  	    }
      }
		}
		catch(ReturnException re)
		{
      t.setLineNumber(lineNumber);
      t.setColumn(column);
      t.setExecURL(execURL);
      if (af != null)
      {
        t.getCallStack().pop();
      }
			ret = re.getResult();
		}
		finally
		{
      if (stack != null)
        t.popStackFrame();
        
      t.getProcess().setEffectivePrivilegeLevel(currentPrivilegeLevel);
		}
		
		return ret;
	}
	
	/**
	 * Call the given func with a new stack frame passing the given
	 * args.  The supplied arguments will override any default
	 * values defined by the target function.
	 */
  public Call(Locate func, Any callingURL, Any args)
  {
    func_       = func;
    callingURL_ = callingURL;
    args_       = args;
  }

	/**
	 * Call the given func without arguments. If the target function
	 * defines arguments then their default values will apply.
	 */
  public Call(Locate func, Any callingURL)
  {
		this (func, callingURL, null);
  }

  public Call(Locate func)
  {
		this (func, null, null);
  }

  public Any exec(Any a) throws AnyException
  {
		Map args = (Map)EvalExpr.evalFunc(getTransaction(),
																			a,
																			args_,
																			Map.class);
		
  	// We don't want to execute the function we will call, we
  	// just want to reach it.
		AbstractInputFunc func = null;
    func = locateFunc(a, getTransaction());
      
    if (func == null)
      throw new AnyException("Could not resolve function " + func_);
      
    // Note that this is the original fn from the catalog. We want
    // to set the flag in here....
    func.startUse();
    
    try
    {
      
      // Check to see if there are any parameter definitions
      // ... these methods are reentrant ...
      Map m = func.buildArgs();
      m = passParams(m, args, a);
  
      if (!getTransaction().getCallStack().isEmpty())
      {
        CallStackEntry se = (CallStackEntry)getTransaction().getCallStack().peek();
        se.setLineNumber(getLineNumber());
      }
      
      return Call.call((Func)func.cloneAny(),
                       m,
                       a,
                       getTransaction(),
                       func);
    }
    finally
    {
      func.endUse();
    }
  }
  
  /**
   * Set the arguments o be passed by this <code>Call</code> object.
   */
  public void setArgs(Any args)
  {
    args_ = args;
  }
  
  public Locate getFunc()
  {
    return func_;
  }

  public Map getArgs()
  {
    if (args_ instanceof Map)
      return (Map)args_;
    
    return null;
  }
  
  public Any getLocalReference()
  {
    return callingURL_;
  }

  /**
   * Used to set an event that will be passed to the called
   * function.  The object passed must be an event.
   */
  public void setParam(Any a)
  {
  	setEvent((Event)a);
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer("call ");
    sb.append(func_.toString());
    if (args_ != null)
    {
      sb.append(" ");
      sb.append(args_);
    }
    return sb.toString();
  }
  
  /**
   * Whether this Call statement is equal to another.
   * Returns <code>true</code> if <code>this</code> references
   * the same target function as <code>a</code>.
   */
  public boolean equals(Any a)
  {
    if (this == a)
      return true;
      
		if (!(a instanceof Call))
      return false;
      
    Call c = (Call)a;
    
    return func_.equals(c.func_);
  }
  
	public int hashCode()
	{
		return func_.hashCode();
	}

  /**
   * Create an iterator which traverses the operands
   * of the <code>Call</code> instance.  In the case
   * of <code>Call</code> this method attempts to resolve
   * the target function in case it is a node reference,
   * which is the most common case.  This allows code
   * that wishes to traverse the operands of a root
   * expression not to 'skip over' those targetted by
   * a call reference
   */
  public Iter createIterator ()
  {
  	// Try to resolve the target function
  	AbstractInputFunc func = null;
  	try
  	{
			func = locateFunc(Catalog.instance().getCatalog(), getTransaction());
  	}
  	catch (Exception e)
  	{
  		throw new RuntimeContainedException(e);
  	}
  	
  	Array a = AbstractComposite.array();
  	
  	// if the func doesn't exist then don't iterate over it
  	if (func != null)
  	  a.add(func);
  	a.add(args_);
  	return a.createIterator();
  }

  /**
   * Resolve the function only.  Does not execute the target
   * function but try to locate it.
   * @return the target functon or <code>null</code> if not found.
   */
  public AbstractInputFunc resolveFunc(Any         root,
                                       Transaction t) throws AnyException
  {
    AbstractInputFunc func = locateFunc(root, t);
	  return func;
  }
  
  /**
   * Resolve the arguments of this <code>Call</code> instance
   * and return their values.  Useful if the object will be
   * executed in another context from where the arguments should
   * be resolved, for example starting a spawned process.
   * @return the resolved arguments or <code>null</code> if
   * this instance does not pass arguments.
   */
  public Map resolveArgs(Any         root,
                         Func        f,
                         Transaction t) throws AnyException
  {
    if (f == null)
      return null;
    
    Map defaults = f.buildArgs();

    Map args = (Map)EvalExpr.evalFunc(t,
                                      root,
                                      args_,
                                      Map.class);
																			
	  return passParams(defaults, args, root);
  }

  public Object clone () throws CloneNotSupportedException
  {
    Call c = (Call)super.clone();
    
    c.func_ = (Locate)func_.cloneAny();
    c.args_ = AbstractAny.cloneOrNull(args_);
    
    return c;
  }

  public void setEvent(Event event)
  {
  	event_ = event;
  }
  
  protected Any afterExecute(Any ret, Any a)
  {
    // Leave anything left in the transaction by the operand for the
    // calling function to pick up.
    return ret;
  }
  
  private AbstractInputFunc locateFunc(Any a, Transaction t) throws AnyException
  {
  	AbstractInputFunc func = null;
    do
    {
      if (cachedFunc_ == null)
      {
        // If there is a callingURL then check that first
        // to implement a local function.
        NodeSpecification s        = null;
        int               localIdx = -1;
        if (callingURL_ != null)
        {
          s = func_.getNodePath();
          s = (NodeSpecification)s.shallowCopy();
          localIdx = s.entries()-1;
          s.add(localIdx, callingURL_);
          func_.setNodePath(s);
        }
        
        func = (AbstractInputFunc)EvalExpr.evalFunc
                                         (t,
                                          a,
                                          func_,
                                          AbstractInputFunc.class,
                                          Locate.class);
        // If we didn't find the local func then try the
        // global one
        if (func == null && s != null)
        {
          s.remove(localIdx);
          func_.reset();
          func = (AbstractInputFunc)EvalExpr.evalFunc
                                           (t,
                                            a,
                                            func_,
                                            AbstractInputFunc.class,
                                            Locate.class);
        }
        
        cachedFunc_ = func;
      }
      else
      {
        func = cachedFunc_;
        if (func.isDefunct())
          cachedFunc_ = null;
      }
    }
    while (func != null && func.isDefunct());
    
    return func;
  }
  
  private Map passParams(Map   defaults,
                         Map   args,
                         Any   context) throws AnyException
  {  
		if (defaults != null)  // its always non-null, but may be empty.
		{
			// as the function describes some arguments we set up any
			// overrides supplied
			if (args != null && args.entries() != 0)
			{
				Iter i = args.createKeysIterator();
				while (i.hasNext())
				{
					Any pn = i.next();
					if (defaults.contains(pn))
						passParameter(defaults, args, pn, context, getTransaction());
				}
			}
			
			if (event_ != null)
      {
        defaults.replaceItem (EventConstants.EVENT_ID, event_.getId());
        defaults.replaceItem (EventConstants.EVENT, event_);
        Any component = event_.getIfContains(EventConstants.component__);

        Any ec = event_.getContext();
        if (ec != null)
        {
          // When the event carries a context item add this to the stack
          defaults.add(EventConstants.EVENT_CONTEXT, ec);
        }
        if (component != null)
          defaults.replaceItem(EventConstants.EVENT_COMPONENT, component);
      }
		}
	  return defaults;
  }
  
  static public void passParameter(Map target,
																	 Map source,
																	 Any pName,
																	 Any root,
																	 Transaction t) throws AnyException
  {
		Any sourceArg = source.get(pName);

		Any targetArg = target.get(pName);
		
		sourceArg = EvalExpr.evalFunc(t,
																	root,
																	sourceArg);
		
    sourceArg = t.readProperty(sourceArg);

    // Special handling of txn map private instances - see
    // LocalTransaction.copyOnWrite()
    /*
    if (sourceArg instanceof Map)
    {
      Map m  = (Map)sourceArg;
      Any tm = m.getUniqueKey();
      if (tm != null && tm.isTransactional())
        sourceArg = tm;
    }
*/

    // Check if there is information in the transaction that says
    // we would be aliasing a private map instance. We don't want
    // this to happen
    if (t.getResolving() == Transaction.R_MAP)
    {
      sourceArg = t.getLastTMap();
    }

    if (sourceArg == null)
		  target.remove(pName);
		else
		{
			if (targetArg instanceof Value)
				targetArg.copyFrom(sourceArg);
			else
      {
        // Check for field ripping pass by reference
        if (t.getResolving() == Transaction.R_FIELD)
        {
          sourceArg = t.getLastTField();
          if (sourceArg != null)
            sourceArg = sourceArg.bestowConstness();
        }

				target.replaceItem(pName, sourceArg);
      }
		}
  }
  
  static public void setupCommandLineArgs(String      scriptUrl,
                                          String      args[],
                                          Map         argsMap,
                                          CommandArgs cArgs,
                                          Transaction t)
  {
    Array aArgs = AbstractComposite.array();
    
    aArgs.add(new AnyString(scriptUrl));
    
    boolean shebang = argsMap.contains(CommandArgs.shebang__);
    
    for (int i = 0; i < args.length; i++)
    {
      // when in shebang mode skip everything until -shebang
      // and its value have been seen
      if (!shebang)
      {
        if (args[i].equals(CommandArgs.minusIn__))
        {
          i++;
          continue;
        }

        AnyString s = new AnyString(args[i]);
        aArgs.add(s);
      }
      else
      {
        if (args[i].equals(CommandArgs.minusShebang__.toString()))
        {
          i++;
          shebang = false;
        }
      }
    }
    
    Map stack = t.getCurrentStackFrame();
    
    stack.replaceItem(args__,    aArgs);
    argsMap.replaceItem(arrays__, cArgs);
    stack.replaceItem(CommandArgs.commandLine__, argsMap);
  }
  
  static public class CallStackEntry extends AbstractAny
  {
    private static final long serialVersionUID = 1L;

    private Any sourceUrl_;
  	private Any fqName_;
  	private int lineNumber_;
  	
  	public CallStackEntry(Any sourceUrl, Any fqName)
  	{
      if (sourceUrl == null)
        sourceUrl = none__;
      
  		sourceUrl_ = sourceUrl;
  		fqName_    = fqName;
  	}
  	
  	public void setLineNumber(int lineNumber)
  	{
  	  if (lineNumber > 0)
  		  lineNumber_ = lineNumber;
  	}
  	
    public int getLineNumber()
    {
      return lineNumber_;
    }
    
    public Any getSourceUrl()
    {
      return sourceUrl_;
    }
    
    public Any getFQName()
    {
      return fqName_;
    }
    
  	public String toString()
  	{
      StringBuffer sb = new StringBuffer(sourceUrl_.toString());
      sb.append(' ');
      sb.append(fqName_.toString());
      sb.append('(');
      sb.append(lineNumber_);
      sb.append(')');
      return sb.toString();
      /*
  		return sourceUrl_.toString() + " " + 
  		       fqName_.toString() + 
  		       "(" + lineNumber_ + ")";
             */
  	}
  }
}
