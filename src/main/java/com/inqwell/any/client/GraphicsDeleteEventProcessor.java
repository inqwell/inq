/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import javax.swing.SwingUtilities;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.RuntimeContainedException;

public class GraphicsDeleteEventProcessor extends DeleteEventProcessor
{

  public GraphicsDeleteEventProcessor()
  {
    super();
  }

  public GraphicsDeleteEventProcessor(Any eventType)
  {
    super(eventType);
  }

  public GraphicsDeleteEventProcessor(Any eventType, Any root)
  {
    super(eventType, root);
  }

  public boolean processEvent(final Event e) throws AnyException
  {
    if (SwingUtilities.isEventDispatchThread())
    {
      exec(e);
    }
    else
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          try
          {
            exec(e);
          }
          catch(Exception ee)
          {
            throw new RuntimeContainedException(ee);
          }
        }
      });
    }
    return true;
  }
}
