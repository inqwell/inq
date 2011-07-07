/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/LinkTo.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class LinkTo extends    AbstractFunc
									 implements Cloneable
{
  private NodeSpecification node_;
  private NodeSpecification path_;

	/**
   * @param path the node path we want the target to be linked as.
	 */
  public LinkTo(NodeSpecification node, NodeSpecification path)
  {
    node_       = node;
    path_       = path;
  }

  public Any exec(Any a) throws AnyException
  {
		NodeSpecification node = node_.resolveIndirections(a, getTransaction());
		NodeSpecification path = path_.resolveIndirections(a, getTransaction());

    Any first = path.getFirst();
    if (NodeSpecification.prefices__.contains(first))
      throw new AnyException("LinkTo path must be relative to $this");
    
    if (!node.getFirst().equals(ServerConstants.ROOT))
      throw new AnyException("LinkTo node must be absolute from $root");
    
    // Strip the given node specification to just its path elements.
    NodeSpecification pathItems = new NodeSpecification();
    Iter i = path.createPathItemsIter();
    while (i.hasNext())
      pathItems.add(i.next());
    
    Any ultimate    = pathItems.removeLast();  // name of linked-to child
    Any penultimate = pathItems.removeLast();  // name of LinkMap
    
    // Step down the path we are linking to and build necessary
    // elements
    Map current = (Map)a;
    
    if (!(current instanceof EventGenerator))
      throw new AnyException("node path not an EventGenerator");
      
    for (int idx = 0; idx < pathItems.entries(); idx++)
    {
      Any pathItem = pathItems.get(idx);
      
      if (current.contains(pathItem))
      {
        current = (Map)current.get(pathItem);
      }
      else
      {
        Map next = (Map)current.buildNew(null);
        current.add(pathItem, next);
        current = next;
      }
      if (!(current instanceof EventGenerator))
        throw new AnyException("node path not an EventGenerator");
    }
    
    // We have all the path elements down to 'penultimate'.  If
    // penultimate exists it must be a LinkMap.
    LinkMap lm = null;
    if (current.contains(penultimate))
    {
      if (!(current.get(penultimate) instanceof LinkMap))
        throw new AnyException("Can't link into a non-LinkMap");
        
      lm = (LinkMap)current.get(penultimate);
    }
    else
    {
      EventGenerator root = (EventGenerator)getTransaction().getProcess().getRoot();
      lm = new LinkMap(root);
      current.add(penultimate, lm);
    }
    
    // Next, try to locate what we are linking to.
    LocateNode ln = new LocateNode(node);
    ln.setTransaction(getTransaction());
    Any targetNode = ln.exec(a);
    
    // Finally set up the linkage and raise the event
    lm.addTarget(node, ultimate);
    if (targetNode != null)
    {
      Any eventId = EventConstants.NODE_ADDED;
      if (lm.contains(ultimate))
        eventId = EventConstants.NODE_REPLACED;
        
      lm.replaceItem(ultimate, targetNode);
    
      Event e = lm.makeEvent(eventId);
      e.setContext(targetNode);
      e.setParameter(ultimate);
      lm.fireEvent(e);
    }
    
	  return targetNode;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
  	a.add(path_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    LinkTo a = (LinkTo)super.clone();

    a.node_   = (NodeSpecification)node_.cloneAny();
    a.path_   = (NodeSpecification)path_.cloneAny();

    return a;
  }
}
