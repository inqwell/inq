/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.ref;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.EventListener;

public class AnyWeakListener extends AnyWeakReference implements EventListener
{
  // We need to keep these so that if the weakly-held listener is cleared
  // this part of the contract can still be honoured.
  private Array eventTypes_;
  
  public AnyWeakListener(EventListener listener)
  {
    super(listener);
    eventTypes_ = listener.getDesiredEventTypes();
  }
  
  public Array getDesiredEventTypes()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean processEvent(Event e) throws AnyException
  {
    // TODO Auto-generated method stub
    return false;
  }

}
