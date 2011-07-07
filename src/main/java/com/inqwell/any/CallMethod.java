/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/CallMethod.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Call methods on objects and classes external to the Any framework.
 * This class supports the invocation of static or instance methods
 * on classes and object instances outside the <code>Any</code>
 * framework.
 * <p>
 * Whether static or instance, a method may return a value
 * implementing the <code>Any</code> interface or be declared
 * <code>void</code>. Methods can take any number of arguments
 * but these must also implement the <code>Any</code> interface.
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 * @see com.inqwell.any.Any
 */ 
public class CallMethod extends    AbstractFunc
						            implements Cloneable
{
	private Any     methodName_;
	private Any     classOrInstance_;
	private Any     args_;
	private boolean isInstance_;
	
	private boolean passTransaction_ = false;
	
	/**
	 * Call the given method name on the specified class or instance
	 * with the supplied arguments. If <code>isInstance</code> is
	 * <code>true</code> then the method is an instance method and
	 * <code>classOrInstance</code> represents the instance on
	 * which the method will be called. Otherwise the method is a
	 * static method and <code>classOrInstance</code> represents
	 * the fully qualified class name.
	 * @param methodName the name of the method to call
	 * @param classOrInstance either the instance on which the
	 * method call will be made or the fully qualified class name
	 * in the case of a static method
	 * @param args the arguments to be supplied.  All arguments must
	 * implement the <code>Any</code> interface.
	 * @param inInstance <code>true</code> if calling an instance
	 * method, <code>false</code> if calling a static method.
	 */
	public CallMethod (Any     methodName,
	                   Any     classOrInstance,
	                   Array   args,
	                   boolean isInstance)
	{
		methodName_      = methodName;
		classOrInstance_ = classOrInstance;
		args_            = args;
		isInstance_      = isInstance;
	}
	
	/**
	 * Call a class constructor.
	 * @param targetClass the fully qualified class name
	 * @param args the arguments to be supplied.  All arguments must
	 * implement the <code>Any</code> interface.
	 */
	public CallMethod (Any   targetClass,
	                   Array args)
	{
		this(null, targetClass, args, false);
	}
	
  public Any exec (Any a) throws AnyException
  {
		Any methodName      = EvalExpr.evalFunc(getTransaction(),
																					  a,
																					  methodName_);

		Any classOrInstance = EvalExpr.evalFunc(getTransaction(),
																					  a,
																					  classOrInstance_);
		if (classOrInstance == null)
		  nullOperand("classOrInstance");

		Array args          = (Array)EvalExpr.evalFunc(getTransaction(),
																								   a,
																								   args_,
																								   Array.class);

  	// Evaluate any arguments optionally adding event/transaction
  	args = evalArgs(a, args);
  	//System.out.println("CallMethod args " + args);
    int numArgs = (args != null) ? args.entries() : 0;

    try
    {
	    // Determine the target class. If we represent a static
	    // method then classOrInstance is a class name so try
	    // to load it. Otherwise classOrInstance is an instance
	    // so determine the class from that.
	
	    Class targetClass = null;
	    if (isInstance_)
	      targetClass = classOrInstance.getClass();
	    else
	      targetClass = Class.forName(classOrInstance.toString());
	    
	    // Search for the desired method

	    BeanInfo bi = Introspector.getBeanInfo(targetClass);
	    boolean methodFound = false;
	    int     i           = 0;

      // Check for methods first
	    Any    ret = null;
	    if (methodName != null)
	    {
		    Method m   = null;
		    MethodDescriptor[] md = bi.getMethodDescriptors();
		    for (i = 0; i < md.length; i++)
		    {
		    	m = md[i].getMethod();
		    	
		      //System.out.println(m);
		    	// name match
		    	if (!methodName.toString().equals(m.getName()))
		    	  continue;
		    	  
		    	int modifiers = m.getModifiers();
		    	
		    	// Limit to public methods
		    	if (!Modifier.isPublic(modifiers))
		    	  continue;
		    	  
		    	// Static/instance selection
		    	if ((isInstance_ && Modifier.isStatic(modifiers)) ||
		    	    (!isInstance_ && !Modifier.isStatic(modifiers)))
		    	  continue;
		    	
		    	Class params[] = m.getParameterTypes();
		    	
		    	// Correct number of args
		    	if (params.length != numArgs)
		    	  continue;
		    	
		    	// All args must be of type Any
		    	if (numArgs != 0 && argsNotAnys(params))
		    	  continue;
		    	
		    	// Return type must be of type Any or null
		    	//if (retNotAny(m.getReturnType()))
		    	  //continue;
		    	
		    	methodFound = true;
		    	break;
		    }
	
		    if (methodFound)
		    {
		    	// All ok - invoke method
		    	//System.out.println("CallMethod invoking " + m + " with " + args);
		    	Object o  = m.invoke(isInstance_ ? classOrInstance : null,
		    	                     args.toArray());
		    	if (o instanceof Any)
		    	  ret = (Any)o;
		    	else
		    	  ret = new AnyObject(o);
		    }
        else
          throw new AnyException("Method " + methodName + " not found on class " + targetClass);
	    }
      else
	    {
	    	// Look for ctor
		    	
	    	Constructor[] ctors = targetClass.getConstructors();
	    	Constructor c = null;
		    for (i = 0; i < ctors.length; i++)
		    {
		    	c = ctors[i];
		    	
		    	int modifiers = c.getModifiers();
		    	
		    	// Limit to public methods (actually all are public)
		    	if (!Modifier.isPublic(modifiers))
		    	  continue;
		    	  
		    	Class params[] = c.getParameterTypes();
		    	
		    	// Correct number of args
		    	if (params.length != args.entries())
		    	  continue;
		    	
		    	// All args must be of type Any
		    	if (args.entries() != 0 && argsNotAnys(params))
		    	  continue;
		    	
		    	methodFound = true;
		    	break;
		    }
	
		    if (methodFound)
		    {
		    	// All ok - invoke method
	        if (Any.class.isAssignableFrom(targetClass))
		    	  ret  = (Any)c.newInstance(args.toArray());
	        else
	          ret = new AnyObject(c.newInstance(args.toArray()));
		    }
	    	else
	    	{
	    	  throw new AnyException("No suitable ctor found on " + classOrInstance);
	    	}
	    }
	    
			return ret;
    }
    catch (InvocationTargetException e)
    {
      Throwable t = e.getCause();
      if (t != null)
        throw new ContainedException(t);
      else
        throw new ContainedException(e);
    }
    catch (Exception e)
    {
      throw new ContainedException(e);
    }
  }
  
  /**
   * If set to <code>true</code> the
   * current <code>Transaction</code> object is passed to
   * the called method as the first argument, or the second
   * argument if <code>setEvent</code> has been called, and any
   * supplied arguments are passed thereafter.
   * <p>
   * Defaults to <code>false</code>.
   */
  public void setPassTransaction(boolean passTransaction)
  {
  	passTransaction_ = passTransaction;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(methodName_);
  	a.add(classOrInstance_);
  	a.add(args_);
  	return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    CallMethod c = (CallMethod)super.clone();
    
    c.methodName_      = AbstractAny.cloneOrNull(methodName_);
    c.classOrInstance_ = classOrInstance_.cloneAny();
    c.args_            = args_.cloneAny();

    return c;
  }
  
  /**
   * The <code>CallMethod</code> function is considered to be
   * a mutating operation on the target instance.  If the
   * <code>CallMethod</code> object is used to make instance
   * method calls and the user object is held within an Inq
   * transactional container then that container will be entered
   * into the transaction.  The user object's <code>clone()</code>
   * method will be called.
   */
  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
  	if (isInstance_)
  	{
		  if (a.isTransactional())
		  {
		  	Map m = (Map)a;
		  	Transaction t = getTransaction();
		  	t.copyOnWrite(m);
		    a = t.getTransInstance(m);
		  }
  	}
  	else
  	{
  		a = super.doTransactionHandling(root, a);
  	}
	
	  return a;
  }

  private boolean argsNotAnys(Class[] params)
  {
  	for (int i = 0; i < params.length; i++)
  	{
  		if (!Any.class.isAssignableFrom(params[i]))
  		  return true;
  	}
  	return false;
  }
  
  private boolean retNotAny(Class retType)
  {
  	if (retType == void.class || Any.class.isAssignableFrom(retType))
  	  return false;
  	else
  	  return true;
  }
  
  private Array evalArgs(Any root, Array args) throws AnyException
  {
  	args = args.shallowCopy();
  	
  	for (int i = 0; i < args.entries(); i++)
  	{
			Any arg      = EvalExpr.evalFunc(getTransaction(),
																		   root,
																		   args.get(i));
		  args.replaceItem(i, arg);
  	}
  	
  	if (passTransaction_)
  	  args.add(0, getTransaction());
  	  
  	return args;
  }
  
  public static Any callMethodTest(Any a)
  {
    return a;
  }

  public static java.awt.Dimension callMethodReturnsObject()
  {
    return new java.awt.Dimension(300,200);
  }
}
