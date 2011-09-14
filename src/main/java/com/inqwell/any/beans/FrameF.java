/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/FrameF.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;

import com.inqwell.any.*;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.client.AnyIcon;
import javax.swing.Icon;

/**
 * Facade interface for Window objects.
 */
public interface FrameF extends WindowF
{
	public void show(boolean withResize, AnyComponent relativeTo);
	public void setIconImage(AnyIcon icon);
	public Icon getIcon();
	public void setIcon(Icon i);
}

