/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Read.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Attempt to read the specified Managed Object type using the given key
 * value.
 * <P>
 * The <code>Read</code> function can be used to start the building of
 * or augment an existing <code>any</code> structure.
 * The instance or instances returned are stored as the 
 * given <code>instanceAlias</code> or as the default alias
 * for the object type if none is specified.  If a set of objects are
 * returned these are stored in an <code>InstanceHierarchyMap</code>,
 * again with 
 * any <code>instanceAlias</code>.
 * <P>
 * The results are stored at the
 * as the given alias, at the node given by <code>target</code>
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 */
public class Read extends    AbstractFunc
									implements Cloneable
{
  private Any descriptor_;  // Managed object descriptor
  private Any keyVal_;      // key value
  private Any keyName_;     // key name (optional)
  private Any instanceAlias_;  // optional name for the node-set child
  private Any typedefAlias_;  // optional name for the typedef instance
  private Any merge_;
  private Any setAlias_;
  private Any target_;
  private Any child_;
  private Any maxCount_;
    
  /**
   * Read objects of the type given by the descriptor using the
   * specified key name and value.  The instance or instances
   * returned are stored as the given target.
   */
  public Read(Any descriptor,
							Any keyVal,
							Any keyName,
							Any target,
							Any setAlias)
  {
    descriptor_     = descriptor;
    keyVal_         = keyVal;
    keyName_        = keyName;
    target_         = target;
    setAlias_       = setAlias;
  }

  public Read(Any descriptor,
              Any keyVal,
              Any keyName,
              Any setAlias)
  {
    this(descriptor,
				 keyVal,
				 keyName,
				 null,
				 setAlias);
  }

  public Read(Any descriptor, Any keyVal)
  {
    this(descriptor, keyVal, null, null, null);  // Other params are filled in by XSL
  }

  public Read(Any descriptor)
  {
    this(descriptor, null, null, null, null);  // Other params are filled in by XSL
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
  	//System.out.println ("Read.exec() : descriptor : " + descriptor_);
		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(t,
		                                   a,
		                                   descriptor_,
		                                   Descriptor.class);
    
    if (descriptor == null)
      nullOperand(descriptor_);

		Map keyVal     = (Map)EvalExpr.evalFunc
																			(t,
		                                   a,
		                                   keyVal_,
		                                   Map.class);
		if (keyVal == null)
      nullOperand(keyVal_);

    Any keyName    = EvalExpr.evalFunc(t,
		                                   a,
		                                   keyName_);

    // This is a bit messy - if there is no explicit key name
    // then the parser puts in defaultKey__ (which is "unique").
    // If the supplied keyVal is a key instance it will have the
    // name in it (contains KeyDef.key__).  So far, so good.  However
    // may be the script wants to override the keyname with an
    // explicit one no matter how the key value was specified.  Hence
    // the below.
    if (keyVal.contains(KeyDef.key__))
    {
      if (keyName.equals(KeyDef.defaultKey__))
        keyName = keyVal.get(KeyDef.key__);
      else if (keyName.equals(KeyDef.primaryKey__))
        keyName = KeyDef.defaultKey__;
    }
    
		Any setAlias   = EvalExpr.evalFunc(t,
		                                   a,
		                                   setAlias_);

    Any typedefAlias = EvalExpr.evalFunc(getTransaction(),
        a,
        typedefAlias_);

    Map child = (Map)EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       child_,
                                       Map.class);

    IntI maxCount = (IntI)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           maxCount_,
                                           IntI.class);
    
    if (maxCount == null && maxCount_ != null)
      nullOperand(maxCount_);
    
    int maxCountI = -1;
    if (maxCount != null)
    {
      maxCountI = maxCount.getValue();
      if (maxCountI < 0)
        throw new AnyException("max count must be >= 0 not " + maxCount);
    }
    
        
    //System.out.println ("Read.exec() target_ " + target_);
		Map target     = (Map)EvalExpr.evalFunc
																			(t,
		                                   a,
		                                   target_,
		                                   Map.class);
    //System.out.println ("Read.exec() target " + target);
    if (target == null)
      nullOperand(target_);
		                                   
    Any result;
    
    //System.out.println ("Read.exec() descriptor is " + descriptor);
    KeyDef kd = null;
		if (keyName == null)
		{
      kd     = descriptor.locateRetrievalKey(keyVal);
      if (kd.shouldCache() && maxCountI > 0)
        throw new AnyException("Cannot use capped read with cached keys");
		  result = descriptor.read(t.getProcess(), kd, keyVal, maxCountI);
		}
		else
		{
      //System.out.println ("Read.exec() descriptor is " + descriptor);
      //System.out.println ("Read.exec() descriptor_ is " + descriptor_);
      //System.out.println ("Read.exec() txn is " + getTransaction());
      //System.out.println ("Read.exec() process is " + getTransaction().getProcess());
		  kd     = descriptor.getKey(keyName);
		  
		  if (kd.shouldCache() && maxCountI > 0)
		    throw new AnyException("Cannot use capped read with cached keys");
		  
		  result = descriptor.read(t.getProcess(), kd, keyVal, maxCountI);
		}
		
		if ((kd != null && !kd.isUnique()) || result instanceof Array)
		{
      // When constructed from the parser, there is always a target.
      // By default it is the stack.
			if (target == null)
				target = AbstractComposite.managedMap();

			if (setAlias == null)
			  throw new IllegalArgumentException("no setAlias given");

			// If we were returned an Array then this will be a simple
			// set of the instances.  For a valid composite structure
			// these must be placed in a two-level map with level-1
			// labels the unique keys of the instances and level-2
			// labels of typedef alias
			Map   nodeSet = (Map)target.buildNew(null);
			nodeSet.setNodeSet(descriptor.getFQName());
			
			Array ar      = (Array)result;
			if (ar != null)
			{
        // Check if there's a row alias fn and if so, evaluate it
				// against each instance returned.
        AnyFuncHolder.FuncHolder rowAlias =
          (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                            a,
                                            instanceAlias_,
                                            AnyFuncHolder.FuncHolder.class);

				for (int i = 0; i < ar.entries(); i++)
				{
					Map instance = (Map)ar.get(i);
          
          // If instance is delete-marked then omit from results.
          if (t.isDeleteMarked(instance))
            continue;
            
					Map m;
          if (child == null)
            m = (Map)target.buildNew(null);
          else
            m = (Map)child.buildNew(null);
					
          // Default row alias is the unique key of the instance
          // itself.  If an instanceAlias_ is provided then this
          // is evaluated with $loop as the instance and used
          // instead.
					Any instanceAlias = instance.getUniqueKey();
          if (rowAlias != null)
          {
            Any curLoop = t.getLoop();
            t.setLoop(instance);
            try
            {
              instanceAlias = rowAlias.doFunc(t, null, a);
            }
            finally
            {
              t.setLoop(curLoop);
            }
            instanceAlias = AbstractAny.ripSafe(instanceAlias, t);
            if (instanceAlias == null)
              throw new AnyException("Row alias evaluated to null");
          }

          if (typedefAlias == null)
            typedefAlias = descriptor.getDefaultAlias();
          
					m.add(typedefAlias, instance);
					m.setUniqueKey(instanceAlias);
	
					nodeSet.add(instanceAlias, m);
          
				}
        
				// Check if the node set has been capped, either by a
				// configured cap not overridden or by an explicit one
				if (maxCountI > 0 || (kd.getMaxCount() > 0 && maxCountI < 0))
				{
				  if (maxCountI < 0)
				    maxCountI = kd.getMaxCount();
          
          if (nodeSet.entries() > maxCountI)
          {
            nodeSet.add(NodeSpecification.atCapped__, new AnyInt(nodeSet.entries()));
          }
				}
			}
      if (target.contains(setAlias))
      {
        Any merge   = EvalExpr.evalFunc(t,
                                        a,
                                        merge_);
        
        if (merge == null && merge_ != null)
          nullOperand(merge_);
        
        if (merge != null)
        {
          BooleanI b = new ConstBoolean(merge);
          merge = b.getValue() ? b : null;
        }
        if (merge != null)
        {
          // merge the results of this read to prior existing set
          Map prior = (Map)target.get(setAlias);
          Iter i = nodeSet.createKeysIterator();
          while (i.hasNext())
          {
            Any k = i.next();
            
            if (!prior.contains(k))
            {
              Any v = nodeSet.get(k);
              i.remove();
              prior.add(k, v);
            }
          }
        }
        else
          target.replaceItem(setAlias, nodeSet);
      }
      else
      {
        target.add(setAlias, nodeSet);
      }
			//System.out.println ("Read.exec() added " + nodeSet + " to target " + target);
      return nodeSet;
		}
		else
		{
			// If we were returned a single instance then add to any target
			// as the alias...
			if (result != null)
			{
        if (t.isDeleteMarked((Map)result))
          return AnyNull.instance();
          
//				if (target == null)
//					target = AbstractComposite.managedMap();
        
        if (typedefAlias == null)
          typedefAlias = descriptor.getDefaultAlias();
        
        target.replaceItem (typedefAlias, result);
        
        return result;
			}
			else
			  return AnyNull.instance();
			
			//System.out.println ("Read.exec() added " + result + " to target " + target);
			
		}

//    System.out.println("Read.exec target is " + target);
  }
  
  public void setKeyVal(Any keyVal)
  {
		keyVal_ = keyVal;
  }

  public void setMerge(Any merge)
  {
    merge_ = merge;
  }

  public void setMaxCount(Any maxCount)
  {
    maxCount_ = maxCount;
  }
  
  public void setChild(Any child)
  {
    child_ = child;
  }

  public void setSetAlias(Any setAlias)
  {
		setAlias_ = setAlias;
  }

  public void setTypedefAlias(Any typedefAlias)
  {
    typedefAlias_ = typedefAlias;
  }
  
  public void setRowAlias(Any rowAlias)
  {
		instanceAlias_ = rowAlias;
  }

  public void setTarget(Any target)
  {
		target_ = target.cloneAny();
  }

  public void setKeyName(Any keyName)
  {
		keyName_ = keyName;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(descriptor_);
  	a.add(keyVal_);
  	a.add(keyName_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    Read r = (Read)super.clone();
    
    r.descriptor_    = descriptor_.cloneAny();        
    r.keyVal_        = AbstractAny.cloneOrNull(keyVal_);
    r.setAlias_      = AbstractAny.cloneOrNull(setAlias_);
    r.instanceAlias_ = AbstractAny.cloneOrNull(instanceAlias_);
    r.typedefAlias_  = AbstractAny.cloneOrNull(typedefAlias_);
    r.keyName_       = AbstractAny.cloneOrNull(keyName_);
    r.target_        = AbstractAny.cloneOrNull(target_);
    r.merge_         = AbstractAny.cloneOrNull(merge_);
    r.child_         = AbstractAny.cloneOrNull(child_);
    r.maxCount_      = AbstractAny.cloneOrNull(maxCount_);
    
    return r;
  }
}

/**
 * A simple implementation of the Locate interface which supports
 * the requirement of placing single read results in a new map
 * when no target was specified while at the same time making this
 * returned structure suitable as an operand to enclosing functions
class ReadLocateNode extends    AbstractFunc
														 implements Locate
{
	Array arrayAncestor_;
	Map   parent_;
	Any   item_;
	
	ReadLocateNode (Array arrayAncestor, Map parent, Any item)
	{
		arrayAncestor_ = arrayAncestor;
		parent_        = parent;
		item_          = item;
	}
	
	ReadLocateNode (Map parent, Any item)
	{
		this(null, parent, item);
	}
	
  public Array getArrayParent() { return null; }
  public Map getMapParent() { return parent_; }
  public Array hasArrayParent() { return arrayAncestor_; }
  
  public Any exec(Any root) { return item_; }
}
 */
