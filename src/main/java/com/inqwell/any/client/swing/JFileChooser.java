/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JFileChooser.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:30:59 $
 */
package com.inqwell.any.client.swing;

import com.inqwell.any.Any;
import com.inqwell.any.AnyFileFilter;
import com.inqwell.any.Globals;
import java.util.Iterator;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * An extension of JFileChooser which defines the necessary
 * methods to notify its wrapper of approve, cancel etc
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class JFileChooser extends javax.swing.JFileChooser
{
	private NotifyFileChooser notifyFileChooser_;
	
	public void approveSelection()
	{
		super.approveSelection();
		NotifyFileChooser n = getFileChooserGenerator();
		n.fireFileChooserApprove(new FileChooserEvent(n,
																									ActionEvent.ACTION_PERFORMED,
																									"APPROVE"));
	}
	
	public void cancelSelection()
	{
		super.cancelSelection();
		NotifyFileChooser n = getFileChooserGenerator();
		n.fireFileChooserCancel(new FileChooserEvent(n,
																								 ActionEvent.ACTION_PERFORMED,
																								 "CANCEL"));
	}
	
	// Override file chooser to synchronize on Globals.process__.
  // There are thread safety issues here as well.  See
  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4770133.
  // Don't know if we are always on a separate thread, as opposed to
  // the awt event thread, so can't know whether full txn stuff
  // should be done here.  Just have to wait and see how much
  // of a problem that is.
  public boolean accept(File f)
  {
    synchronized(Globals.process__)
    {
      boolean haveProcessForThread = Globals.haveProcessForThread();
      
      if (!haveProcessForThread)
        Globals.setProcessForThread(Thread.currentThread(), Globals.process__);

      try
      {
        boolean b = super.accept(f);
        
        return b;
      }
      finally
      {
        if (!haveProcessForThread)
          Globals.removeProcessForThread(Thread.currentThread(), Globals.process__);
      }
    }
  }
  
	public NotifyFileChooser getFileChooserGenerator()
	{
		if (notifyFileChooser_ == null)
			notifyFileChooser_ = new NotifyFileChooser();
			
		return notifyFileChooser_;
	}

	public static class NotifyFileChooser
	{
		ArrayList listeners_ = new ArrayList();
		
		public void addFileChooserListener(FileChooserListener l)
		{
			listeners_.add(l);
		}

		public void removeFileChooserListener(FileChooserListener l)
		{
			int i = -1;
			if ((i = listeners_.indexOf(l)) >= 0)
				listeners_.remove(i);
		}
		
		public void fireFileChooserApprove(FileChooserEvent e)
		{
			Iterator i = listeners_.iterator();
			while (i.hasNext())
			{
				FileChooserListener l = (FileChooserListener)i.next();
				l.fileChooserApprove(e);
			}
		}
		
		public void fireFileChooserCancel(FileChooserEvent e)
		{
			Iterator i = listeners_.iterator();
			while (i.hasNext())
			{
				FileChooserListener l = (FileChooserListener)i.next();
				l.fileChooserCancel(e);
			}
		}
	}
}
