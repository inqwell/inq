/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyFuncHolder.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.ref.AnyWeakReference;

/**
 * This class holds a function making it possible to pass functions as
 * arguments
 * in <code>Inq</code> scripts without executing them.
 * Normally, functions are evaluated as part of the process of
 * yielding operands for other functions.  However, sometimes it
 * is desirable to pass functions themselves.  In this case the
 * function must be protected from evaluation inside
 * an <code>AnyFuncHolder</code>
 * <p>
 * A function so contained is executed by
 * the <code>ExecFuncHolder</code> class
 */
public class AnyFuncHolder extends    AbstractFunc
													 implements Cloneable
{
  private static final long serialVersionUID = 1L;

  public static AnyFuncHolder null__ = new AnyFuncHolder();
  public static Any xfunc__ = new ConstString("xfunc");
  private static AnyWeakReference dummyContext__ = new AnyWeakReference(AbstractComposite.simpleMap());
	
	private Func       f_;
	private FuncHolder fh_;
  private Any        baseURL_;
  private Any        fqName_;

  // Means preserve the context in which the func was declared
  // so that it can be established when it is executed.  This
  // is not always what is required, so make it optional.
  private boolean    preserveContext_ = true;

	public AnyFuncHolder(Func    f,
                       boolean preserveContext,
                       Any     name,
                       Any     url)
	{
		f_ = f;
    preserveContext_ = preserveContext;
    setFQName(name);
    setBaseURL(url);
	}
	
	public AnyFuncHolder() {}

	public Func getFunc()
	{
		return f_;
	}
	
	public void setFunc(Func f)
	{
		f_ = f;
	}
	
  public void setBaseURL(Any url)
  {
    baseURL_ = url;
  }

  public Any getBaseURL()
  {
    return baseURL_;
  }

  public Any getFQName()
  {
    return fqName_;
  }
  
  public void setFQName(Any fqName)
  {
    fqName_ = fqName;
  }
  
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (a instanceof AnyFuncHolder)
			{
				AnyFuncHolder f = (AnyFuncHolder)a;
				this.setFunc(f.getFunc());
			}
			else if (a instanceof AnyFuncHolder.FuncHolder)
			{
				fh_ = (AnyFuncHolder.FuncHolder)a;
			}
			else
			{
				throw new IllegalArgumentException("AnyFuncHolder.copyFrom(): " + a.getClass());
			}
		}
    return this;
  }
  
  public Any exec(Any a) throws AnyException
  {
		// Create the actual func holder and return that, saving the
		// current context node, execution node (in case different)
		// and context path. They can then be restored when the
		// encapsulated func is executed by ExecFuncHolder, which may
		// well be running in some other context.
		if (fh_ == null)
			fh_ = new FuncHolder(getTransaction(),
                           f_,
                           a,
                           preserveContext_,
                           getFQName(),
                           getBaseURL());
		else
			fh_.setup(getTransaction(),
                f_,
                a,
                preserveContext_,
                getFQName(),
                getBaseURL());
		
		return fh_;
  }

  public Object clone() throws CloneNotSupportedException
  {
		// We don't bother to clone the function yet.  See ExecFuncHolder
    return super.clone();
  }
  
  public static class FuncHolder extends    AnyObject
                                 implements Value,
                                            Cloneable
  {
		private static final long serialVersionUID = 1L;
		
		private transient AnyWeakReference  context_;        // Map
		private           Any               contextPath_;
		private transient AnyWeakReference  execNode_;       // Any
    
    // Remember the url and a name for the function.
    private           Any  url_;
    private           Any  name_;
    
    // f_ is now the underlying object, now that we are an AnyObject
		// private           Func f_;
		
		// Create a FuncHolder that stores the current context
		// node, execution node and context path.
		private FuncHolder(Transaction t,
		                   Func        f,
		                   Any         a,
		                   boolean     preserveContext,
                       Any         name,
                       Any         url)
		{
			setup(t, f, a, preserveContext, name, url);
		}
		
		// Create a context-less FuncHolder. When the function is
		// executed the current context, execution node and context
		// path will be used.
		public FuncHolder(Func f)
		{
      this.setValue(f);
      name_ = xfunc__;
    }
    
		private void setup(Transaction t,
                       Func        f,
                       Any         a,
                       boolean     preserveContext,
                       Any         name,
                       Any         url)
		{
      if (preserveContext)
      {
        context_     = new AnyWeakReference(t.getContext());
        contextPath_ = t.getContextPath();
        execNode_    = new AnyWeakReference(a);
      }
      //System.out.println("FUNCHOLDER CONTEXT " + contextPath_);
			this.setValue(f);
      
      if (name == null)
        name_ = xfunc__;
      else
        name_ = name;
      
      url_  = url;
		}
		
		public Func getFunc()
		{
      return (Func)this.getValue();
    }
    
    public Any getUrl()
    {
      return url_;
    }
    
    public Any getName()
    {
      return name_;
    }
    
    public void setUrl(Any url)
    {
      url_ = url;
    }
    
    public void setName(Any name)
    {
      name_ = name;
    }
    
    // Only for exception handling when an exception handling
    // function is called.
    public void setContext(Map context)
    {
      context_  = new AnyWeakReference(context);
      execNode_ = new AnyWeakReference(context);
    }
    
    // Only for formal function arguments. See parser.
    public void setFuncParam()
    {
      // We don't want arbitrary context nodes and paths etc lying around in
      // FuncHolders that are sitting in the formal parameter definitions
      // of functions. These type of func folders will always be copied to
      // when the function is called because a func is a pass-by-value
      // argument type. Thus, they will take on the context (if any) of
      // the value being passed.
      context_     = dummyContext__;
      contextPath_ = null;
      execNode_    = null;
    }
    
    // Only for exception handling when an exception handling
    // function is called.
    public Any getContextPath()
    {
      return contextPath_;
    }
    
    public boolean isNull()
    {
      return getValue() == null;
    }
    
    public void setNull()
    {
      setValue(null);
    }
    
    public boolean isConst()
    {
      return false;
    }
  
    public Any bestowConstness()
    {
      return this;
    }
    
    public boolean isViable()
    {
      boolean ret = true;
      
      if (context_ != null && context_ != dummyContext__)
      {
        Any a = context_.getAny();
        if (a != null && a instanceof InstanceHierarchyMap)
        {
          InstanceHierarchyMap m = (InstanceHierarchyMap)a;
          if (m.getOwnerProcess() == null)
            ret = false;
        }
      }
      
      return ret;
    }
    
		public Any copyFrom (Any a)
		{
      if (AnyNull.isNullInstance(a))
        this.setNull();
      else if (a != null && a != this)
			{
				if (!(a instanceof FuncHolder))
					throw new IllegalArgumentException("AnyFuncHolder.FuncHolder.copyFrom(): " + a.getClass());
				
				FuncHolder fh = (FuncHolder)a;
        
        // Copy over the function. It's cloned when/if it is executed
        this.setValue(fh.getValue());
				
				// A bit messy but if we are a context-less FuncHolder
				// then don't copy these fields.  Makes typedef func
				// fields (which don't require a context) work by
				// not acquiring context which could be setup if such
				// a field is modified by running script.
        // 20070805: In fact, prevent a func from becoming a
        // cfunc by copying, and vice-versa
				//if (context_ != null && fh.context_ != null)
        if (context_ != null)
				{
          context_      = fh.context_;
          contextPath_  = fh.contextPath_;
          execNode_     = fh.execNode_;
        }
			}
			return this;
		}
		
		public Any doFunc(Transaction t, Map args, Any callingContext) throws AnyException
		{
			AnyWeakReference  context     = context_;
			Any               contextPath = contextPath_;
      AnyWeakReference  execNode    = execNode_;
      
      Any  a           = (Any)this.getValue();
			Func f           = a != null ? (Func)a.cloneAny() : null;
			
			if (f == null)
			  return null;
			
//			context_     = null;
//			contextPath_ = null;
//			execNode_    = null;
//			f_           = null;

			Any ret = null;
			Map mContext;
			Any aExec;
			
      // if we are a context-less func holder (like those
      // stored as typedef fields) then use current
      if (context == null)
      {
        aExec       = callingContext;
        mContext    = t.getContext();
        if (mContext == null)
          mContext = (Map)callingContext; // hopefully only gets the case when a listener is using a cfunc 
        contextPath = t.getContextPath();
      }
      else
      {
        // Try to retrieve weak refs. If anything is gc then can't continue
        mContext = (Map)context.getAny();
        if (execNode != null)
          aExec = execNode.getAny();
        else
          aExec = null;
        if (/*aExec == null ||*/ mContext == null)
          return ret;
      }
			
			Map oldContext = t.getContext();
			Any oldContextPath = t.getContextPath();
			
			try
			{
				if (f != null)
				{
          t.getProcess().setContext(mContext);
          t.getProcess().setContextPath(contextPath);
          
					ret = Call.call(f, args, aExec, t, null, this);
          
          // Hmmm we need this here, or better inside the call?
          // This is only really necessary for naked funcs.
          if (ret != null && ret.isTransactional())
            ret = f.doTransactionHandling(aExec, ret);

				}
			}
			finally
			{
				if (f != null)
				{
          t.getProcess().setContext(oldContext);
          t.getProcess().setContextPath(oldContextPath);
				}
			}
			
			return ret;
		}
  
		public Object clone() throws CloneNotSupportedException
		{
			// We don't bother to clone the function yet - it is cloned above
			// in doFunc (if we are ever actually executed, i.e. as lazy
			// as possible).
			return super.clone();
		}
  }
}
