/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/RenderInfo.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * Definition for rendering support of GUIs
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public interface RenderInfo extends Any
{
	/**
	 * Establish the fully qualified name of the descriptor to
	 * be used to assist with rendering
	 */
	public void setFQName(String fQName) throws AnyException;

	/**
	 * Establish the field to be used to assist with rendering
	 */
	public void setField(String field);

	/**
	 * Set whether data being rendered is also editable
	 */
	public void setEditable(boolean editable);

	/**
	 * Return editable status
	 */
	public boolean isEditable();

	/**
	 * Determine if we are capable of building the data node and the path
	 * to it.
	 */
	public boolean isBuildable();
	/**
	 * Return <code>true</code> if we are rendering an Enum
	 */
	public boolean isEnum();

	/**
	 * Provide a data leaf if rendering input data rather than
	 * output data
	 */
	public void setData(Any data);

	/**
	 * Support on-the-fly data creation for dynamic tables
	 */
	public Any buildData(Map root) throws AnyException;

	/**
	 * Provide a label for the rendered information if no descriptor is
	 * given or to override same
	 */
	public void setLabel(String label);
	
	/**
	 * Provide a preferred rendering width if no descriptor is
	 * given or override same.
	 */
	public void setWidth(int width);

	/**
	 * Provide a format pattern for the rendered information if no
	 * descriptor is given or to override same.
	 */
	public void setFormat(String f);

	/**
	 * Attempt to return the prevailing format string.  If there is
   * no descriptor and no explicit format string has been
   * established then <code>null</code> will be returned.
	 */
	public String getFormatString();

	/**
	 * If set to true then the rendered value will be
	 * evaluated for all event types, specifically,
	 * including updates.
	 */
	public void setAlwaysEvaluate(boolean b);
	
	/**
	 * Establish the data to render.  This can be an expression
	 * which will be evaluated.
	 */
	public void setDataNode(Any dataNode);
	
	/**
	 * Establish sub-rendering information when a list of
	 * editable items is desired
	 */
	public void setEditingList(AnyListModel list);

	/**
	 * Fetch the data node expression.
	 */
	public Any getDataNode();

  public Any getValueExpression();
  
  public NodeSpecification getRenderPath();
  
  public void setRenderPath(NodeSpecification path);
  
  /**
	 * Provide an optional expression for the actual data node this
	 * RenderInfo is responsible for.  If provided as well as that
	 * to setDataNode then will be used to build data for on-the-fly
	 * data model creation.
	 */
	public void setResponsibleData(Locate data);

  /**
   * Return rendering label
   */
	public String getLabel();
  
  /**
   * Return the default label when a specific override was provided.
   */
  public String getDefaultLabel();


  /**
   * Return rendering width
   */
	public int getWidth();

	public Any resolveDataNode(Any root, boolean force) throws AnyException;

  /**
   * Request the data to be rendered.  An expression may be evaluated
   * from the given root.  Caching the result is supported via
   * the <code>force</code> parameter.
   * TODO: build arg is no longer used - to be removed
   */
	public Any resolveDataNode(Any root, boolean force, boolean build) throws AnyException;

	public Any resolveDataNode(Any root, boolean force, boolean build, Transaction t) throws AnyException;
  
	/**
	 * Return the primary data we are rendering.
	 */
	public Any resolveResponsibleData(Any root) throws AnyException;

	public AnyFormat getFormat(Any a);

  public boolean isDispatching(Any field);

  public Descriptor getDescriptor();

	public Any getField();

  //public void setStyle(AnyAttributeSet style);

  //public AnyAttributeSet getStyle();

  /**
	 * If our editing possibilities are constrained to a list
	 * of items then return the <code>RenderInfo</code> for this.
	 * Otherwise return <code>null</code>
	 */
	public AnyListModel getEditingList();

  /**
   * Support for events.  Generate the node specifications that this
   * object would require re-evaluation for from node events.
   * The map will contain the node specifications as keys and the field
   * set as values.
   */
	public void resolveNodeSpecs(Map nodeSpecs, Any contextNode);

	public void resolveNodeSpecs(Any contextNode);
	
  /**
   * Support for events.  Return the node specifications that this
   * object would require re-evaluation for from node events.
   * The map contains the node specifications as keys and the field
   * set as values.
   */
	public Map getNodeSpecs();
}
