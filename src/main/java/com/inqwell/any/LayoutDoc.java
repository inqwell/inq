/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LayoutDoc.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.client.AnyDocument;
import com.inqwell.any.client.swing.SwingInvoker;
import java.io.Reader;
import java.io.StringReader;

/**
 * Layout a document template in a StyledDocument
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class LayoutDoc extends    AbstractFunc
                    implements Cloneable
{
  private Any layoutString_;
  private Any doc_;
  
  static Any layoutDoc__      = new ConstString("<layoutdoc>");

  public LayoutDoc(Any doc, Any layoutString)
	{
    doc_          = doc;
    layoutString_ = layoutString;
	}
	
	public Any exec(Any a) throws AnyException
	{
		final AnyDocument doc    = (AnyDocument)EvalExpr.evalFunc(getTransaction(),
                                               a,
                                               doc_,
                                               AnyDocument.class);

		final StringI layoutString = (StringI)EvalExpr.evalFunc(getTransaction(),
                                                          a,
                                                          layoutString_,
                                                          StringI.class);

    Reader rr = null;
	  try
	  {      
      final Reader r       = new StringReader(layoutString.toString());
      rr = r;
      final Any    context = a;

      // Push a new Inq stack frame.  The parser sets the line number
      // in the transaction as the layout is parsed so that any stack
      // trace should hopefully be close to the point of the error in
      // the layout.
      Transaction t = getTransaction();
      Call.CallStackEntry se = (Call.CallStackEntry)t.getCallStack().peek();
      int curLine = se.getLineNumber();
      se.setLineNumber(t.getLineNumber());
      t.getCallStack().push(new Call.CallStackEntry(se.getSourceUrl(), this.layoutDoc__));

      // If we are a client then invoke on the swing thread,
      // otherwise go ahead here.
      if (Globals.process__ != null)
      {
        SwingInvoker ss = new SwingInvoker()
        {
          protected void doSwing()
          {
            Globals.interpreter__.layoutDoc(context,
                                            doc.getStyledDocument(),
                                            getTransaction(),
                                            r);
          }
        };

        ss.maybeSync();
      }
      else
      {
        Interpreter intr = new InqInterpreter();
        intr.layoutDoc(a,  // context
                       doc.getStyledDocument(),
                       getTransaction(),
                       r);
      }
      
      t.getCallStack().pop();
      se.setLineNumber(curLine);
      
      return doc;
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
    LayoutDoc l = (LayoutDoc)super.clone();
    
    l.doc_          = doc_.cloneAny();
    l.layoutString_ = layoutString_.cloneAny();
    
    return l;
  }
	
}
