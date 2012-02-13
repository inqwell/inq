/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyRenderInfo.java $
 * $Author: sanderst $
 * $Revision: 1.9 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.Catalog;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.FieldNotFoundException;
import com.inqwell.any.Globals;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.Process;
import com.inqwell.any.Set;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.Visitor;

/**
 * A simple helper class to hold rendering information.  Instances
 * of this class are set up by configuration tools and may be used
 * by the GUI rendering process to create much of the GUI itself.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.9 $
 */
public class AnyRenderInfo extends    NodeFunction
                           implements RenderInfo,
                                      Map,
                                      Cloneable
{
	// If we are rendering a typedef field or using a typedef to
  // act as a provider of formatting/labeling information
	// then these will be set up to the descriptor and the field.
	//private StringI  fQName_;
	private Any        field_;

	private boolean    editable_       = false;
	private boolean    alwaysEvaluate_ = false;
	private Any        label_;
	private int        width_          = -1;
  
  // Any explicitly provided format string (that may override
  // that contained in a typedef field).
	private Any        fmtStr_;
  
  // Only non-null if we processed some format directives, so that
  // we can pass on to underlying Java formatters outside the Inq
  // environment (in particular JDateChooser)
  private String     javaFormatStr_;
  
	private AnyFormat  formatter_;

	private Descriptor d_;
	
	private AnyListModel editList_;

	private Locate     responsibleData_;

	// This is the result of executing the above expression.
	private Any        resolvedDataNode_;

  // Any supporting information for additional rendering.  This
  // may be shared amongst other AnyRenderInfo objects. It is an
  // expression that is resolved at the same time as our rendered
  // data.
  //private AnyAttributeSet style_;
	
	private static Set keys__;
	
	static
	{
	  keys__ = AbstractComposite.set();
	  
    keys__.add(AnyComponent.editable__);
    keys__.add(AnyComponent.path__);
    // Iteration across the 'Map' is tailored towards xml stream format, so
    // we leave out the typedef child which is handled by the xml stream
    // using an attribute.
    //keys__.add(AnyComponent.typedef__);
    keys__.add(AnyComponent.field__);
    keys__.add(AnyComponent.formatString__);
    keys__.add(AnyComponent.label__);

	}
  
	public AnyRenderInfo()
	{
		this(null);
	}

	public AnyRenderInfo(Any dataNode)
	{
		super(dataNode);
	}

	public void setFQName(String fQName) throws AnyException
	{
		//fQName_ = new ConstString(fQName);
    
		LocateNode ln = new LocateNode(fQName);

		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(Transaction.NULL_TRANSACTION,
																			 Catalog.instance().getCatalog(),
																			 ln,
																			 Descriptor.class);
    d_ = descriptor;
    
    if (d_ == null)
			throw new AnyException ("Could not resolve Descriptor at " +
															fQName);
//    if (!resolveDescriptor())
//      return;

//		if (descriptor == null)
//			throw new ModelException ("Could not resolve Descriptor at " +
//																fQName_);

    /*
		if (formatter_ == null)
		{
			if (fmtStr_ == null)
				formatter_ = AnyFormat.makeFormat(d_.getProto().get(field_),
																					d_.getFormat(field_));
			else
				formatter_ = AnyFormat.makeFormat(d_.getProto().get(field_),
																					fmtStr_.toString());
		}
    */
    Any f = d_.getProto().getIfContains(field_);
    if (f == null)
      throw new AnyException("Undefined field " + field_ + " in typedef " + d_);
    
    getFormat(f);
    
		if (label_ == null)
		{
			label_ = d_.getTitle(field_);
		}
		
		if (width_ < 0)
		  width_ = d_.getWidth(field_);

		if (dataNode_ == null)
		{
			// make a default expression out of the
			// descriptor-based data

			ln.setNodePath(new ConstString(//"*" +
															 d_.getDefaultAlias().toString() +
															 "." +
															 field_.toString()));
			dataNode_ = ln;
		}
	}

	public void setField(String field)
	{
		field_ = AbstractValue.flyweightString(field);
	}

	public void setEditable(boolean editable)
	{
		editable_ = editable;
	}

	public boolean isEditable()
	{
		// could enhance with an expression to make conditional on
		// other cell values, for example
		return editable_;
	}

	public boolean isEnum()
	{
		if (editList_ != null)
		  return true;
		  
		if (d_ == null)
			return false;

		return d_.isEnum(field_);
	}

	/**
	 * Determine if we are capable of building the data node and the path
	 * to it.
	 */
	public boolean isBuildable()
	{
		if (d_ != null && field_ != null)
			return true;

//		if ((responsibleData_ != null) && (data_ != null))
//			return true;

		return false;
	}

	public void setData(Any data)
	{
		// If this method is called then we should have a dataNode_
		// expression and it should be a LocateNode.  Then, if the
		// node is not found, we build it using this argument.
		// See resolveDataNode()
    // TODO: Remove
		//data_ = data;
	}

	public void setAlwaysEvaluate(boolean b)
	{
		alwaysEvaluate_ = b;
  }

	public void setLabel(String label)
	{
		label_ = new ConstString(label);
	}

	public void setFormat(String f)
	{
		fmtStr_ = new ConstString(f);
    formatter_ = null;
	}

	public void setWidth(int width)
	{
		width_ = width;
	}
  
	public void setEditingList(AnyListModel list)
	{
		editList_ = list;
	}

	public String getLabel()
	{
		if (label_ != null)
			return label_.toString();
		else
			return AnyString.EMPTY.toString();
	}

  public String getDefaultLabel()
  {
    if (d_ == null)
      return getLabel();
    
    return d_.getTitle(field_).toString();
  }
  
	public int getWidth()
	{
		if (width_ < 0)
			return 12;
		else
			return width_;
	}

	public Any resolveResponsibleData(Any root) throws AnyException
	{
		// There's three possibilities: 1) there is a responsibleData_
		// node reference; 2) the rendering expression is a locate node
		// 3) there's a Bot/Field. If none of these then revert to
		// normal (rendered) data node

		Any ret = null;
		if (responsibleData_ != null)
		{
			ret = EvalExpr.evalFunc(Globals.getProcessForCurrentThread().getTransaction(),
															root,
															responsibleData_);
		}
		else if (dataNode_ instanceof Locate)
		{
			//Locate l = (Locate)dataNode_;
			//ret = l.exec(root);
			ret = EvalExpr.evalFunc(Globals.getProcessForCurrentThread().getTransaction(),
															root,
															dataNode_);
		}
		else if (d_ != null)
		{
			if (dataNode_ instanceof Locate)
			{
				ret = EvalExpr.evalFunc(Globals.getProcessForCurrentThread().getTransaction(),
																root,
																dataNode_);
			}
			else
			{
				LocateNode l = new LocateNode(new ConstString(
															 d_.getDefaultAlias().toString() +
															 "." +
															 field_.toString()));
//				ret = l.exec(root);
				ret = EvalExpr.evalFunc(Globals.getProcessForCurrentThread().getTransaction(),
																root,
																l);
			}
		}
		else
      ret = resolveDataNode(root, true, false);
      
		return ret;
	}

	public Any resolveDataNode(Any root, boolean force) throws AnyException
	{
    return resolveDataNode(root, force, false);
  }

	public Any resolveDataNode(Any root, boolean force, boolean build) throws AnyException
	{
    return resolveDataNode(root, force, false, Globals.getProcessForCurrentThread().getTransaction());
	}

	public Any resolveDataNode(Any         root,
                             boolean     force,
                             boolean     build,
                             Transaction t) throws AnyException
  {
		if (resolvedDataNode_ == null || force || alwaysEvaluate_)
		{
      if (dataNode_ != null)
      {
        resolvedDataNode_ = EvalExpr.evalFunc(t,
                                              root,
                                              dataNode_);
      }

      if (resolvedDataNode_ != null)
      {
        // We've got a datanode. Try making a formatter now so we can
        // apply any directives, which require a xaction and a root
        AnyFormat f = getFormat(resolvedDataNode_);
        if (f != null)
          f.resolveDirectives(root, t);
        
        if (dataNode_ instanceof Locate)
        {
          Locate l = (Locate)dataNode_;
          l.getMapParent().setTransactional(true);
        }
      }
		}

		return resolvedDataNode_;
  }

	public Any buildData(Map root) throws AnyException
	{
		if (!isBuildable())
			throw new AnyException ("Can't build data node");

		Any value = null;

		if (d_ != null)
		{
			if (!root.contains(d_.getDefaultAlias()))
				root.add(d_.getDefaultAlias(), d_.newInstance());
			value = resolveDataNode(root, true);
		}

		// Finally if there's no formatter yet then build one now.
		getFormat(value);

		return value;
	}

	public void setResponsibleData(Locate data)
	{
		responsibleData_ = data;
	}
  
  public Any getValueExpression()
  {
    // Yet another access method.  Returns responsibleData_ if
    // there is one or dataNode_ otherwise
    return (responsibleData_ == null) ? dataNode_ : responsibleData_;
  }

  public NodeSpecification getRenderPath()
  {
    Any l = getValueExpression();
    if (!(l instanceof Locate))
      throw new AnyRuntimeException("Not a simple path");
    
    NodeSpecification n = ((Locate)l).getNodePath();
    
    return (NodeSpecification)n.cloneAny();
  }
  
  public void setRenderPath(NodeSpecification path)
  {
    // Assumes any indirections are resolved (eg MakePath from script) and
    // that there is no responsibleData_ (this feature is as yet undocumented
    // and now beginning to look unnecessary).
    dataNode_ = new LocateNode(path);
    resolvedDataNode_ = null;
  }
  
	public AnyFormat getFormat(Any a)
	{
		if (formatter_ == null || (!formatter_.canFormat(a)))
		{
			// in the absence of any formatter so far created
			// by the time it is demanded, make one now which will
			// do for anything
			//if (fmtStr_ == null)
				//fmtStr_ = new ConstString("{x}");

      String str = (fmtStr_ != null) ? fmtStr_.toString() : null;
      if (str == null && d_ != null)
        str = d_.getFormat(field_);
        
      AnyString s = new AnyString();
			formatter_ = AnyFormat.makeFormat((a != null) ? a : AnyString.EMPTY,
																				str,///*"{x}"*/,
                                        s);
      if (!s.isNull())
        javaFormatStr_ = s.getValue();
		}
		return formatter_;
	}

	public String getFormatString()
  {
    if (javaFormatStr_ != null)
      return javaFormatStr_;
      
    if (fmtStr_ != null)
      return fmtStr_.toString();
    
    String s = null;
    
    if (d_ != null)
      s = d_.getFormat(field_);

    return s;
  }
  
	public Descriptor getDescriptor()
	{
	  if (d_ != null)
		  return d_;
	  else
	    return Descriptor.degenerateDescriptor__;
	}

	public Any getField()
	{
		return field_;
	}

	public AnyListModel getEditingList()
	{
		return editList_;
	}

	// Note: we must be able to iterate over an expression!
	public void resolveNodeSpecs(Any contextNode)
	{
		resolveNodeSpecs(nodeSpecs_, contextNode);
	}

	public void resolveNodeSpecs(Map nodeSpecs, Any contextNode)
	{
    alwaysEvaluate_ = resolveNodeRefs(nodeSpecs, contextNode);
	}

  // Map interface
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported");
  }

  public Any buildNew (Any a)
  {
    throw new IllegalArgumentException ("buildNew() not supported");
  }

  public boolean isTransactional()
  {
    return false;
  }

  public boolean isConst()
  {
    return false;
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  /**
   * Implemented for the specific key values our Map interface supports. Primarily
   * for xml i/o support.
   */
  public void add(Any key, Any value)
  {
    if (key.equals(AnyComponent.editable__))
      editable_ = ((BooleanI)value).getValue();
    else if (key.equals(AnyComponent.path__))
    {
      dataNode_ = new LocateNode(value);
      responsibleData_ = null; // hmmm
    }
    else if (key.equals(AnyComponent.typedef__))
    {
      if (AnyNull.isNull(value)) 
        d_ = null;
      else
        d_ = (Descriptor)value;
    }
    else if (key.equals(AnyComponent.field__))
    {
      if (AnyNull.isNull(value))
        field_ = null;
      else
        field_ = value;
    }
    else if (key.equals(AnyComponent.label__))
    {
      if (AnyNull.isNull(value))
        label_ = AnyString.EMPTY;
      else
        label_ = value;
    }
    else if (key.equals(AnyComponent.formatString__))
    {
      formatter_ = null; // force recreation next time we use it
      if (AnyNull.isNull(value))
      {
        fmtStr_ = null;
        javaFormatStr_ = null; // thus defaults to any field
      }
      else
      {
        fmtStr_ = value;
      }
    }
    else
      throw new IllegalArgumentException(key.toString());
  }

  public void add(StringI keyAndValue)
  {
    throw new UnsupportedOperationException();
  }

  public Map bestowIdentity()
  {
    throw new UnsupportedOperationException();
  }

  public boolean contains(Any key)
  {
    return (key.equals(AnyComponent.editable__) ||
            key.equals(AnyComponent.path__) ||
            key.equals(AnyComponent.typedef__) ||
            key.equals(AnyComponent.field__) ||
            key.equals(AnyComponent.formatString__) ||
            key.equals(AnyComponent.label__));
  }

  public boolean containsValue(Any value)
  {
    throw new UnsupportedOperationException();
  }

  public Iter createKeysIterator()
  {
    return keys__.createIterator();
  }

  public Iter createConcurrentSafeKeysIterator()
  {
    return DegenerateIter.i__;
  }
  
  public Any get(Any key)
  {
    if (key.equals(AnyComponent.editable__))
      return new AnyBoolean(editable_);
    else if (key.equals(AnyComponent.path__))
      return getRenderPath();
    else if (key.equals(AnyComponent.typedef__))
      return getDescriptor();
    else if (key.equals(AnyComponent.field__))
      return getField();
    else if (key.equals(AnyComponent.label__))
      return AbstractValue.flyweightString(getLabel());
    else if (key.equals(AnyComponent.formatString__))
    {
      String s = getFormatString();
      if (s == null)
        return AnyString.NULL;
      else
        return AbstractValue.flyweightString(s);
    }
    else
      throw new FieldNotFoundException(key.toString());
  }

  /*
   * Name clash - need to worry about this?
  public Descriptor getDescriptor()
  {
    return Descriptor.degenerateDescriptor__;
  }
   */

  public Any getIfContains(Any key)
  {
    if (key.equals(AnyComponent.editable__))
      return new AnyBoolean(editable_);
    else if (key.equals(AnyComponent.path__))
      return getRenderPath();
    else if (key.equals(AnyComponent.typedef__))
      return getDescriptor();
    else if (key.equals(AnyComponent.field__))
      return getField();
    else if (key.equals(AnyComponent.label__))
      return AbstractValue.flyweightString(getLabel());
    else if (key.equals(AnyComponent.formatString__))
    {
      String s = getFormatString();
      if (s == null)
        return AnyString.NULL;
      else
        return AbstractValue.flyweightString(s);
    }
    else
      return null;
  }

  public java.util.Map getMap()
  {
    throw new UnsupportedOperationException();
  }

  public short getPrivilegeLevel(Any access, Any key)
  {
    return Process.MINIMUM_PRIVILEGE;
  }

  public Object getPropertyBean()
  {
    throw new UnsupportedOperationException();
  }

  public Any getUniqueKey()
  {
    return null;
  }

  public boolean hasKeys(Array keys)
  {
    throw new UnsupportedOperationException();
  }

  public Array keys()
  {
    throw new UnsupportedOperationException();
  }

  public Any getMapKey(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public Any remove(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceItem(Any key, Any item)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceValue(Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void setContext(Any context)
  {
    throw new UnsupportedOperationException();
  }

  public void setDescriptor(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }

  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    throw new UnsupportedOperationException();
  }

  public void setPropertyBean(Object bean)
  {
    throw new UnsupportedOperationException();
  }

  public void setTransactional(boolean isTransactional)
  {
    throw new UnsupportedOperationException();
  }

  public void setUniqueKey(Any keyVal)
  {
    throw new UnsupportedOperationException();
  }

  public Map shallowCopy()
  {
    throw new UnsupportedOperationException();
  }

  public Composite shallowCopyOf()
  {
    throw new UnsupportedOperationException();
  }

  public void add(Any element)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAny(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void empty()
  {
    throw new UnsupportedOperationException();
  }

  public int entries()
  {
    return 1;
  }

  public boolean valueEquals(Map m)
  {
    return this.equals(m);
  }
  
  public Any getNameInParent()
  {
    throw new UnsupportedOperationException();
  }

  public Any getNodeSet()
  {
    return null;
  }

  public Composite getParentAny()
  {
    throw new UnsupportedOperationException();
  }

  public Any getPath(Any to)
  {
    throw new UnsupportedOperationException();
  }

  public Process getProcess()
  {
    return null;
  }

  public boolean hasIdentity()
  {
    return false;
  }

  public int identity()
  {
    return System.identityHashCode(this);
  }

  public boolean isDeleteMarked(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty()
  {
    return false;
  }

  public boolean isParentable()
  {
    return false;
  }

  public void markForDelete(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public void removeAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void removeInParent()
  {
  }

  public void retainAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void setNodeSet(Any nodeSet)
  {
    throw new UnsupportedOperationException();
  }

  public void setParent(Composite parent)
  {
  }
  
  public Object clone () throws CloneNotSupportedException
  {
  	AnyRenderInfo ri = (AnyRenderInfo)super.clone();
  	
    ri.field_ = (StringI)AbstractAny.cloneOrNull(field_);
    ri.label_ = (StringI)AbstractAny.cloneOrNull(label_);
    ri.fmtStr_ = (StringI)AbstractAny.cloneOrNull(fmtStr_);
    ri.formatter_ = null;
    ri.editList_  = null;  // I think this is always null (i.e. not used TBI)
    ri.resolvedDataNode_ = null;
  	ri.responsibleData_ = (Locate)AbstractAny.cloneOrNull(responsibleData_);

    return ri;
  }
  
  public String toString()
  {
    return super.toString() + dataNode_;
    //return "AnyRenderInfo.toString()";
    //return getLabel();
  }
  
//  private boolean resolveDescriptor() throws AnyException
//  {
//    if (fQName_ == null)
//      return false;
//      
//		LocateNode ln = new LocateNode(fQName_.toString());
//
//		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
//																			(Transaction.NULL_TRANSACTION,
//																			 Catalog.instance().getCatalog(),
//																			 ln,
//																			 Descriptor.class);
//    d_ = descriptor;
//    
//    return d_ != null;
//  }
  
}
