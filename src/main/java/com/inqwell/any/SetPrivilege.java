/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SetPrivilege.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Set the privilege levels on a node.
 * Sets the privilege levels of the specified node subject to the
 * following restrictions:
 * <BL>
 * <LI>A privilege level cannot be raised to a level above that of
 * the executing process.
 * <LI>A privilege level cannot be lowered if it is currently above
 * that of the executing process and also cannot be lowered unless
 * the node provilege is already lower than the process privilege.
 * </BL>
 */
public class SetPrivilege extends    AbstractFunc
								       implements Cloneable
{
  private Any node_;
  private Any privileges_;
  private Any merge_;
  private Any key_;

	/**
	 * 
	 */
  public SetPrivilege(Any node, Any privileges)
  {
    this(node, privileges, null, null);
  }

  public SetPrivilege(Any node, Any privileges, Any merge)
  {
    this(node, privileges, merge, null);
  }

  public SetPrivilege(Any node, Any privileges, Any merge, Any key)
  {
    node_        = node;
    privileges_  = privileges;
    merge_       = merge;
    key_         = key;
  }

  public Any exec(Any a) throws AnyException
  {
		Map node         = (Map)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              node_,
                                              Map.class);
		
		Map privileges   = (Map)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              privileges_,
                                              Map.class);

		Any key          = EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         key_);

		BooleanI merge = (BooleanI)EvalExpr.evalFunc(getTransaction(),
                                                 a,
                                                 merge_,
                                                 BooleanI.class);

    if (node == null)
      nullOperand("node");
    
    if (privileges == null)
      nullOperand("privileges");

    boolean mergePrivs = (merge != null) ? merge.getValue()
                                         : false;

    Iter i = AbstractMap.defaultPrivileges__.createKeysIterator();
    while (i.hasNext())
    {
      ShortI level;
      Any k = i.next();
      if (privileges.contains(k))
      {
        Any priv = EvalExpr.evalFunc(getTransaction(),
                                     a,
                                     privileges.get(k));

        level = (ShortI)AbstractValue.flyweightConst(new ConstShort(priv));
      }
      else
      {
        ShortI thisLevel = new ConstShort(AbstractMap.defaultPrivileges__.get(k));
        level = thisLevel;
      }

      short processLevel = getTransaction().getProcess().getEffectivePrivilegeLevel();
      if (level.getValue() < processLevel)
        throw new AnyException("Cannot request a privilege level higher than that of the requesting process");
      
      short currentLevel = node.getPrivilegeLevel(k, key);
      if (level.getValue() > currentLevel &&
          currentLevel < processLevel)
        throw new AnyException("Cannot lower a privilege level when current level is higher than that of the requesting process");
    }

    // If we get here then the specified privilege levels are validated OK
    node.setPrivilegeLevels(privileges, key, mergePrivs);
    
		return node;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
		a.add(privileges_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    SetPrivilege r = (SetPrivilege)super.clone();
    
    r.node_       = node_.cloneAny();
    r.privileges_ = privileges_.cloneAny();
    r.key_        = AbstractAny.cloneOrNull(key_);
    
    return r;
  }
}
