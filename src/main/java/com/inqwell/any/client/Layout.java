/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Layout.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.Transaction;
import com.inqwell.any.Call;
import com.inqwell.any.StringI;
import com.inqwell.any.ConstString;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.Map;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Globals;
import com.inqwell.any.Interpreter;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.client.swing.SwingInvoker;
import java.io.Reader;
import java.io.StringReader;

/**
 * Layout a collection of GUI components
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Layout extends    AbstractFunc
                    implements Cloneable
{
	private Any components_;
	private Any inqRoot_;
	private Any awtRoot_;
	private Any rootName_;
	private Any layoutString_;
	
	static Any layout__      = new ConstString("<layout>");

  public Layout(Any components, Any inqRoot, Any layoutString)
	{
		this(components, inqRoot, null, null, layoutString);
	}
	
	public Layout(Any components, Any inqRoot, Any awtRoot, Any rootName, Any layoutString)
	{
		components_   = components;
		inqRoot_      = inqRoot;
		awtRoot_      = awtRoot;
		rootName_     = rootName;
		layoutString_ = layoutString;
	}
	
	public Any exec(Any a) throws AnyException
	{
		final Map components    = (Map)EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               components_,
                                               Map.class);
    if (components == null)
      throw new AnyException("Could not resolve components");

		final Map    mInqRoot   = (Map)EvalExpr.evalFunc(getTransaction(),
                                                   a,
                                                   inqRoot_,
                                                   Map.class);

    if (mInqRoot == null)
      throw new AnyException("Could not resolve inq root node " + inqRoot_);

		final AnyView awtRoot   = (AnyView)EvalExpr.evalFunc(getTransaction(),
                                                   a,
                                                   awtRoot_,
                                                   AnyView.class);

    if (awtRoot == null && awtRoot_ != null)
      throw new AnyException("Could not resolve awt root node " + awtRoot_);

		final StringI rootName  = (StringI)EvalExpr.evalFunc(getTransaction(),
                                                   a,
                                                   rootName_,
                                                   StringI.class);

    if (rootName == null && rootName_ != null)
      throw new AnyException("Could not resolve explicit root name " + rootName_);

		final StringI layoutString = (StringI)EvalExpr.evalFunc(getTransaction(),
                                                          a,
                                                          layoutString_,
                                                          StringI.class);
    if (layoutString == null)
      throw new AnyException("Could not resolve layout string");

    // Validate args
    if (awtRoot == null && (!(mInqRoot instanceof AnyView)))
      throw new AnyException("Common inq/awt root, class " + mInqRoot.getClass() + " must be an AnyView");

  	//final Object oAwtRoot = (awtRoot != null) ? awtRoot.getAddIn()
  	//                                          : inqRoot.getAddIn();

    Any ret = null;
    Reader rr = null;
    
    try
    {
      final Reader r     = new StringReader(layoutString.toString());
      rr = r;
      final Any    lroot = a;
  
      // Push a new Inq stack frame.  The parser sets the line number
      // in the transaction as the layout is parsed so that any stack
      // trace should hopefully be close to the point of the error in
      // the layout.
      Transaction t = getTransaction();
      Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
      int curLine = se.getLineNumber();
      se.setLineNumber(t.getLineNumber());
      t.getCallStack().push(new Call.CallStackEntry(se.getSourceUrl(), this.layout__));
  
      // If we are a client then invoke on the swing thread,
      // otherwise go ahead here.  [Surely we must always be a client? Ed.]
      if (Globals.process__ != null)
      {
        SwingInvoker ss = new SwingInvoker()
        {
          protected void doSwing()
          {
            Globals.interpreter__.layoutGui(lroot,
                                            components,
                                            mInqRoot,
                                            awtRoot,
                                            rootName,
                                            getTransaction(),
                                            r);
          }
        };
  
        ss.maybeSync();
      }
      else
      {
        Interpreter intr = new InqInterpreter();
        ret = intr.layoutGui(lroot,
                             components,
                             mInqRoot,
                             awtRoot,
                             rootName,
                             getTransaction(),
                             r);
      }
      
      t.getCallStack().pop();
      se.setLineNumber(curLine);
      
      return ret;
    }
    catch (Exception e)
    {
      throw new ContainedException(e);
    }
    finally
    {
      if (rr != null)
      {
        try
        {
          rr.close();
        }
        catch (Exception e) {}
      }
    }
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    Layout l = (Layout)super.clone();
    
    l.components_   = components_.cloneAny();
    l.inqRoot_      = inqRoot_.cloneAny();
    l.layoutString_ = layoutString_.cloneAny();
    l.awtRoot_      = AbstractAny.cloneOrNull(awtRoot_);
    l.rootName_     = AbstractAny.cloneOrNull(rootName_);
    
    return l;
  }
	
}
