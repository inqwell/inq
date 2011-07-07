/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/GuiAttacher.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * A function which attaches 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GuiAttacher extends    AbstractFunc
                         implements Cloneable
{
	private Any     attachTo_;
	private Func    expr_;
	private Array   eventTypes_;
	private Any     dialogRedirect_;
	private boolean consume_;
	private boolean busy_;
  private Any     modelFires_;
	
	public GuiAttacher (Any     attachTo,
	                    Func    expr,
	                    Array   eventTypes,
	                    Any     dialogRedirect,
	                    boolean consume,
                      boolean busy,
                      Any     modelFires)
	{
		attachTo_       = attachTo;
		expr_           = expr;
		eventTypes_     = eventTypes;
		dialogRedirect_ = dialogRedirect;
		consume_        = consume;
		busy_           = busy;
    modelFires_     = modelFires;
	}

	/**
	 * 
	 */
	public Any exec(Any a) throws AnyException
	{
    AnyView attachTo = (AnyView)EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  attachTo_,
                                                  AnyView.class);

    if (attachTo == null)
      nullOperand(attachTo_);
    
    // Only applies for non-dialog redirects anyway
    Any modelFires = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       modelFires_);

    if (modelFires == null && modelFires_ != null)
      nullOperand(modelFires_);
    
    AnyView.EventBinding eb = null;
    
    if (dialogRedirect_ != null && (dialogRedirect_.equals(MakeComponent.dialogOk__) ||
                                    dialogRedirect_.equals(MakeComponent.dialogCancel__)))
    {
      attachTo.addAdaptedEventListener(eb = attachTo.makeDialogRedirect
                                                   (dialogRedirect_,
                                                    eventTypes_,
    	                                              consume_,
    	                                              busy_));
    }
    else if (dialogRedirect_ != null && dialogRedirect_.equals(EventConstants.CELLEDITOR_STOPPED))
    {
      attachTo.addAdaptedEventListener(eb = attachTo.makeEditorRedirect
                                                  (dialogRedirect_,
                                                      eventTypes_,
                                                      consume_,
                                                      busy_));
    }
    else
    {
      // Just a normal event binding
    	Func f = (Func)(expr_ != null ? expr_.cloneAny() : null);
      boolean fireModel = false;
      if (modelFires != null)
      {
        BooleanI b = new ConstBoolean(modelFires);
        fireModel = b.getValue();
      }
    	attachTo.addAdaptedEventListener(eb = attachTo.makeEventBinding
    	                                             (f,
    	                                              eventTypes_,
    	                                              consume_,
    	                                              busy_,
                                                    fireModel));

    }
    
    // Return the EventBinding object so that the script can later
    // remove it, if desired.
    return eb;
	}
	
  public Object clone() throws CloneNotSupportedException
  {
  	GuiAttacher g = (GuiAttacher)super.clone();
  	
  	g.attachTo_   = (Func)attachTo_.cloneAny();
  	g.modelFires_ = AbstractAny.cloneOrNull(modelFires_);
  	
    return g;
  }
}
