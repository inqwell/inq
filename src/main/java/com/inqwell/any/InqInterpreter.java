/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/InqInterpreter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.parser.Inq;
import com.inqwell.any.Call.CallStackEntry;
import com.inqwell.any.client.AnyView;
import com.inqwell.any.client.swing.StyledDocument;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

public class InqInterpreter extends    AbstractAny
                            implements Interpreter
{
  private Inq     interpreter_;
  
  private boolean isBusy_;

  public Any run(Any root, AnyURL baseURL, Transaction t, InputStream i)
  {
    return run(root, baseURL, t, i, false);
  }
    
  public Any run(Any root, AnyURL baseURL, Transaction t, Reader r)
  {
    return run(root, baseURL, t, r, false);
  }

  public Any compile(Any root, AnyURL baseURL, Transaction t, InputStream i)
  {
    return run(root, baseURL, t, i, true);
  }
    
  public Any compile(Any root, AnyURL baseURL, Transaction t, Reader r)
  {
    return run(root, baseURL, t, r, true);
  }

  private Any run(Any root, AnyURL baseURL, Transaction t, InputStream i, boolean compile)
  {
    if (isBusy_)
    {
      // Like to push a new Inq stack entry here
//      if (!t.getCallStack().isEmpty())
//      {
//        CallStackEntry se = (CallStackEntry)t.getCallStack().peek();
//        se.setLineNumber(getLineNumber());
//      }
      InqInterpreter ii = new InqInterpreter();
      return ii.run(root, baseURL, t, i, compile);
    }
    
    isBusy_ = true;
    Any ret;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(i);

      if (compile)
        ret = interpreter_.compileInq(root, baseURL, t, i);
      else
        ret = interpreter_.runInq(root, baseURL, t, i);
    }
    finally
    {
      isBusy_ = false;
    }
    return ret;
  }
  
  private Any run(Any root, AnyURL baseURL, Transaction t, Reader r, boolean compile)
  {
    if (isBusy_)
    {
      InqInterpreter ii = new InqInterpreter();
      return ii.run(root, baseURL, t, r);
    }
    
    isBusy_ = true;
    Any ret;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(r);
      
      if (compile)
        ret = interpreter_.compileInq(root, baseURL, t, r);
      else
        ret = interpreter_.runInq(root, baseURL, t, r);
    }
    finally
    {
      isBusy_ = false;
    }
    return ret;
  }

  public Any layoutGui(Any         context,
                       Map         components,
                       Map         mInqRoot,
                       AnyView     awtRoot,
                       Any         rootName,
                       Transaction t,
                       Reader      r)
  {
    if (isBusy_)
    {
      InqInterpreter ii = new InqInterpreter();
      return ii.layoutGui(context, components, mInqRoot, awtRoot, rootName, t, r);
    }
    
    isBusy_ = true;
    Any ret;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(r);
      
      ret = interpreter_.layoutGui(context,
                                   components,
                                   mInqRoot,
                                   awtRoot,
                                   rootName,
                                   t,
                                   r);
    }
    finally
    {
      isBusy_ = false;
    }
    return ret;
 }
  
  public Any layoutGui(Any         context,
                       Map         components,
                       Map         mInqRoot,
                       AnyView     awtRoot,
                       Any         rootName,
                       Transaction t,
                       InputStream i)
  {
    if (isBusy_)
    {
      InqInterpreter ii = new InqInterpreter();
      return ii.layoutGui(context, components, mInqRoot, awtRoot, rootName, t, i);
    }
    
    isBusy_ = true;
    Any ret;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(i);
      
      ret = interpreter_.layoutGui(context,
                                   components,
                                   mInqRoot,
                                   awtRoot,
                                   rootName,
                                   t,
                                   i);
    }
    finally
    {
      isBusy_ = false;
    }
    return ret;
  }
  
  public void layoutDoc(Any            context,
                        StyledDocument doc,
                        Transaction    t,
                        InputStream    i)
  {
    if (isBusy_)
    {
      InqInterpreter ii = new InqInterpreter();
      ii.layoutDoc(context, doc, t, i);
      return;
    }
    
    isBusy_ = true;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(i);

      interpreter_.layoutDoc(context, doc, t, i);
    }
    finally
    {
      isBusy_ = false;
    }
  }
                       
  public void layoutDoc(Any            context,
                        StyledDocument doc,
                        Transaction    t,
                        Reader         r)
  {
    if (isBusy_)
    {
      InqInterpreter ii = new InqInterpreter();
      ii.layoutDoc(context, doc, t, r);
      return;
    }
    
    isBusy_ = true;
    try
    {
      if (interpreter_ == null)
        interpreter_ = new Inq(r);

      interpreter_.layoutDoc(context, doc, t, r);
    }
    finally
    {
      isBusy_ = false;
    }
  }
}
