/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JList.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:31:46 $
 */
package com.inqwell.any.client.swing;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.LocateNode;
import com.inqwell.any.client.AnyCellRenderer;
import com.inqwell.any.client.AnyList;
import com.inqwell.any.client.AnyListModel;
import com.inqwell.any.client.RenderInfo;

public class JList extends javax.swing.JList
{
  private AnyList list_;
 
  private static Any defaultProto__ = AbstractValue.flyweightString("mmmmmmmmmmmm");
  
	public JList(AnyListModel m)
	{
		super(m);
		init(m);
	}
	
	public JList()
	{
		super(new AnyListModel(LocateNode.null__));
		init((AnyListModel)getModel());
	}
	
  public void setAnyList(AnyList list)
  {
    list_ = list;
  }
  
  public AnyList getAnyList()
  {
    return list_;
  }
  
  public void reinit(AnyListModel m)
  {
    setModel(m);
    init(m);
  }
  
	private void init(AnyListModel m)
	{
		RenderInfo  r  = m.getRenderInfo();

		AnyCellRenderer ar = new AnyCellRenderer(r);
		this.setCellRenderer(ar);
    
		if (m.getPrototypeDisplayValue() != null)
      this.setPrototypeCellValue(m.getPrototypeDisplayValue());
		else
		  this.setPrototypeCellValue(defaultProto__);
		
		//this.setEditor(ar); tbd
	}
}
