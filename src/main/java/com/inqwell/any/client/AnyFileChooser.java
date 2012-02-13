/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyFileChooser.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-05-02 20:10:30 $
 */

package com.inqwell.any.client;

import java.awt.Container;
import java.io.File;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFile;
import com.inqwell.any.AnyFileFilter;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.Event;
import com.inqwell.any.Func;
import com.inqwell.any.Set;
import com.inqwell.any.Transaction;
import com.inqwell.any.Vectored;
import com.inqwell.any.beans.SelectionF;
import com.inqwell.any.client.swing.JFileChooser;

public class AnyFileChooser extends    AnyComponent
														implements SelectionF
{
	private static Set      fcProperties__;
  private static Any      filters__        = AbstractValue.flyweightString("filters");
  private static Any      directory__      = AbstractValue.flyweightString("directory");

	private JFileChooser  f_;
	private Array         selection_;
  
  static
  {
    fcProperties__ = AbstractComposite.set();
    fcProperties__.add(filters__);
    fcProperties__.add(directory__);
	}

// TODO can we set initial value(s) programatically ?
	
  public AnyFileChooser()
  {
		selection_ = AbstractComposite.array();
		this.replaceItem (selection__, selection_);
	}

	/**
	 * Called when file(s) are selected.  The action event from the underlying
	 * <code>JFileChooser</code> is solicited to capture the file selection
	 * and make it available as <code>{this-component-name}.selection</code>
	 * like list and table selections do.
	 * <p>
	 * Its still OK to attach other listeners to this component as the
	 * internal one is always called first so the selection list is
	 * always up to date.
	 */
	public void newSelection(Event e)
	{
		selection_ = AbstractComposite.array();
		
		File selected[] = f_.getSelectedFiles();
		if ((selected != null) && (selected.length > 0))
		{
			for (int i = 0; i < selected.length; i++)
			{
				selection_.add(new AnyFile(selected[i]));
			}
		}
		else
		{
			File single = f_.getSelectedFile();
			if (single != null)
			{
				selection_.add(new AnyFile(single));
			}
		}
		this.replaceItem (selection__, selection_);
	}
	
	/**
	 * Retrieve the current selection as a list of objects
	 */
	public Any getItemSelection()
	{
		return selection_;
	}
	
	public void setItemSelection(Any a)
	{
		// tbd
	}
	
  public Container getComponent()
  {
    return f_;
  }

	public void setSelectionMode(int selectionInterval)
	{
		if (selectionInterval == MakeComponent.multiIntervalSelect__)
		{
			f_.setMultiSelectionEnabled(true);
		}
		else
		{
			f_.setMultiSelectionEnabled(false);
		}
	}
	
  public void setFilters(Any filters)
  {
    // Assumes a vector of AnyFileFilters or AnyNull
    f_.resetChoosableFileFilters();

    if (filters == AnyNull.instance())
      return;
    
    Vectored v = (Vectored)filters;
		for (int i = 0; i < v.entries(); i++)
		{
      AnyFileFilter aff = (AnyFileFilter)v.getByVector(i);
      f_.addChoosableFileFilter(aff.inqFileFilter());
		}
  }
  
  public void setDirectory(AnyFile dir)
  {
    if (!dir.getFile().isDirectory())
      throw new AnyRuntimeException("Not a directory: " + dir);
    
    f_.setCurrentDirectory(dir.getFile());
  }
  
	public void setObject(Object o)
	{
		if (!(o instanceof JFileChooser))
			throw new IllegalArgumentException
									("AnyFileChooser wraps com.inqwell.any.client.swing.JFileChooser and sub-classes");
		
		
		f_ = (JFileChooser)o;

		super.setObject(f_);

		// Tell base class about our spoof dialog events generator
		setupEventSet(f_.getFileChooserGenerator());
	}

	protected Object getPropertyOwner(Any property)
	{
		if (fcProperties__.contains(property))
		  return this;
		
		return super.getPropertyOwner(property);
	}
	
	protected void initUpdateModel()
	{
		addAdaptedEventListener(new ActionListener(actionEventType__));
	}

	protected Object getAttachee(Any eventType)
	{
		if (eventType.equals(ListenerConstants.FILECHOOSER))
			return f_.getFileChooserGenerator();
		else
			return super.getAttachee(eventType);
	}
	
  private class ActionListener extends EventBinding
  {
    public ActionListener(Array eventTypes)
    {
      super(eventTypes, false);
    }

		protected Any execExpr(Transaction t, Any context, Func expr, Event e) throws AnyException
		{
			newSelection(e);
			return null;
		}
  }
}
