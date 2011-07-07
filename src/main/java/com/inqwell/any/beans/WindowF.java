/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/WindowF.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;

import com.inqwell.any.*;
import java.awt.Component;

/**
 * Facade interface for Window objects.
 */
public interface WindowF extends UIFacade
{
	public void show(boolean withResize);
  public void hide();
  public void toFront();
	public void dispose(boolean disposeChildren);
  public Component getGlassPane();
	public WindowF getParentFrame();
  public boolean isBypassModality();
}

