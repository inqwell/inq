/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;

/**
 * Base class for components that view a single data value.
 * <p/>
 * A single {@link RenderInfo} is supported and its expression
 * defines the node(s) of interest for MVC.
 * 
 * @author tom
 */
public class AnySimpleComponent extends AnyComponent
{
  private   RenderInfo  renderInfo_;

//  public String getLabel()
//  {
//    if (renderInfo_ != null)
//    {
//      return renderInfo_.getLabel();
//    }
//    else
//    {
//      return null;
//    }
//  }
  
  /**
   * Provide information about the basic data this component is
   * rendering.
   * <p>
   * This method is suitable for components that render a single
   * item of data, such as labels and text fields.
   * The <code>RenderInfo</code> object carries the information
   * required to reference the data node,
   * as well as providing support for default component labelling.
   */
  public void setRenderInfo(RenderInfo r)
  {
    renderInfo_ = r;
    
    if (getContextNode() != null && r != null)
    {
      r.resolveNodeSpecs(getContextNode());
  
      // We must listen to the context node for events which will cause
      // us to render our data.
      setupDataListener(r.getNodeSpecs());
      
      // Try to render now.
      try
      {
        Any a = renderInfo_.resolveDataNode(getContextNode(), true);
        setValueToComponent(a);
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  }

  public void setEditable(boolean editable)
  {
    if (renderInfo_ == null)
      return;

    renderInfo_.setEditable(editable);
  }

  public boolean isEditable()
  {
    if (renderInfo_ == null)
      return false;

    return renderInfo_.isEditable();
  }

  public RenderInfo getRenderInfo()
  {
    return renderInfo_;
  }

  public Any getRenderedValue()
  {
    try
    {
      // Get the current rendered value, if any (for simple things)
      Any a = null;
      
      if (renderInfo_ != null)
        a = renderInfo_.resolveResponsibleData(getContextNode());
      
      return a;
    }
    catch(AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  /**
   * Default processing for data node events (as opposed to
   * adapted GUI events) which are received at this node.  This
   * implementation attempts to resolve the data node and
   * render it into the component.
   */
  protected void componentProcessEvent(Event e) throws AnyException
  {
    Any a = getGUIRendered(e);
    setValueToComponent(a);
  }

  protected void contextEstablished()
  {
    setRenderInfo(getRenderInfo());
  }
}
