/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JSplitPane.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.client.MakeComponent;

/**
 * An extension of JSplitPane to support the axis property.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class JSplitPane extends javax.swing.JSplitPane
{
  public JSplitPane()
  {
    super(VERTICAL_SPLIT);
    setResizeWeight(0.5);
  }

  public void setAxis(Any axis)
  {
    if (axis.equals(MakeComponent.X_AXIS))
      setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    else
      setOrientation(JSplitPane.VERTICAL_SPLIT);
  }
  
  public void parserAxis(int axis)
  {
    if (axis == JPanel.X_AXIS)
      setOrientation(JSplitPane.HORIZONTAL_SPLIT);
    else
      setOrientation(JSplitPane.VERTICAL_SPLIT);
  }
  
  public Any getAxis()
  {
  	return null;
  }
  
}
