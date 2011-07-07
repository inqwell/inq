/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/SetFocus.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import javax.swing.SwingUtilities;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.UIFacade;

/**
 * Attempt to set the focus to the specified component.
 * <p>
 * If the optional
 * second argument is present it is taken as a column name and the
 * component is assumed to be a table. There must be a selected row
 * and this together with the column defines a cell on which editing
 * is requested.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetFocus extends    AbstractFunc
											implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any ui_;

  private UIFacade rUi_;
  private Any      tableColumn_;
  
  private boolean postTransaction_ = false;
  
	public SetFocus(Any ui, Any tableColumn)
	{
		ui_ = ui;
    tableColumn_ = tableColumn;
	}
	
	public Any exec(Any a) throws AnyException
	{
		if (!postTransaction_)
		{
			getTransaction().addAction(this, Transaction.AFTER_EVENTS);
			postTransaction_ = true;
			rUi_    = (UIFacade)EvalExpr.evalFunc(getTransaction(),
																						a,
																						ui_,
																						UIFacade.class);
      
      if (rUi_ == null)
        nullOperand(ui_);
	
			Any tableColumn    = EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          tableColumn_);
      
      if (tableColumn == null && tableColumn_ != null)
        nullOperand(tableColumn_);
      
      tableColumn_ = tableColumn;
      
			return rUi_;
		}

    if (rUi_ != null)
    {
      final UIFacade lUi = rUi_;
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          //System.out.println("DISPOSING!");
          // custom disposal - remove us from our parent
          lUi.requestFocus();
        }
      });
    }
    
    if (tableColumn_ != null && rUi_ instanceof AnyTable)
    {
      AnyTable t = (AnyTable)rUi_;
      if (!t.setEditColumn(tableColumn_))
        throw new AnyException("Cannot start editing column " + tableColumn_);
    }
		
		UIFacade ret = rUi_;
	  rUi_ = null;
    tableColumn_ = null;

	  return ret;	
	}
	
  public Object clone () throws CloneNotSupportedException
  {
    SetFocus s = (SetFocus)super.clone();
    
    s.ui_           = ui_.cloneAny();
    s.tableColumn_  = AbstractAny.cloneOrNull(tableColumn_);
    
    return s;
  }
	
}
