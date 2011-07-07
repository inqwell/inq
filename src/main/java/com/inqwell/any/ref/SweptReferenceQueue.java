/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/ref/SweptReferenceQueue.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:23 $
 */

package com.inqwell.any.ref;

import com.inqwell.any.server.cache.*;
import com.inqwell.any.Array;
import com.inqwell.any.AbstractComposite;

/**
 * A ReferenceQueue that starts its own thread to deal with objects
 * that have become garbage collected.
 */
public class SweptReferenceQueue extends AnyReferenceQueue
{
  static String cacheSweeper__      = "CacheSweeper";
  
  private static int    maxSweeperThreads__ = 5;
  private static int    lastIndex__         = 0;
  private static Array  queues__;
  
	private CacheSweeper  sweeper_;
  
  public static SweptReferenceQueue getSweptReferenceQueue()
  {
    synchronized(SweptReferenceQueue.class)
    {
      if (queues__ == null)
        queues__ = AbstractComposite.array();
      
      if (queues__.entries() < maxSweeperThreads__)
      {
        SweptReferenceQueue q = new SweptReferenceQueue();
        queues__.add(q);
        return q;
      }
      else
      {
        if (lastIndex__ == maxSweeperThreads__)
          lastIndex__ = 0;
        
        
        return (SweptReferenceQueue)queues__.get(lastIndex__++);
      }
    }
  }

  public SweptReferenceQueue()
  {
    sweeper_ = new CacheSweeper();
    sweeper_.setDaemon(true);
    sweeper_.setName(cacheSweeper__);
    sweeper_.start();
  }

//	public synchronized void processQueue()
//	{
//    CacheSoftReference csr;
//    while ((csr = (CacheSoftReference)this.pollAny()) != null)
//    {
//      Cache c = csr.getCache();
//      synchronized(c)
//      {
//        // the user data in the csr is the original key
//        c.remove (csr.getData());
//      }
//    }
//	}

  private class CacheSweeper extends Thread
	{
		private CacheSweeper ()
		{
			super ();
		}
		
		// Wait at the reference queue and remove any keys that come through
		public void run() 
		{
			try
			{
				CacheSoftReference csr;
        while (true)
        {
          csr = (CacheSoftReference)SweptReferenceQueue.this.removeAny();
          Cache c = csr.getCache();
          synchronized(c)
          {
            c.remove (csr.getData());
          }
        }
			}
			catch (InterruptedException e) {}
		}
	}

}
