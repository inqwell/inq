/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/BuildNodeMap.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Given an Any (which may or may not be null) build a composite
 * structure according to the NodeSpecification.
 * When the structure has been built place the passed in Any leafNode
 * at the end point.
 * <p>
 * This is analogous to the unix <i>mkdir -p</i> command that
 * creates directories and all those in between.
 * <p>
 * So if we get a NodeSpec like <i>a.b.c</i> and the leaf node
 * is an AnyInt(2) and the given Any is Null then what BuildNodeMap
 * should make is:-
 * Map->a(Map)->b(Map)->c(AnyInt(2))
 */
public final class BuildNodeMap extends    AbstractFunc
                                implements Cloneable
{
  private NodeSpecification s_;
  private Any               leaf_;
  private Any               lastPath_;
//  private boolean           replace_   = true;
  private Map               proto_     = null;
  private Map               mapParent_ = null;

  // The current path element being processed
  private Any               path_;
  
  // Where the ultimate leaf was inserted, when the parent was
  // a node set and supported ordering and a comparator was present
  private int insertionPosition_ = -2;

  private Any               eventId_      = EventConstants.NODE_ADDED;
  private Any               childEventId_ = EventConstants.NODE_ADDED_CHILD;

  /**
   * Create a end leaf node with the given specification.
   * Place the passed in Any at this point
   */
  public BuildNodeMap(NodeSpecification s, Any leaf, Map proto)
  {
    s_      = s;
    leaf_   = leaf;
		proto_  = proto;
  }

  public BuildNodeMap(NodeSpecification s, Any leaf)
  {
		this(s, leaf, null);
  }

	public BuildNodeMap(String nodeSpecStr, Any leaf, Map proto)
	{
		this(new NodeSpecification(nodeSpecStr), leaf, proto);
	}

	public BuildNodeMap()
	{
    s_      = null;
    leaf_   = null;
    proto_  = null;
	}

	public BuildNodeMap(Map proto)
	{
		proto_ = proto;
	}

  public Any build(String s, Any leaf, Any a) throws AnyException
  {
		return build( new NodeSpecification(s), leaf, a);
	}

  public Any build(StringI s, Any leaf, Any a) throws AnyException
  {
		return build( new NodeSpecification(s), leaf, a);
	}


  public Any build(NodeSpecification s, Any leaf, Any a) throws AnyException
  {
		s_ = s;
		leaf_ = leaf;
		return exec (a);
	}

  /**
   * Build the node specification represented by this in the given structure.
   */
  public Any exec (Any a) throws AnyException
  {
    // the passed in Any can be null
    // so we may have to build it
    Map root = null;

    if (a == null || (!(a instanceof Map)))
    {
	    if (proto_ != null)
	      root = (Map)proto_.buildNew(null);
	    else
	      root = AbstractComposite.map();
    }
    else
    {
       root = (Map)a;
    }

		mapParent_ = root;
		
		int j = s_.entries();
		boolean isControl = true;
		lastPath_ = null;
		while (j > 0 && isControl)
		{
      lastPath_ = s_.get(--j);
      isControl = NodeSpecification.isControl(lastPath_);
    }

    if (lastPath_ == null)
      return null;

    Iter i = s_.createIterator();

		ResolvePathComponent res = new ResolvePathComponent(root, i);
		res.setTransaction(getTransaction());

    //System.out.println("BuildNodemap.exec(): root is " + root);
    //System.out.println("BuildNodemap.exec(): path is " + s_);

    insertionPosition_ = -2;
    processPathItem(root, i, res);
    return leaf_;
  }

  private void processPathItem(Composite current, Iter i, ResolvePathComponent res)
  {
	  if (i.hasNext())
	  {
		  // May be build a new node at the current point and
		  // return the name we built it at
      //Any pathItem = applyItem(current, i, res);
      applyItem(current, i, res);
	  }
	
		  // If its a node set then we may like to reorder after
		  // building below the current node is complete.
      // Note, reordering only applies at the insertion point,
      // determined by the path being exhausted. Otherwise, structures
      // like recursive trees would be erroneously reordered as
      // each node set was traversed on the way back up.
//		  if (current.getNodeSet() != null &&
//		      pathItem != null &&
//          insertionPosition_ == -2)
//		  {
//			  if (current instanceof Orderable)
//			  {
//				  Orderable o = (Orderable)current;
//				  insertionPosition = o.reorder(pathItem);
//				  
//				  // When dealing with a node set and there is no comparator
//				  // present then it is important for GUI dispatching that a
//				  // vector number is supplied. If none is yet known
//				  if (insertionPosition < 0 && current instanceof Vectored)
//				  {
//				    Vectored v = (Vectored)current;
//				    v.initOrderBacking();
//				    insertionPosition = v.indexOf(pathItem);
//				  }
//				}
//		  }
//		}
//    
//    // Remember only deepest insertion position. Have to resort to this
//    // mechanism as insertionPosition is not passed up through the recursive
//    // call path. Only assign the member once as we pop back up.
//    if (insertionPosition_ == -2 && insertionPosition >= 0)
//      insertionPosition_ = insertionPosition;
//    
	}
	
  private boolean resolvePathItem(ResolvePathComponent res, Iter i)
  {
  	// Apply the current path item to the resolving visitor.
  	// If it is a function it will be evaluated against the
   	// current context.  The visitor also has the current node
   	// in case that is required (for example, in the case of
   	// vectored access)
    path_.accept(res);

    // The protocol for NodeSpecification embedded functions is
    // that they return the new node, if that it is what
    // it is, through getParam().  If they only did the path
    // resolving then they return null here and the path is
    // what is returned by exec().  This fits with the majority
    // of path items which are just non-funcs.

    Any node     = res.getOutputParam();

    if (node == null)
    {
      Any pathItem = res.getResolved();
      // Its a path component.  If its not the self-same
      // as the current then parse it, consume the first element
      // and push all elements back into the current iterator.
      if ((pathItem != null) && (pathItem != path_))
      {
        boolean last = !i.hasNext(); //last = (path_ == lastPath_);
        // It had better be a string!
        if (pathItem instanceof StringI)
        {
          //System.out.println("pushing " + pathItem);
          path_ = null;
          if (pathItem.toString().length() != 0)
          {
            NodeSpecification n = new NodeSpecification(pathItem.toString());

            path_ = n.get(0);
            for (int j = 1; j < n.entries(); j++)
              i.add(n.get(j));
            if (last)
              lastPath_ = n.getLast();
            for (int j = 1; j < n.entries(); j++)
              i.previous();
          }
        }
        else if (pathItem instanceof NodeSpecification)
        {
          NodeSpecification n = (NodeSpecification)pathItem;

          path_ = n.get(0);
          for (int j = 1; j < n.entries(); j++)
            i.add(n.get(j));
          if (last)
            lastPath_ = n.getLast();
          for (int j = 1; j < n.entries(); j++)
            i.previous();
        }
        else
        {
          if (pathItem instanceof AnyObject)
          {
            AnyObject o = (AnyObject)pathItem;
            if (o.getValue() instanceof Any)
              pathItem = (Any)o.getValue();
          }
          path_ = pathItem;
          if (last)
            lastPath_ = path_;
        }
        if (path_ != null)
          return resolvePathItem(res, i);
        else
          return true;
      }

      if (pathItem != null)
        path_ = pathItem;

      return true;
    }
    else
    {
      // its a new node
      return false;
    }
  }


  public int getInsertionPosition()
  {
    return insertionPosition_;
  }

  private Any applyItem(Composite current, Iter i, ResolvePathComponent res)
	{
		Any pathItem = null;
		boolean isControl = true;
		while (isControl)
		{
			pathItem = i.next();
      isControl = NodeSpecification.isControl(pathItem);
    }

    path_    = pathItem;
    //System.out.println("current: " + current);
    //System.out.println("pathItem: " + pathItem);

    res.param_ = current;
    resolvePathItem(res, i);

    Any retPathItem = null;

    if (res.isPathItem())
    {
      if (!(current instanceof Map))
      {
        throw new AnyRuntimeException("Illegal structure request at " + path_ + "(" + current.getClass() + ")");
      }
      Map node = (Map)current;
      //path_       = res.getResolved();
      retPathItem = path_;
      
      if (path_ == null)
      {
        // current path element evaluated to the empty string - ignore
  			mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
      }
      
      pathItem = path_;

      // System.out.println("BuildNodemap.exec(): path_ is " + path_);
      if (path_.toString().equals(NodeSpecification.root__))
  		{
  			node = (Map)NodeSpecification.processRoot(node);
  			mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
  		}
      else if (path_.toString().equals(NodeSpecification.stack__))
      {
        node = (Map)NodeSpecification.processStack(getTransaction());
        mapParent_ = node;
        processPathItem(node, i, res);
        return retPathItem;
      }
      else if (path_.toString().equals(NodeSpecification.loop__))
      {
        node = (Map)NodeSpecification.processLoop(getTransaction());
        mapParent_ = node;
        processPathItem(node, i, res);
        return retPathItem;
      }
  		else if (path_.toString().equals(NodeSpecification.context__))
  		{
  			node = NodeSpecification.processContext(getTransaction());
  			mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
  		}
  		else if (path_.toString().equals(NodeSpecification.process__))
  		{
  			node = (Map)NodeSpecification.processProcess(getTransaction());
  			mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
  		}
 			else if (path_.toString().equals(NodeSpecification.parent__))
 			{
 				node = (Map)NodeSpecification.processParent(node);
 				mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
 			}
 			else if (path_.toString().equals(NodeSpecification.catalog__))
 			{
 				node = (Map)NodeSpecification.processCatalog();
 				mapParent_ = node;
  			processPathItem(node, i, res);
        return retPathItem;
 			}

     	if (i.hasNext()) //(pathItem != lastPath_)
     	{
	      Composite child;
     		if (!node.contains(path_) || node.get(path_) == null)
     		{
            // Check for ADD PRIVILEGE at the current node, key of path_
            getTransaction().checkPrivilege(AbstractMap.P_ADD, node, path_);

  					// If we've been given an explicit prototype then use it.
  					// Otherwise use the map type of the current node.
            // Using replaceItem because of possibility that current
            // node contains (Java) null for current path
  					if (proto_ != null)
  						node.replaceItem(path_, (child = (Composite)proto_.buildNew(null)));
  					else
  						node.replaceItem(path_, (child = (Composite)node.buildNew(null)));
  			}
      	else
      	{
          Any c = node.get(path_);
          if (!(c instanceof Composite))
          {
            throw new AnyRuntimeException("Illegal structure request at " + path_ + "(" + c.getClass() + ")");
          }
      	  child = (Composite)c;
  			}

        if (child instanceof Map)
          mapParent_ = (Map)child;
        processPathItem(child, i, res);
        return retPathItem;
      }
      else
      {
        // Add in the leaf
	      if (path_ != null)
	      {
	      	if (node.contains(path_))
	      	{
            // require REMOVE and ADD PRIVILEGE on node for key path_
            getTransaction().checkPrivilege(AbstractMap.P_ADD, node, path_);
            getTransaction().checkPrivilege(AbstractMap.P_REMOVE, node, path_);
      		  node.replaceItem(path_, leaf_);
            eventId_      = EventConstants.NODE_REPLACED;
            childEventId_ = EventConstants.NODE_REPLACED_CHILD;
            
            // See if we can get the insertion (original) position here:
//            if (node.getNodeSet() != null && node instanceof Vectored)
//              insertionPosition_ = ((Vectored)node).indexOf(path_);
	      	}
	      	else
	      	{
            // require ADD PRIVILEGE on node for key path_
            getTransaction().checkPrivilege(AbstractMap.P_ADD, node, path_);
	      		node.add(path_, leaf_);
	          eventId_      = EventConstants.NODE_ADDED;
	          childEventId_ = EventConstants.NODE_ADDED_CHILD;
	          
	          // Determine the insertion position when adding to a node set
	          if (node.getNodeSet() != null && (node instanceof Orderable))
	          {
              Orderable o = (Orderable)node;
              int insertionPosition = o.reorder(path_);
              
              // When dealing with a node set and there is no comparator
              // present then it is important for GUI dispatching that a
              // vector number is supplied. If none is yet known (because
              // there is no order backing / comparator present) then
              // set up the 
              if (insertionPosition < 0 && current instanceof Vectored)
              {
                Vectored v = (Vectored)current;
                v.initOrderBacking();
                insertionPosition = v.indexOf(pathItem);
              }
              insertionPosition_ = insertionPosition;
	          }
	      	}
	      }
	      return retPathItem;
      }
    }
    else
    {
    	if (i.hasNext()) //(pathItem != lastPath_)
      {
        // it was a vectored get or a node indirect
        current = (Composite)res.getResolved();
        if (current instanceof Map)
          mapParent_ = (Map)current;
        processPathItem(current, i, res);
        return null;
      }
      else
      {
	      // in the case of a vectored add there's nothing to do - the visitor
	      // has already done it
	      eventId_      = EventConstants.NODE_ADDED;
	      childEventId_ = EventConstants.NODE_ADDED_CHILD;
	    }
	    return null;
    }
  }

	public void setMapProto(Map m)
	{
		proto_ = m;
	}

	public Map getMapParent()
	{
		return mapParent_;
	}

	public Any getPath()
	{
		return path_;
	}
	
	public Any getRaisedEventId()
	{
		return eventId_;
  }

	public Any getChildRaisedEventId()
	{
		return childEventId_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    BuildNodeMap ln = (BuildNodeMap)super.clone();
    return ln;
  }

  private class ResolvePathComponent extends    AbstractVisitor
                                     implements Cloneable
  {
    private static final long serialVersionUID = 1L;

    private Any  resolved_;
    private Any  root_;
    private Any  param_;
    private Any  paramOut_;
    //private Iter i_;

    private boolean pathItem_;

    ResolvePathComponent(Any root, Iter i)
    {
      root_ = root;
      //i_    = i;
    }

    Any getResolved()
    {
      Any ret   = resolved_;
      resolved_ = null;
      param_    = null;
      return ret;
    }

    Any getOutputParam()
    {
      Any ret   = paramOut_;
      paramOut_ = null;
      return ret;
    }

    boolean isPathItem()
    {
      return pathItem_;
    }

    public void visitAnyByte (ByteI b)
    {
      resolved_ = b;
      pathItem_ = true;
    }

    public void visitAnyChar (CharI c)
    {
      resolved_ = c;
      pathItem_ = true;
    }

    public void visitAnyInt (IntI i)
    {
      resolved_ = i;
      pathItem_ = true;
    }

    public void visitAnyShort (ShortI s)
    {
      resolved_ = s;
      pathItem_ = true;
    }

    public void visitAnyLong (LongI l)
    {
      resolved_ = l;
      pathItem_ = true;
    }

    public void visitAnyFloat (FloatI f)
    {
      resolved_ = f;
      pathItem_ = true;
    }

    public void visitAnyDouble (DoubleI d)
    {
      resolved_ = d;
      pathItem_ = true;
    }

    public void visitDecimal (Decimal d)
    {
      resolved_ = d;
      pathItem_ = true;
    }

    public void visitAnyString (StringI s)
    {
      resolved_ = s;
      pathItem_ = true;
    }

    public void visitFunc (Func f)
    {
      // If we hit on a func then clone before executing.
      // Not always necessary (check up !)
      Func org = f;
      f = (Func)f.cloneAny();
      f.setTransaction(getTransaction());
      f.setParam(param_);

      // If the func is an index get and we are dealing with
      // the last component in the path then the operation
      // becomes an 'addByVector'
      LocateNode.Index li = null;
      if (org == lastPath_ && (org instanceof LocateNode.Index))
      {
        li = (LocateNode.Index)f;
        li.setVectorBuild(true);
      }

      Any a = null;
      try
      {
        a = f.execFunc(root_);
        if (a != null && a.isTransactional())
          a = f.doTransactionHandling(root_, a);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }

      if (li == null)
      {
        if (a != null)
          a.accept(this);
        else
          throw new AnyRuntimeException("Substitution did not resolve " + f);
      }
      
      if (li != null)
      {
        // The index function doesn't do the vector build - we do it here.
        insertionPosition_ = li.doVectorBuild(leaf_);
        pathItem_ = false;
      }
      else
      {
        // only remember any parameter from the outermost function
        paramOut_ = f.getParam();

        // We are dealing with a func but its not the last path
        // or its not an index (just an indirect node path
        // component evaluation.
        // If its an index then we resolve the node.  If its an
        // indirection then we set up the path member in our
        // outer class for normal processing.

        if (paramOut_ == null)
        {
          // its a new path component.
          pathItem_ = true;
        }
        else
        {
          // its a new node
          pathItem_ = false;
        }
      }
    }

    public void visitAnyDate (DateI d)
    {
      resolved_ = d;
      pathItem_ = true;
    }

    public void visitMap (Map m)
    {
      resolved_ = m;
      pathItem_ = true;
    }

    public void visitArray (Array a)
    {
      resolved_ = a;
      pathItem_ = true;
    }

    public void visitSet (Set s)
    {
      resolved_ = s;
      pathItem_ = true;
    }

    public void visitAnyObject (ObjectI o)
    {
      resolved_ = o;
      pathItem_ = true;
    }

    public void visitUnknown(Any o)
    {
      resolved_ = o;
      pathItem_ = true;
    }
  }
}

