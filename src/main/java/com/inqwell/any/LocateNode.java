/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LocateNode.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Given an Any structure and a node specification, locate the node
 * in the structure described by the specification.
 * <p>
 * Location may be either
 * strict or lazy.  If strict then the exact structure described by the
 * specification must be found when locating the node.  A lazy location
 * allows for a path component to be absent at the current node, when
 * a breadth first iteration is performed in an attempt to find it.
 * <P>
 * A locate path is of the form <code>w.x*y.z</code> where * and . are
 * delimiter characters separating the various path elements.  As the path
 * is consumed the most recent separator controls the strictness of the
 * next element: "*" means <code>lazy</code> while "."
 * means <code>strict</code>.  Strictness can only be controlled in this
 * way and in initially set to <code>lazy</code>.  Hence to enforce
 * a strict location at the outset a path like <code>.x.y.z</code>
 * can be used.
 * <P>
 * The <code>exec(Any)</code> method will apply the location from the given
 * node unless there is a <code>preferredRoot</code>, in which case that
 * is used as the location root instead.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 */
public class LocateNode extends    AbstractFunc
												implements Locate,
																	 Cloneable
{
	private static final long serialVersionUID = 1L;

	public  static LocateNode null__ = new LocateNode(NodeSpecification.null__);
	private static AnyObject  emptyIndirect__ = new AnyObject();

	//private Any op1_;
	//private Any root_;

  private NodeSpecification    s_;
  private boolean              strict_;
  private transient LocateAction         act_;
  private transient ResolvePathComponent res_;

  private transient Iter              pathI_;
  private transient Map               lastNodeSet_;
  private transient Map               mapParent_;
  private transient Map               tmapParent_;
  private transient Map               tmapFound_;
  private transient Array             arrayParent_;
  private transient Any               pathItem_;
  private transient Any               found_;

	public LocateNode(String nodeSpecStr)
	{
		this(new NodeSpecification(nodeSpecStr));
	}

  /**
   * Locate the node given by <code>NodeSpecification s</code>
   */
  public LocateNode(NodeSpecification s)
  {
    s_   = s;
    //op1_ = s;
    init();
  }

  public LocateNode(StringI s)
  {
		this(new NodeSpecification(s));
  }

  public LocateNode(Any a)
  {
    if (a instanceof NodeSpecification)
      s_   = (NodeSpecification)a;
    else
      s_   = new NodeSpecification(a.toString());

    init();
  }

  /**
	 * Locate first operand with respect to second, as opposed to
	 * the context node supplied whe the location is performed
	 */
//  public LocateNode(Any a, Any altRoot)
//  {
//		op1_  = a;
//		root_ = altRoot;
//		s_    = null;
//    init();
//  }

	public LocateNode()
	{
    s_   = null;
    //op1_ = null;
    init();
	}

	/**
	 * Locate using current NodeSpecification from the new root.
	 */
  public Any locate (Any a) throws AnyException
  {
		strict_      = true;
    mapParent_   = null;
		return execFunc (a);
	}


	/**
	 * Locate node given by <code>s</code> from root at <code>a</code>.
	 */
  public Any locate (NodeSpecification s, Any a) throws AnyException
  {
		s_           = s;
		//op1_         = s;
		strict_      = true;
    mapParent_   = null;
		return execFunc (a);
	}

  /**
   * Apply the node specification represented by this to the given structure.
   */
  public Any exec (Any a) throws AnyException
  {
    Any ret = null;

    Transaction t = getTransaction();
    
    if ((a != null) && (tmapParent_ == a))
    {
      // being asked to resolve final part of location specification on a
      // private instance that has previously been performed and resulted
      // in a t-Map parent
      //System.out.println ("Resolving tmap parent " + a);
      //System.out.println ("for " + pathItem_);

      // Get any transaction private instance
      Map m = t.getTransInstance(tmapParent_);
      
      // Tell the resolving visitor that we are doing the transaction
      // handling phase and put the private instance in to the node
      // resolver.
      act_.resolvingTransaction_ = true;
      res_.param_ = m;

      //System.out.println ("Resolved to  " + m);
      // check read PRIVILEGE for pathItem_ in tmapParent_
      t.checkPrivilege(AbstractMap.P_READ, tmapParent_, pathItem_);
      ret = m.contains(pathItem_) ? m.get(pathItem_) : null;

      t.setLastTMap(tmapParent_);
      t.setLastTField(ret);
      t.setResolving(Transaction.R_FIELD);

      return ret;
    }

    if ((a != null) && (tmapFound_ == a))
    {
      // being asked to fetch the private instance of a previous location
      // which resulted in a t-Map target
      ret = t.getTransInstance(tmapFound_);

      t.setLastTMap(tmapFound_);
      t.setLastTField(null);
      t.setResolving(Transaction.R_MAP);

      return ret;
    }

    // just start normal location
    //System.out.println (toString());
		act_.setTransaction(t);
		act_.resolvingTransaction_ = false;
		res_.setTransaction(t);

		// Resolve op1_ w.r.t. given root
		//resolveNodePath(a);

		// Resolve any root
		//Any root = resolveRoot(a);

    pathI_ = s_.mustShallowCopy() ? s_.shallowCopy().createIterator()
                                  : s_.createIterator();

    found_ = doLocate(a);
    
    t.setLastTMap(null);
    t.setLastTField(null);
    t.setResolving(Transaction.R_NOTHING);
    
		return checkReturn();
  }

  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
	  return exec(a);
  }

	// plug in equality and hashCode.  Allows us to flyweight LocateNode
	// instances within a process.
	public boolean equals (Object o)
	{
		if (this == o)
			return true;

		if (!(o instanceof LocateNode))
			return false;

		LocateNode ln = (LocateNode)o;

		if ((s_ == null) || (ln.s_ == null))
			return false;

		return s_.equals(ln.s_);
	}

	public int hashCode()
	{
		return s_.hashCode();
	}

  public Map getMapParent ()
  {
    return mapParent_;
  }

  public Map getTMapFound ()
  {
    return tmapFound_;
  }

  public Array getArrayParent ()
  {
    return arrayParent_;
  }

  /**
	 * Attempt to see if the locate path to the specified node has an
	 * array parent.  A tree is not guaranteed to be navigable so this
	 * method can trow a runtime exception.
	 * @return the nearest ancestor of the locate target which is an array
	 * or null if location failed or there is no array ancestor
	 */
  public Array hasArrayParent()
  {
		Array ret = null;
		if (found_ != null)
		{
			if (arrayParent_ != null)
			{
				ret = arrayParent_;
			}
			else
			{
				Composite c      = mapParent_;
				Composite parent = null;
				while ((!((parent = c.getParentAny()) instanceof Array)) && parent != null)
				{
					c = parent;
				}
				if (parent != null)
					ret = (Array)parent;
			}
		}
		return ret;
  }

  public boolean isVectorElement()
  {
    return (res_.getVectorNumber() != -2);
    //boolean ret = (res_.getVectorNumber() != -2);
    //System.out.println("LocateNode.isVectorElement() " + ret);
    //return ret;
  }
  
  public int getVectorNumber()
  {
    return res_.getVectorNumber();
    //int ret = res_.getVectorNumber();
    //System.out.println("LocateNode.getVectorNumber() " + ret);
    //return ret;
  }
  
  public Any getPath()
  {
		return pathItem_;
	}

  public Object clone() throws CloneNotSupportedException
  {
    LocateNode ln = (LocateNode)super.clone();
    ln.init();

    // if a node spec was provided (in s_) then this is read-only
    // and so doesn't need cloning.
    // if s_ is null then there must be an operand for it in op1_
    // and we must clone this.  May be a bit messy but then may be
    // its also the most efficient as the majority of LocateNode
    // instances are literal

    //if (s_ == null)
			//ln.op1_ = op1_.cloneAny();

		//ln.root_ = AbstractAny.cloneOrNull(root_);

    return ln;
  }

	public String toString()
	{
		//return super.toString() + " " + s_;
		return s_.toString();
	}

  protected void nodeFound(Any pathItem, Any node, Any parent)
  {
    // derived class hook for picking up each node and
    // its path as it is found
  }
  
  private Any checkReturn()
  {

    if (found_ != null && found_.isTransactional())
    {
			tmapFound_ = (Map)found_;
    }

      
    Any ret = ((!getTransaction().isDegenerate()) &&
							 (tmapParent_ != null)) ? tmapParent_ : found_;

    // if ret == found_ Check read PRIVILEGE in mapParent_ for pathItem_
    if (ret == found_)
      getTransaction().checkPrivilege(AbstractMap.P_READ, mapParent_, pathItem_);
		return ret;
	}

  private Any doLocate(Any a) throws AnyException
  {
  	//System.out.println ("Evaluating against " + a);

		found_ = null;

		res_.root_ = a;

		if (this == LocateNode.null__)
			return found_;

		while ((pathI_.hasNext()) && (a != null))
		{
			Any nextItem = next(pathI_);
			if (nextItem == null)
				break;

			pathItem_ = nextItem;

      Any cur = a;
			a = apply (cur);
      
      nodeFound(pathItem_, a, cur);

		}
		res_.root_ = null;
		return a;
	}

  private Any apply (Any a)
  {
    // The current path component is in pathItem_ but it may be a
    // function.  Put the current node into the
    // resolving visitor - it may be needed by whatever func we
    // are applying.
    res_.param_  = a;

    act_.found_ = null;
		a.accept (act_);
    Any found = act_.found();

    if ((found != null) || (strict_))
      return found;

    BreadthFirstIter descendI = new BreadthFirstIter(a);

    while ((descendI.hasNext()) && (found == null))
    {
      a = descendI.next();

      a.accept (act_);
      found = act_.found();

    }
    return found;
  }

/*
  public void resolveOperands(Any root) throws AnyException
  {
		if (s_ == null)
		{
			op1_ = EvalExpr.resolveOperand(getTransaction(),
																		 root,
																		 op1_);
		}
	}
*/
  public void setNodePath(Any path)
  {
		s_   = null;
		//op1_ = path;
		if (path instanceof StringI)
			s_ = new NodeSpecification((StringI)path);
		else if (path instanceof NodeSpecification)
			s_ = (NodeSpecification)path;
    else
      s_ = new NodeSpecification(path.toString());

		reset();
  }

  public NodeSpecification getNodePath()
  {
  	return s_;
  }

  public Any getLast()
  {
    return s_.getLast();
  }
  
  public Map getLastNodeSet()
  {
    return lastNodeSet_;
  }
  
  /**
	 * Reset this instance for re-use.  Discards any nodes and parents
	 * remembered from the last execution.  Should be called if this
	 * instance would not normally be garbage collected, so as to avoid
	 * dangling references to nodes which should be collected themselves.
	 */
  public void reset()
  {
    strict_        = true;
    mapParent_     = null;
    lastNodeSet_   = null;
    tmapParent_    = null;
    arrayParent_   = null;
    pathItem_      = null;
    found_         = null;
    act_.reset();
    res_.reset();
  }

  private void init()
  {
    act_           = new LocateAction();
    res_           = new ResolvePathComponent();
    reset();
  }

	private void readObject(ObjectInputStream instr)
																							throws IOException,
																										 ClassNotFoundException
	{
		instr.defaultReadObject();
    init();
	}

  // Fetch the next path token from the given iterator and process any
  // path separators controlling the strict-ness of the location along the
  // way.
	private Any next(Iter i)
	{
		boolean control = true;

		Any a = null;

		while (i.hasNext() && control)
		{
			a = i.next();

			if (NodeSpecification.isStrict(a))
			{
				strict_ = true;
				control = true;
				a       = null;
			}
			else if (NodeSpecification.isLazy(a))
			{
				strict_ = false;
				control = true;
				a       = null;
			}
			else
			{
				control = false;
			}
		}
		return a;
	}

//  private void resolveNodePath(Any root) throws AnyException
//  {
//		if (s_ == null)
//		{
//			AnyString s = (AnyString)EvalExpr.evalFunc(getTransaction(),
//																								 root,
//																								 op1_,
//																								 AnyString.class);
//			s_ = new NodeSpecification(s);
//		}
//	}
//
//  private Any resolveRoot(Any root) throws AnyException
//  {
//		if (root_ != null)
//		{
//			root = EvalExpr.evalFunc(getTransaction(),
//															 root,
//															 root_);
//		}
//		return root;
//  }

  // Take the trouble to implement all the Visitor methods, rather than
  // just falling back on AbstractVisitor, whose defaults throw
  // an IllegalArgumentException.  This way, if we are doing a lazy
  // evaluation and we end up traversing through a lot of leaf nodes,
	// we don't incur the overhead of throwing and catching unwanted
	// exceptions.  It also means that the interface is the same whether
	// the location is lazy or not, we just return null.
  private class LocateAction implements Visitor
  {
    private static final long serialVersionUID = 1L;

    private Any         found_       = null;
    private AnyInt      arrayIndex_  = new AnyInt();
  	private Transaction transaction_ = Transaction.NULL_TRANSACTION;
  	
  	private boolean     resolvingTransaction_ = false;

    public Any found()
    {
      return found_;
    }

    void reset()
    {
      found_ = null;
      resolvingTransaction_ = false;
    }

    // Return true if the result is a path component, false
    // if it is the new node.
    private boolean resolvePathItem(Any curr)
    {
      // Tell the resolving visitor if we are performing transaction
      // handling
      //res_.resolvingTransaction_ = resolvingTransaction_;
      
      // Reset the vector flag - this is only valid for the
      // last path component
      res_.vectorNumber_ = -2;
      
    	// Apply the current path item to the resolving visitor.
    	// If it is a function it will be evaluated against the
    	// current context.  The visitor also has the current node
    	// in case that is required (for example, in the case of
    	// vectored access)
      pathItem_.accept(res_);

      // The protocol for NodeSpecification embedded functions is
      // that they return the new node, if that it is what
      // it is, through getParam().  If they only did the path
      // resolving then they return null here and the path is
      // what is returned by exec().  This fits with the majority
      // of path items which are just non-funcs.

      Any pathItem = res_.getResolved();
      Any node     = res_.getOutputParam();

      if (node == null)
      {
        // Its a path component.  If its not the self-same
        // as the current parse it, consume the first element
        // and push all elements back into the current iterator.
	      if ((pathItem != null) && (pathItem != pathItem_))
	      {
          if (pathItem instanceof StringI)
          {
	          // If its a string treat as a path and tokenise.
	          // First element becomes the current path item and
	          // any remaining go into the parent iterator
	          //System.out.println("pushing " + pathItem);
            // Using toString here - a bit cheap but avoids
            // a cast
	          if (pathItem.toString().length() != 0)
	          {
              NodeSpecification n = new NodeSpecification(pathItem.toString());
              doIndirectNs(n);
            }
            else
            {
              pathItem_ = AnyNull.instance();
            }
	        }
	        else if (pathItem instanceof NodeSpecification)
	        {
	        	NodeSpecification n = (NodeSpecification)pathItem;
	        	doIndirectNs(n);
	        }
	        else
	        {
	          if (pathItem instanceof AnyObject)
	          {
	            AnyObject o = (AnyObject)pathItem;
	            if (o.getValue() instanceof Any)
	              pathItem = (Any)o.getValue();
	          }
	          pathItem_ = pathItem;
		      }

          return resolvePathItem(curr);
        }

        if (pathItem != null)
          pathItem_ = pathItem;

        return true;
      }
      else
      {
        // its a new node
        found_ = node;
        return false;
      }
    }
    
    private void doIndirectNs(NodeSpecification n)
    {
      int i = 0;
      int e = n.entries();
      while (i < e && NodeSpecification.isControl(n.get(i))) i++;
      if (i < e)
      {
          pathItem_ = n.get(i++);
          for (int j = i; j < e; j++)
            pathI_.add(n.get(j));
          for (int j = i; j < e; j++)
            pathI_.previous();
      }
    }

    /*
		private void mapLocateAction(Map m)
		{
			found_ = null;

      if (m.contains(LocateNode.this.pathItem_))
      {
				// ...normal behaviour - the map contains the path item we are
				// currently looking for
        found_ = m.get(LocateNode.this.pathItem_);
				mapParent_   = m;
				arrayParent_ = null;
			}
		}
     */
    private void mapLocateAction(Map m)
    {
      if ((found_ = m.getIfContains(LocateNode.this.pathItem_)) != null)
      {
        // ...normal behaviour - the map contains the path item we are
        // currently looking for
        //found_ = m.get(LocateNode.this.pathItem_);
        mapParent_   = m;
        arrayParent_ = null;
      }
    }
    public void visitMap (Map m)
    {
      if (resolvePathItem(m))
      {
        if (LocateNode.this.pathItem_ == AnyNull.instance())
        {
          found_ = AnyNull.instance();
        }
        else
        {
          // normal case of applying the current path item to the
          // current node
          if (!processKeyTokens(m))
          {
            mapLocateAction(m);
            if (m.isTransactional())
              tmapParent_ = m;
            else
              tmapParent_  = null;
          }
        }
      }
      else
      {
        // We've already got the node in found_.  Since we don't
        // know what the NodeSpecification-embedded function actually
        // did its a moot point whether we set the mapParent_ member.
        // At the moment the only implememtation of such a function
        // is LocateNode.Index which allows us to do vectored gets
        // on the current node.  In this case its valid to set the
        // parent.
				mapParent_   = m;
				arrayParent_ = null;
        if (m.isTransactional())
          tmapParent_ = m;
        else
          tmapParent_  = null;
      }
      if (mapParent_ != null && mapParent_.getNodeSet() != null)
        lastNodeSet_ = mapParent_;
    }

    public void visitArray (Array a)
    {
			//System.out.println ("LocateNode.LocateAction.visitArray " + a);
			//System.out.println ("LocateNode.pathItem_ " + LocateNode.this.pathItem_);
      if (resolvePathItem(a))
      {
        if (!processKeyTokens(a))
        {
          boolean copied = true;
          try
          {
            arrayIndex_.copyFrom (LocateNode.this.pathItem_);
          }
          catch (RuntimeException e)
          {
            // Parse error occurred on current path element so this
            // element is not numeric.
            copied = false;
            found_ = null;
            //System.out.println ("LocateNode caught exception " + e.getMessage());
          }
          if (copied)
            found_ = a.get(arrayIndex_);

          mapParent_    = null;
          tmapParent_   = null;
          arrayParent_  = a;
        }
      }
      else
      {
        mapParent_    = null;
        tmapParent_   = null;
        arrayParent_  = a;
      }
    }

		public void visitUnknown(Any b)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

    public void visitSet (Set s)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyBoolean (BooleanI b)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyByte (ByteI b)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyChar (CharI c)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyInt (IntI i)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyShort (ShortI s)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyLong (LongI l)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyFloat (FloatI f)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyDouble (DoubleI d)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitDecimal (Decimal d)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyString (StringI s)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyDate (DateI d)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitFunc (Func f)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

		public void visitAnyObject (ObjectI o)
		{
      mapParent_    = null;
      tmapParent_   = null;
      arrayParent_  = null;
			found_        = null;
			processKeyTokens(null);
		}

    public void setTransaction(Transaction t)
    {
  		transaction_ = t;
 			found_       = null;
 	  }

    public Transaction getTransaction()
    {
  		return transaction_;
  	}

		private boolean processKeyTokens(Composite c)
		{
			//System.out.println ("KeyToken: " + pathItem_);
			boolean keyToken = false;
			
			if (pathItem_ == null)
        return keyToken;
        
			if (pathItem_.toString().equals(NodeSpecification.root__))
			{
				keyToken = true;

				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
				found_ = getTransaction().getProcess().getRoot();
				//System.out.println ("LocateNode processing root, found " + found_);
			}
			else if (pathItem_.toString().equals(NodeSpecification.stack__))
			{
				keyToken = true;

				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
				found_ = getTransaction().getCurrentStackFrame();
			}
			else if (pathItem_.toString().equals(NodeSpecification.catalog__))
			{
				keyToken = true;

				// Direct to the system catalog
				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
				found_ = Catalog.instance().getCatalog();
			}
			else if (pathItem_.toString().equals(NodeSpecification.process__))
			{
				//System.out.println ("LocateNode.processKeyTokens - process");
				keyToken = true;

				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
				found_        = getTransaction().getProcess();
				//System.out.println ("LocateNode.processKeyTokens - process " + found_);
			}
      else if (pathItem_.toString().equals(NodeSpecification.loop__))
      {
        keyToken = true;

        mapParent_    = null;
        tmapParent_   = null;
        arrayParent_  = null;
        found_        = getTransaction().getLoop();
        if (found_ == null)
          throw new AnyRuntimeException("No current $loop");
      }
			else if (pathItem_.toString().equals(NodeSpecification.context__))
			{
				keyToken = true;

				found_        = getTransaction().getContext();
				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
			}
			else if (pathItem_.toString().equals(NodeSpecification.contextPath__))
			{
				keyToken = true;

				found_        = getTransaction().getContextPath();
				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
			}
			else if (pathItem_.toString().equals(NodeSpecification.null__))
			{
				keyToken = true;

				found_        = null;
				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
			}
			else if (pathItem_.toString().equals(NodeSpecification.properties__))
			{
				keyToken = true;

				// Direct to the system catalog
				mapParent_    = null;
				tmapParent_   = null;
				arrayParent_  = null;
				found_ = SystemProperties.instance().getSystemProperties();
			}
      else if (pathItem_.toString().equals(NodeSpecification.uidefaults__))
      {
        keyToken = true;

        mapParent_    = null;
        tmapParent_   = null;
        arrayParent_  = null;
        found_ = Globals.getUIDefaults();
      }
      else if (pathItem_.toString().equals(NodeSpecification.parent__))
      {
        keyToken = true;

        mapParent_    = null;
        tmapParent_   = null;
        arrayParent_  = null;

        // Go to pur parent.  May fail as above.  If we are at the
        // root already then just return self.
        Composite parent = c.getParentAny();
        if (parent != null)
          found_ = parent;
        else
          found_ = c;
      }
			return keyToken;
		}
  }

  private class ResolvePathComponent extends    AbstractVisitor
                                     implements Cloneable
  {
    private static final long serialVersionUID = 1L;

    private Any resolved_;
    private Any root_;
    private Any param_;   // this is set to the current node
    private Any paramOut_;
    private int vectorNumber_ = -2; // set if we did an Index operation
  	//private boolean  resolvingTransaction_ = false;

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

    void reset()
    {
      root_     = null;
      param_    = null;
      paramOut_ = null;
      resolved_ = null;
      vectorNumber_ = -2;
    }
    
    int getVectorNumber()
    {
      return vectorNumber_;
    }
    
    void setVectorNumber(int vectorNumber)
    {
      vectorNumber_ = vectorNumber;
    }
    
    public void visitAnyByte (ByteI b)
    {
      resolved_ = b;
    }

    public void visitAnyChar (CharI c)
    {
      resolved_ = c;
    }

    public void visitAnyInt (IntI i)
    {
      resolved_ = i;
    }

    public void visitAnyShort (ShortI s)
    {
      resolved_ = s;
    }

    public void visitAnyLong (LongI l)
    {
      resolved_ = l;
    }

    public void visitAnyFloat (FloatI f)
    {
      resolved_ = f;
    }

    public void visitAnyDouble (DoubleI d)
    {
      resolved_ = d;
    }

    public void visitDecimal (Decimal d)
    {
      resolved_ = d;
    }

    public void visitAnyString (StringI s)
    {
      resolved_ = s;
    }

    public void visitFunc (Func f)
    {
      // Note that LocateNode does not clone the NodeSpecification
      // since this is read-only for simple token-based paths.
      // If we hit on a func then clone before executing.
      f = (Func)f.cloneAny();
      f.setTransaction(getTransaction());
      // pass the current node on to the embedded function
      f.setParam(param_);
      
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

      if (a == null)
      	throw new AnyRuntimeException("Substitution did not resolve " + f);
      
      // NOTE - Vector access returns AnyNull.instance() if [first]
      // or [last] are used and do not resolve. This is just to silence
      // IndexOutOfBounds and the exception above in these cases as
      // their use is intended to be tolerated. Stretching things a bit,
      // watch out for bugs!
      //if (a ==  AnyNull.instance())
        //a = null;
      
      if (a != null && a != AnyNull.instance())
        a.accept(this);

      // only remember any parameter from the outermost function
      if (a != AnyNull.instance())
        paramOut_ = f.getParam();
      
      // Also set the column number for those node spec functions
      // that use it.   At the moment this is just the Index
      // function, which places the resolved index there
      vectorNumber_ = f.getColumn();
    }

    public void visitAnyDate (AnyDate d)
    {
      resolved_ = d;
    }

    public void visitMap (Map m)
    {
      resolved_ = m;
    }

    public void visitArray (Array a)
    {
      resolved_ = a;
    }

    public void visitSet (Set s)
    {
      resolved_ = s;
    }

    public void visitAnyObject (ObjectI o)
    {
      resolved_ = o;
    }

    public void visitUnknown(Any o)
    {
      resolved_ = o;
    }
  }

  /**
   * A function that will be placed within a <code>NodeSpecification</code>
   * as a result of using the <code>[]</code> syntax.  It returns its
   * resulting node through <code>getParam()</code>
   */
  public static class Index extends    AbstractFunc
                            implements Cloneable
  {
    private static final long serialVersionUID = 1L;

    private Any       index_;
    private EvalIndex ei_;
    private Any       root_;
    private Vectored  node_;
    private Any       res_;

    private boolean   vectorBuild_ = false;

    private int       resolvedIndex_ = -2;

    /**
     * Perform a vectored retrieval of the next node.
     */
    Index(Any a)
    {
      index_ = a;
      setColumn(-2);

      // Officially constructed instances of this class are never
      // used in anger - they are cloned as that part of the
      // NodeSpecification is processed.  Accordingly other
      // members are only set up in clone() below.
    }

    public Any exec (Any a) throws AnyException
    {
      // remember the execution context for the inner class
      root_ = a;

      // Evaluate the index expression leaving the result in
      // resolvedIndex_
      ei_.setTransaction(getTransaction());
      index_.accept(ei_);
      ei_.setTransaction(Transaction.NULL_TRANSACTION);

      if (!vectorBuild_)
      {
        if (node_.entries() != 0)
        {
          // reuse the column variable to save the resolved index.
          setColumn(resolvedIndex_);
          
          if (resolvedIndex_ == -1)
            res_ = node_.getByVector(node_.entries()-1);  // last
          else if (resolvedIndex_ == -3) // first
          {
            res_ = node_.getByVector(0);
          }
          else
          {
            //AbstractAny.stackTrace();
            //System.out.println("GETTING " + resolvedIndex_);
            res_ = node_.getByVector(resolvedIndex_);
            //System.out.println("GOT " + res_);
          }
        }
        else
        {
          if (resolvedIndex_ == -3) // "first" is like [0] but without exception
            res_ = AnyNull.instance();
          else
            throw new IndexOutOfBoundsException("Vector is empty");
        }

        return res_;
      }
      else
      {
        return null;
      }
    }

    public Any getParam()
    {
    	// exec() has run already
    	return res_;
    }

    public void setParam(Any a)
    {
      if (!(a instanceof Vectored))
        throw new IllegalArgumentException("Index must apply to Vectored");

      node_ = (Vectored)a;
    }

    public Object clone() throws CloneNotSupportedException
    {
      Index i = (Index)super.clone();

      // index_ might be an expression, which are generally not
      // thread safe.
      i.index_ = index_.cloneAny();

      // Set up the inner class.  See ctor comments above
      i.init();

      return i;
    }

    public void setVectorBuild(boolean vectorBuild)
    {
      vectorBuild_ = vectorBuild;
    }

    public int doVectorBuild(Any toAdd)
    {
      if (resolvedIndex_ == -1)
      {
        node_.addByVector(toAdd);  // add at end
        return node_.entries() - 1;
      }
      else
      {
        node_.addByVector(resolvedIndex_, toAdd);
        return resolvedIndex_;
      }
    }
    
    public String toString()
    {
    	StringBuffer s = new StringBuffer();
    	s.append(NodeSpecification.indexOpen__);
    	s.append(index_.toString());
    	s.append(NodeSpecification.indexClose__);
    	return s.toString();
    }

    private void init()
    {
      ei_ = new EvalIndex();
    }

    // Evaluate the index represented by outer class
    private class EvalIndex extends AbstractVisitor
    {
      private static final long serialVersionUID = 1L;

      public void visitAnyByte (ByteI b)
      {
        resolvedIndex_ = (int)b.getValue();
      }

      public void visitAnyChar (CharI c)
      {
        resolvedIndex_ = (int)c.getValue();
      }

      public void visitAnyInt (IntI i)
      {
        resolvedIndex_ = i.getValue();
      }

      public void visitAnyShort (ShortI s)
      {
        resolvedIndex_ = (int)s.getValue();
      }

      public void visitAnyLong (LongI l)
      {
        long val = l.getValue();
        if (val <= Integer.MAX_VALUE)
          resolvedIndex_ = (int)l.getValue();
      }

      public void visitAnyFloat (FloatI f)
      {
        float val = f.getValue();
        if (val <= Integer.MAX_VALUE)
          resolvedIndex_ = (int)f.getValue();
      }

      public void visitAnyDouble (DoubleI d)
      {
        double val = d.getValue();
        if (val <= Integer.MAX_VALUE)
          resolvedIndex_ = (int)d.getValue();
      }

      public void visitDecimal (Decimal d)
      {
        double val = d.getValue().doubleValue();
        if (val <= Integer.MAX_VALUE)
          resolvedIndex_ = (int)d.getValue().doubleValue();
      }

      public void visitAnyString (StringI s)
      {
      	if (s.toString().equals("first"))
      	  resolvedIndex_ = -3;
      	else if (s.toString().equals("last"))
      	  resolvedIndex_ = -1;
      	else
          resolvedIndex_ = Integer.parseInt(s.getValue());
      }

      public void visitFunc (Func f)
      {
        f.setTransaction(getTransaction());
        Any a = null;
        try
        {
          a = f.execFunc(root_);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }

        if (a != null)
          a.accept(this);
      }
    }
  }

  /**
   * A function that will be placed within a <code>NodeSpecification</code>
   * as a result of using the <code>()</code> syntax.  It returns its
   * resulting node through <code>getParam()</code>
   */
  public static class Indirect extends    AbstractFunc
                               implements Cloneable
  {
    private static final long serialVersionUID = 1L;

    private Any          indirect_;
    private EvalIndirect ei_;
    private Any          root_;
    private Any          res_;

    /**
     * Perform an indirect retrieval of the next node.
     */
    Indirect(Any a)
    {
      indirect_ = a;
      setColumn(-2);

      // Officially constructed instances of this class are never
      // used in anger - they are cloned as that part of the
      // NodeSpecification is processed.  Accordingly other
      // members are only set up in clone() below.
    }

    public Any exec (Any a) throws AnyException
    {
      // remember the execution context for the inner class
      root_ = a;

      // Evaluate the indirect expression leaving the result in
      // res_
      ei_.setTransaction(getTransaction());
      indirect_.accept(ei_);
      ei_.setTransaction(Transaction.NULL_TRANSACTION);

      return res_;
    }

    public Any getParam()
    {
    	// exec() has run already
    	return res_;
    }

    public Object clone() throws CloneNotSupportedException
    {
      Indirect i = (Indirect)super.clone();

      // index_ might be an expression, which are generally not
      // thread safe.
      i.indirect_ = indirect_.cloneAny();

      // Set up the inner class.  See ctor comments above
      i.init();

      return i;
    }

    private void init()
    {
      ei_ = new EvalIndirect();
    }

    private class EvalIndirect extends AbstractVisitor
    {
      private static final long serialVersionUID = 1L;

      public void visitAnyBoolean (BooleanI b)
      {
        res_ = b;
      }

      public void visitAnyByte (ByteI b)
      {
        res_ = b;
      }

      public void visitAnyChar (CharI c)
      {
        res_ = c;
      }

      public void visitAnyInt (IntI i)
      {
        res_ = i;
      }

      public void visitAnyShort (ShortI s)
      {
        res_ = s;
      }

      public void visitAnyLong (LongI l)
      {
        res_ = l;
      }

      public void visitAnyFloat (FloatI f)
      {
        res_ = f;
      }

      public void visitAnyDouble (DoubleI d)
      {
        res_ = d;
      }

      public void visitDecimal (Decimal d)
      {
        res_ = d;
      }

      public void visitAnyString (StringI s)
      {
        res_ = s;
      }

		  public void visitAnyDate (AnyDate d)
		  {
        res_ = d;
		  }

		  public void visitMap (Map m)
		  {
        res_ = m;
		  }

		  public void visitArray (Array a)
		  {
        res_ = a;
		  }

		  public void visitAnyObject (ObjectI o)
		  {
        res_ = o;
		  }

		  public void visitUnknown(Any o)
		  {
        res_ = o;
		  }

      public void visitFunc (Func f)
      {
        f.setTransaction(getTransaction());
        Any a = null;
        try
        {
          a = f.execFunc(root_);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }

        res_ = a;

        if (a != null)
          a.accept(this);
      }
    }
  }

  /**
   * A function that will be placed within a <code>NodeSpecification</code>
   * as a result of using the <code>||</code> syntax.  It returns its
   * resulting group map through <code>getParam()</code>.
   * <p>
   * The group specification may only appear as the last component of a
   * node specification and results in the specified field(s) being
   * placed in a slave map for group assignment, testing etc.
   */
  public static class Group extends    AbstractFunc
                            implements Cloneable
  {
    private Any          group_;
    private EvalGroup    eg_;
    private Any          root_;
    private Map          res_;
    private Map          currentNode_;
    private Array        fields_;

    /**
     * Perform an indirect retrieval of the next node.
     */
    Group(Any a)
    {
      group_ = a;
      setColumn(-2);

      // Officially constructed instances of this class are never
      // used in anger - they are cloned as that part of the
      // NodeSpecification is processed.  Accordingly other
      // members are only set up in clone() below.
    }

    public Any exec (Any a) throws AnyException
    {
      // remember the execution context for the inner class
      root_ = a;
      
      // set up the map to hold the field group
      res_ = AbstractComposite.simpleMap();

      // Evaluate the group expression leaving the result in
      // res_
      eg_.setTransaction(getTransaction());
      group_.accept(eg_);

      // res_ contains desired keys, put in the values from
      return res_;
    }

	  public Any doTransactionHandling (Any root, Any a) throws AnyException
	  {
	  	// res_ is already created and has the public members
	  	// in it.  We remembered the fields in fields_ to save
	  	// creating an iterator here, and to avoid concurrent
	  	// modification problems.
	  	Map m = getTransaction().getTransInstance(currentNode_);
	  	if (m != currentNode_)
	  	{
		  	for (int i = 0; i < fields_.entries(); i++)
		    {
		       res_.replaceItem(fields_.get(i),
		                        m.get(fields_.get(i)));
		    }
	  	}
	  	return res_;
	  }
	  
    public Any getParam()
    {
    	// exec() has run already
    	return res_;
    }

    public void setParam(Any a)
    {
      if (!(a instanceof Map))
        throw new IllegalArgumentException("Group must apply to Map");

    	currentNode_ = (Map)a;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
      Group i = (Group)super.clone();

      // group_ might be an expression, which are generally not
      // thread safe.
      i.group_ = group_.cloneAny();

      // Set up the inner class.  See ctor comments above
      i.init();

      return i;
    }

    private void init()
    {
      eg_     = new EvalGroup();
      fields_ = AbstractComposite.array();
    }

    private class EvalGroup extends AbstractVisitor
    {
      // If we resolve the group specification to a scalar then
      // assume that scalar to be the single field name.
      public void visitAnyBoolean (BooleanI b)
      {
        res_.add(b, currentNode_.get(b));
        fields_.add(b);
      }

      public void visitAnyByte (AnyByte b)
      {
        res_.add(b, currentNode_.get(b));
        fields_.add(b);
      }

      public void visitAnyChar (CharI c)
      {
        res_.add(c, currentNode_.get(c));
        fields_.add(c);
      }

      public void visitAnyInt (IntI i)
      {
        res_.add(i, currentNode_.get(i));
        fields_.add(i);
      }

      public void visitAnyShort (ShortI s)
      {
        res_.add(s, currentNode_.get(s));
        fields_.add(s);
      }

      public void visitAnyLong (LongI l)
      {
        res_.add(l, currentNode_.get(l));
        fields_.add(l);
      }

      public void visitAnyFloat (FloatI f)
      {
        res_.add(f, currentNode_.get(f));
        fields_.add(f);
      }

      public void visitAnyDouble (DoubleI d)
      {
        res_.add(d, currentNode_.get(d));
        fields_.add(d);
      }

      public void visitDecimal (Decimal d)
      {
        res_.add(d, currentNode_.get(d));
        fields_.add(d);
      }

      public void visitAnyString (StringI s)
      {
        res_.add(s, currentNode_.get(s));
        fields_.add(s);
      }

		  public void visitAnyDate (AnyDate d)
		  {
        res_.add(d, currentNode_.get(d));
        fields_.add(d);
		  }

		  public void visitMap (Map m)
		  {
		  	// add keys from given map
		  	Iter i = m.createKeysIterator();
		  	while (i.hasNext())
		  	{
		  		Any k = i.next();
          res_.add(k, currentNode_.get(k));
          fields_.add(k);
		  	}
		  }

		  public void visitArray (Array a)
		  {
		  	for (int i = 0; i < a.entries(); i++)
		  	{
	        res_.add(a.get(i), currentNode_.get(a.get(i)));
          fields_.add(a.get(i));
		  	}
		  }

		  public void visitAnyObject (ObjectI o)
		  {
        res_.add(o, currentNode_.get(o));
        fields_.add(o);
		  }

		  public void visitUnknown(Any o)
		  {
        res_.add(o, currentNode_.get(o));
        fields_.add(o);
		  }

      public void visitFunc (Func f)
      {
        f.setTransaction(getTransaction());
        Any a = null;
        try
        {
          a = f.execFunc(root_);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }

        if (a != null)
          a.accept(this);
      }
    }
  }
}
