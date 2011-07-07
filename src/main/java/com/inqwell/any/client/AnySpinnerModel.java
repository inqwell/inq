/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnySpinnerModel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;
import javax.swing.AbstractSpinnerModel;
import com.inqwell.any.client.swing.SwingInvoker;

/**
 * A model relating anys to the SpinnerModel interface.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class AnySpinnerModel extends AbstractSpinnerModel
{
  public static Any  spinnerValue__ = new AnyString("current");
  private Any        context_;

  private RenderInfo renderInfo_;

  // Expressions to yield the next and previous values.
  private Call       nextValue_;
  private Call       prevValue_;
  
  private Map        fnArgs_;
  
  static
  {
    // If we create spinners then compile the default next
    // and previous value functions
    try
    {
      ExecInq execInq = new ExecInq(new AnyURL("classpath:///com/inqwell/any/tools/Spinner.inq"));
      execInq.setTransaction(Globals.process__.getTransaction());
  
      execInq.exec(AbstractComposite.simpleMap());
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
	public AnySpinnerModel(Any contextNode)
	{
    setContext(contextNode);
    init();
	}

	public AnySpinnerModel(Any contextNode, RenderInfo r)
	{
    setContext(contextNode);
    setRenderInfo(r);
    init();
	}

  // From SpinnerModel

  public Object getNextValue()
  {
    return callValueFunc(nextValue_);
  }

  public Object getPreviousValue() 
  {
    return callValueFunc(prevValue_);
  }

  public Object getValue()
  {
    if (renderInfo_ == null)
      return null;

    Any dataNode = null;
    
    try
    {
      dataNode = renderInfo_.resolveDataNode(context_, false);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }

    return dataNode;
  }

  public Any getResponsibleValue()
  {
    Any ret = null;
    
    try
    {
      ret = renderInfo_.resolveResponsibleData(context_);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    
    return ret;
  }
  
  public void setValue(Object value)
  {
    if (renderInfo_ == null)
      return;

    try
    {
      Any vs = renderInfo_.resolveResponsibleData(context_);
      if (vs == null)
        return;
      
      vs.copyFrom((Any)value);
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
    fireStateChanged();
  }
  
  public void setNextValueExpr(Call next)
  {
    nextValue_ = next;
  }
  
  public void setPrevValueExpr(Call prev)
  {
    prevValue_ = prev;
  }

  void translateEvent(Event e) throws AnyException
  {
		Map id = (Map)e.getId();

		Any eventType = id.get(EventConstants.EVENT_TYPE);

		if (eventType.equals(EventConstants.BOT_UPDATE))
		{
      renderInfo_.resolveDataNode(context_, false);
		}
		else
		{
      boolean notDeleting = !(eventType.equals(EventConstants.NODE_REMOVED) ||
                              eventType.equals(EventConstants.NODE_REMOVED_CHILD));
      renderInfo_.resolveDataNode(context_, true, notDeleting);
		}
    fireStateChanged();
  }
  
	void setContext(Any context)
	{
		context_ = context;
	}

  void setRenderInfo(RenderInfo r)
	{
		renderInfo_ = r;
    
    createExpressions();
	}
  
  private Any callValueFunc(Call f)
  {
    Any ret = null;
    
    if (f != null)
    {
      Transaction t = Globals.process__.getTransaction();

      try
      {
        Any curr = renderInfo_.resolveResponsibleData(context_);
        if (curr == null)
          return null;

        fnArgs_.add(spinnerValue__, curr);
        f.setArgs(fnArgs_);
        f.setTransaction(t);
        ret = f.exec(context_);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        f.setArgs(null);
        fnArgs_.remove(spinnerValue__);
        f.setTransaction(null);
      }
    }
    return ret;
  }
  
  private void createExpressions()
  {
    // Create default expressions based on the type of the
    // data node if none already specified
    if (nextValue_ == null && prevValue_ == null)
    {
      nextValue_ = new Call(new LocateNode("$catalog.system.util.spinner.exprs.nextValue"));
      prevValue_ = new Call(new LocateNode("$catalog.system.util.spinner.exprs.prevValue"));
    }
  }
  
  private void init()
  {
    fnArgs_ = AbstractComposite.simpleMap();
  }
  
  /*
  private class MakeExpression extends AbstractVisitor
  {
    public void visitAnyBoolean (AnyBoolean b)
    {
      unsupportedOperation (b);
    }
  
    public void visitAnyByte (AnyByte b)
    {
      unsupportedOperation (b);
    }
  
    public void visitAnyChar (AnyChar c)
    {
      unsupportedOperation (c);
    }
  
    public void visitAnyInt (AnyInt i)
    {
      unsupportedOperation (i);
    }
  
    public void visitAnyShort (AnyShort s)
    {
      unsupportedOperation (s);
    }
  
    public void visitAnyLong (AnyLong l)
    {
      unsupportedOperation (l);
    }
  
    public void visitAnyFloat (AnyFloat f)
    {
      unsupportedOperation (f);
    }
  
    public void visitAnyDouble (AnyDouble d)
    {
      unsupportedOperation (d);
    }
  
    public void visitAnyString (AnyString s)
    {
      unsupportedOperation (s);
    }
  
    public void visitAnyDate (AnyDate d)
    {
      unsupportedOperation (d);
    }
  
    public void visitMap (Map m)
    {
      unsupportedOperation (m);
    }
  
    public void visitArray (Array a)
    {
      unsupportedOperation (a);
    }
  
    public void visitFunc (Func f)
    {
      unsupportedOperation (f);
    }
  
    public void visitAnyObject (AnyObject o)
    {
      unsupportedOperation (o);
    }
    
    public void visitUnknown(Any o)
    {
      unsupportedOperation (o);
    }
  
  }
  */
}
