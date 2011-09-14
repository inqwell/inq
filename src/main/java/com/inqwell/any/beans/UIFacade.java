/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/UIFacade.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;

import com.inqwell.any.client.RenderInfo;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import javax.swing.border.Border;
import javax.swing.JComponent;

/**
 *
 */
public interface UIFacade extends Facade
{
  /**
   * Establish rendering information, including a node from
   * where this component gets its data from
   */
	public void setRenderInfo(RenderInfo r);

	public String getLabel();

	public JComponent getBorderee();
	public Object     getAddee();
	public Object     getAddIn();

	public void setEnabled(Any enabled);

	public void requestFocus();
}

