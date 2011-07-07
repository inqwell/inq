/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Aggregate.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Attempt to aggregate the specified Managed Object
 * instance to the specified Managed Object type with an
 * optional key name.
 * <p>
 * Together with <code>Read</code>, this class defines and enforces the
 * rules by which INQ structures are built.  These are
 * <ul>
 * <li>
 * A managed object instance cannot appear as a direct child of an Array.
 * A Map must be interposed so that the managed object has a name.  That name
 * can either be specified as the structure is created or can be allowed to
 * default to a pre-configured value.
 * <li>
 * Array content will be homogeneous, i.e. if a managed object instance
 * has an array ancestor then any aggregation on that instance will apply
 * to all similar instances under the array.
 * </ul>
 * <p>
 * The <code>Aggregate</code> and <code>Read</code> operations are the way
 * in which INQ structures are built
 * up, in combination with other operations like Filter and so forth.
 * A network of these Func implementations can be assembled to build a working
 * set of data for subsequent use by Func implementations specific to
 * the application.
 * @author $Author: sanderst $
 * @version $Revision: 1.8 $
 */
public class Aggregate extends    AbstractFunc
											 implements Cloneable
{
  private static final long serialVersionUID = 1L;

  private Any    descriptor_;     // Managed object descriptor
  private Locate instanceFrom_;   // key value
  private Any    keyName_;        // key name (optional)
  private Any    keyMap_;         // key map (optional)
  private Any    setAlias_;       // set alias (optional but causes an error
                                  //            if the aggregation yields a
                                  //            set and none was provided)
  private Any typedefAlias_;      // optional name for the typedef instance
  private Any mustJoin_;          // whether the join must succeed for
                                  // the child node to remain in the set
                                  // (applies only to set aggregations, default
                                  // is false)
  private Any key_;               // an expression that will be executed
                                  // to yield the key value instead of
                                  // using instanceFrom_ (optional)
  private Any foreach_;           // an expression that is exceuted
                                  // when aggregating from sets after
                                  // each [iterated] aggregate operation
                                  // has been performed (optional)
  private Any rowName_;           // the row name in the resulting
                                  // node set for aggregations that
                                  // use a non-unique key
  private Any maxCount_;          // Optional - maximum # instances to agg. to
  
  private Any nodeSetParent_;

	/**
	 * Aggregate using a specified key of the target.  The new instance(s)
	 * will be keyed by the <code>instanceAlias</code> parameter or by the
	 * descriptor's alias if none is provided.  If a set is
	 * yielded then this will be known by the <code>setAlias</code>.   It is
	 * an error if no <code>setAlias</code> is provided and the aggregation
	 * results in a set.
	 */
  public Aggregate(Locate instanceFrom,
                   Any    descriptor,
                   Any    keyName,
                   Any    setAlias)
  {
    descriptor_    = descriptor;
    instanceFrom_  =  new LocateNode(instanceFrom.getNodePath())
    {
      protected void nodeFound(Any pathItem, Any node, Any parent)
      {
        // Remember the parent of the last node set traversed (if any)
        if (node instanceof Map)
        {
          Map m = (Map)node;
          if (m.getNodeSet() != null)
            nodeSetParent_ = parent;
        }
      }
    };
    
    keyName_       = keyName;
		setAlias_      = setAlias;
  }

	/**
	 * Aggregate using a specified key in the target.  The new instance(s)
	 * will be keyed by the <code>descriptor.getName()</code> parameter in
	 * their container.
	 * <p>
	 * Note that by not supplying an alias the instancs(s) created by this
	 * operation will be known by their descriptor name.  This means that
	 * any node specification that specifies this part of the locate path
	 * will necessarily be tied to the configured descriptor name.  In MDA
	 * this name is the instance's 'type' (or class) in old money.
	 * Any subsequent operations on the structure would then be specifying
	 * this type name, which partially defeats the point of MDA in the first
	 * place.  We thus encourage the use of aliases so that application Funcs
	 * can work on structures comprising arbitrary types.
	 */
  public Aggregate(Locate instanceFrom, Any descriptor, Any keyName)
  {
    this(instanceFrom, descriptor, keyName, null);
  }

	/**
	 * Aggregate using a specified key in the target.  The new instance(s)
	 * will be keyed by <code>descriptor.getName()</code> in their container.
	 * The key used will be searched for in the target descriptor from the
	 * source instance.
	 */
  public Aggregate(Locate instanceFrom, Any descriptor)
  {
    this(instanceFrom, descriptor, null, null);
  }

  public Aggregate(Any descriptor)
  {
		this(null, descriptor, null, null);
  }
  
	/**
	 * Perform the aggregation defined by this instance.  The returned object
	 * is the root of the new structure
	 */
  public Any exec(Any a) throws AnyException
  {
		// Validation of the instanceFrom argument is a bit more complex
		// than just the correct class so defer.  Our transaction context
		// returns any private instance of a managed object.
		Map instanceFrom      = (Map)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   instanceFrom_,
		                                   Map.class);
    
    // If no instanceFrom silently ignore
    if (instanceFrom == null)
      return null;

		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   descriptor_,
		                                   Descriptor.class);

		Any keyName           = EvalExpr.evalFunc(getTransaction(),
																							a,
																							keyName_);
		
		Map keyMap            = (Map)EvalExpr.evalFunc(getTransaction(),
																							a,
																							keyMap_,
                                              Map.class);
		
		Any setAlias          = EvalExpr.evalFunc(getTransaction(),
																							a,
																							setAlias_);

    Any typedefAlias      = EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              typedefAlias_);

		Any mustJoin          = EvalExpr.evalFunc(getTransaction(),
																							a,
																							mustJoin_);

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

    AnyBoolean bMustJoin  = new AnyBoolean(mustJoin);

		AnyFuncHolder.FuncHolder key = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
                                                (getTransaction(),
                                                 a,
                                                 key_,
																								 AnyFuncHolder.FuncHolder.class);

    AnyFuncHolder.FuncHolder foreachF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  foreach_,
                                                  AnyFuncHolder.FuncHolder.class);
    
    // Check if there's a row alias fn and if so, evaluate it
		// against each instance returned.
    AnyFuncHolder.FuncHolder rowNameF =
      (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(getTransaction(),
                                        a,
                                        rowName_,
                                        AnyFuncHolder.FuncHolder.class);

		Any result = processInstance(a,
                                 getTransaction(),
                                 instanceFrom,
		                             descriptor,
		                             keyName,
                                 keyMap,
		                             setAlias,
                                 typedefAlias,
                                 bMustJoin.getValue(),
                                 key,
                                 foreachF,
                                 rowNameF,
                                 maxCountI);
    
	  return result;
  }
  
  public void setSetAlias(Any setAlias)
  {
		setAlias_ = setAlias;
  }

  public void setTypedefAlias(Any typedefAlias)
  {
    typedefAlias_ = typedefAlias;
  }
  
  public void setKeyName(String keyName)
  {
		keyName_ = new ConstString(keyName);
	}
	
  public void setKeyName(Any keyName)
  {
		keyName_ = keyName;
  }

  public void setKeyMap(Any a)
  {
    keyMap_ = a;
  }
  
  public void setKey(Any a)
  {
    key_ = a;
  }
  
  public void setMaxCount(Any maxCount)
  {
    maxCount_ = maxCount;
  }
  
  public void setForeach(Any a)
  {
    foreach_ = a;
  }
  
  public void setMustJoin(Any a)
  {
    mustJoin_ = a;
  }
  
  public void setRowName(Any rowName)
  {
		rowName_ = rowName;
  }

  public Object clone () throws CloneNotSupportedException
  {
    Aggregate a = (Aggregate)super.clone();
    
    a.descriptor_     = descriptor_.cloneAny();
    a.instanceFrom_   = (Locate)instanceFrom_.cloneAny();
    a.keyName_        = AbstractAny.cloneOrNull(keyName_);
    a.key_            = AbstractAny.cloneOrNull(key_);
    a.foreach_        = AbstractAny.cloneOrNull(foreach_);
    a.keyMap_         = AbstractAny.cloneOrNull(keyMap_);
    a.rowName_        = AbstractAny.cloneOrNull(rowName_);
    a.setAlias_       = AbstractAny.cloneOrNull(setAlias_);
    a.mustJoin_       = AbstractAny.cloneOrNull(mustJoin_);
    a.nodeSetParent_  = null;
     
    return a;
  }
  
	// We have located an instance from which we wish to aggregate.  There are
	// some possibilities:
	//    1) The instance has node set ancestor, so we process all children
	//       of this ancestor.  Qv processSet - because of the way the
	//       structures are defined we can assume that the immediate child of
	//       the map ancestor is itself a map and is the map where
	//       we should place the aggregation result(s);
	//    2) The instance has no node set ancestor but already has a map
	//       immediate parent so we just process that instance and place
	//       the result(s) into the map parent
	private Any processInstance(Any         context,
                              Transaction t,
                              Map         instanceFrom,
															Descriptor  d,
															Any         keyName,
                              Map         keyMap,
															Any         setAlias,
                              Any         typedefAlias,
                              boolean     mustJoin,
                              AnyFuncHolder.FuncHolder key,
                              AnyFuncHolder.FuncHolder foreachF,
                              AnyFuncHolder.FuncHolder rowNameF,
                              int         maxCount) throws AnyException
	{
		Any ret           = null;
		Map parentNodeSet = null;
		Map parentMap     = null;
		
		parentNodeSet = instanceFrom_.getLastNodeSet();
    
		KeyDef kd = null;
		if (keyName != null)
		{
		  if (keyName.equals(KeyDef.primaryKey__))
		    kd = d.getKey(KeyDef.defaultKey__);
		  else
			  kd = d.getKey(keyName);
		}
		else
		{
			// There is always a keyName as the parser defaults it
			// to "unique", so this branch is never executed. Overall,
			// the locateRelationshipKey is looking suspect
			kd = d.locateRelationshipKey(instanceFrom.getDescriptor());
		}
		
    Map ii = null;
    if (keyMap != null)
    {
      ii = AbstractComposite.simpleMap();
      Iter i = keyMap.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        ii.add(keyMap.get(k), instanceFrom.get(k).cloneAny());
      }
    }
    
    Iter currentIter = t.getIter();
    Any  currentLoop = t.getLoop();
    
    try
    {
      if (parentNodeSet != null)
  		{
  			// We located an instance and it is under a node representing
  			// a homogeneous set.
  			// Case 1 above
  			Iter i = parentNodeSet.createIterator();
  			
  			// aggLocater_ has been used so getPath() returns the final
  			// element.  This can be used from the node set's child composites
  			// to reach the instances from which we are aggregating.
  			Any path = instanceFrom_.getPath();
  
        while (i.hasNext())
  			{
  				Map parent = (Map)i.next();
  				t.setIter(i);
  				t.setLoop(parent);
  				
  				Map instance = (Map)parent.get(path);
  				boolean joined = expandAggregation(context,
                                             parent,
                                             instance,
                                             d,
                                             kd,
                                             keyName,
                                             keyMap,
                                             ii,
                                             setAlias,
                                             typedefAlias,
                                             key,
                                             rowNameF,
                                             maxCount);
          if (mustJoin && !joined)
            i.remove();
          else if (foreachF != null)
          {
            foreachF.doFunc(getTransaction(), null, context);
          }
  			}
  
        //ret = parentNodeSet.getParentAny();
        ret = nodeSetParent_;
      
  		}
  		else if ((parentMap = instanceFrom_.getMapParent()) != null)
  		{
  			// We located an instance and it has a map parent with no
  			// node-set ancestor.  Case 2.

  		  // Set the 'loop' even though there isn't one really. Makes
  		  // scripts consistent.
        t.setLoop(parentMap);

  			expandAggregation(context,
                          parentMap,
  			                  instanceFrom,
  			                  d,
  			                  kd,
  			                  keyName,
                          keyMap,
                          ii,
  			                  setAlias,
                          typedefAlias,
                          key,
                          rowNameF,
                          maxCount);
  			ret = parentMap;
  		}
    }
    finally
    {
      t.setIter(currentIter);
      t.setLoop(currentLoop);
    }
    
		return ret;
	}
	
	// expand from instance i to descriptor d using the key of keyName placing
	// result in parent p with name of instanceAlias or setAlias for a set
	private boolean expandAggregation (Any        context,
                                     Map        p,
                                     Map        i,
                                     Descriptor d,
                                     KeyDef     kd,
                                     Any        keyName,
                                     Map        keyMap,
                                     Map        ii,
                                     Any        setAlias,
                                     Any        typedefAlias,
                                     AnyFuncHolder.FuncHolder key,
                                     AnyFuncHolder.FuncHolder rowNameF,
                                     int        maxCount) throws AnyException
	{
    Transaction t = getTransaction();
    if (key != null)
    {
      Any a = key.doFunc(t, null, context);
      // Hard null not allowed
      if (a == null)
        throw new AnyException("key function cannot return null");
      
      // Soft null means don't bother
      if (AnyNull.isNullInstance(a))
        return false;
      
      if (!(a instanceof Map))
        throw new AnyException("key is not a map");
      
      i = (Map)a;
      
      // Check if the value returned is a real key. If it is
      // and we haven't already explicitly named the key to use
      // then use that specified in i. See also the comment
      // and code in Read.java to a similar effect.
      if (i.contains(KeyDef.key__))
      {
        if (keyName.equals(KeyDef.defaultKey__))
          kd = d.getKey(i.get(KeyDef.key__));
        else if (keyName.equals(KeyDef.primaryKey__))
          kd = d.getKey(KeyDef.defaultKey__);
      }
    }
      
    if (keyMap != null)
    {
      // key map has keys that are names of values in i
      // and values that they should be named as in ii
      Iter iter = keyMap.createKeysIterator();
      while (iter.hasNext())
      {
        Any k = iter.next();
        Any v = keyMap.get(k);
        ii.get(v).copyFrom(i.get(k));
      }
      i = ii;
    }
    
    if (kd.shouldCache() && maxCount > 0)
      throw new AnyException("Cannot use capped read with cached keys");
    
		Any result = d.read(t.getProcess(), kd, i, maxCount);
		
		if (!kd.isUnique())
		{
			if (setAlias == null)
			  throw new IllegalArgumentException("no setAlias given");

			// If we were returned an Array then this will be a simple
			// set of the instances.  For a valid composite structure
			// these must be placed in a map with labels of the supplied
			// (or default) alias.
			Map   nodeSet = (Map)p.buildNew(null);
			
			nodeSet.setNodeSet(d.getFQName());

      if (result != null)
      {
				Array ar = (Array)result;
				Any curLoop = t.getLoop();
				try
				{
  				for (int j = 0; j < ar.entries(); j++)
  				{
  					Map instance = (Map)ar.get(j);
  					t.setLoop(instance);
  					Map m        = (Map)p.buildNew(null);
  					
  					Any instanceAlias = instance.getUniqueKey();
            if (rowNameF != null)
            {
              instanceAlias = rowNameF.doFunc(t, null, context);
              if (instanceAlias == null)
                throw new AnyException("Row alias evaluated to null");
            }
  	
            if (typedefAlias == null)
              typedefAlias = d.getDefaultAlias();
            
  					m.add(typedefAlias, instance);
  					m.setUniqueKey(instanceAlias);
  	
  					nodeSet.add(instanceAlias, m);
  				}
				}
				finally
				{
  				t.setLoop(curLoop);
				}
				
        // Check if the node set has been capped, either by a
        // configured cap not overridden or by an explicit one
        if (maxCount > 0 || (kd.getMaxCount() > 0 && maxCount < 0))
        {
          if (maxCount < 0)
            maxCount = kd.getMaxCount();
          
          if (nodeSet.entries() > maxCount)
          {
            nodeSet.add(NodeSpecification.atCapped__, new AnyInt(nodeSet.entries()));
          }
        }
			}
			p.replaceItem(setAlias, nodeSet);
		}
		else
		{
			// If we were returned a single instance then add to any target
			// as the alias...
			if (result != null)
      {
        if (typedefAlias == null)
          p.add(d.getDefaultAlias(), result);
        else
          p.add(typedefAlias, result);
      }
//      else
//      {
//        if (setAlias == null)
//          p.add(d.getDefaultAlias(), d.newInstance());
//        else
//          p.add(setAlias, d.newInstance());
//      }
		}
    return result != null;
	}
}
