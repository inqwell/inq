/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;

import com.inqwell.any.Any;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.EventListener;

public class AnySingleCDockable extends AnyCDockable
{
  public void addEventListener (EventListener l, Any eventParam)
  {
    super.addEventListener(l, eventParam);
    if (l == getParentAny())
    {
      // Adding to the node space. Find out our name and create the
      // dockable.
      // NOTE: moving a dockable around in the hierarchy and changing
      // its name in the process, or its dependent CControl, is not
      // supported at this time.
      
      AnyCControl c = AnyCControl.getCControl(this, true);
      DefaultSingleCDockable dockable = createDefaultSingleCDockable(getNameInParent());
      setObject(dockable);
      c.addDockable(this);
    }
  }
  
  /* (non-Javadoc)
   * @see com.inqwell.any.beans.WindowF#dispose(boolean)
   */
  @Override
  public void dispose(boolean disposeChildren)
  {
    AnyCControl c = AnyCControl.getCControl(this, true);
    c.removeDockable(this);    
  }

  private DefaultSingleCDockable createDefaultSingleCDockable(Any id)
  {
    // A DefaultSingleCDockable requires an id, which must be unique
    // amongst all DefaultSingleCDockables managed by a given CControl.
    // This means that we cannot actually create the DefaultSingleCDockable
    // when a statement of the form
    //   gSingleDock myDockable;
    // is executed, because we do not yet know its name in the hierarchy.
    // It is this name that is used as the DefaultSingleCDockable's id,
    // which may not be the same as myDockable.
    // Note this also means that properties relating to the DefaultSingleCDockable
    // cannot be set until after this time. This means that gSingleDocks
    // must be created as
    //
    //   gSingleDock $this.exposureMonitor  // example proper name
    //   $this.exposureMonitor.properties.title = "Exposure Monitor";
    //
    // or
    //
    //   gSingleDock myDockable;
    //     .
    //     .
    //   any $this.exposureMonitor = myDockable;
    //   myDockable.properties.title = "Exposure Monitor";
    //
    // This is not as bad as it seems. For other top level containers
    // it does not matter which way round this happens and for
    // dockables we need access to the AnyCControl, which we search
    // upwards in the hierarchy for, anyway.
    
   return new DefaultSingleCDockable(id.toString());
  }

  DefaultSingleCDockable getDefaultSingleCDockable()
  {
    CDockable d = getCDockable();
    
    if (d instanceof DefaultSingleCDockable)
      return (DefaultSingleCDockable)d;
    
    throw new AnyRuntimeException("Not a DefaultSingleCDockable");
  }
}
