/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Composite is the interface for the structure and array
 * data classes of RTF.  Essentially a place-holder in the hierarchy
 * marking the introduction of complex Anys.
 */
public interface Composite extends Any
{
  /**
   * Returns the number of children the composite holds.
   */
  public int entries();

  public boolean equals (Any a);
  
  /**
   * Checks for the existence of the given any in this composite.
   * @return true if this contains the any; false otherwise
   */
  public boolean contains (Any a);

  /**
   * Add the given element to this composite.  Optional operation
   */
  public void add (Any element);

	/**
	 * Checks if <i>all</i> the contents of the given composite are
	 * contained within this
	 */
  public boolean containsAll (Composite c);
  
	/**
	 * Checks if <i>any</i> of the contents of the given composite are
	 * contained within this
	 */
  public boolean containsAny (Composite c);
  
  /**
   * Remove everything contained in <code>this</code> that is
   * also contained in <code>c</code>.
   */
  public void removeAll(Composite c);

  /**
   * Retain everything contained in <code>this</code> that is
   * also contained in <code>c</code>. Everything else that
   * is contained in <code>this</code> is removed.
   */
  public void retainAll(Composite c);
  
  /**
   * Remove all entries from the composite
   */
  public void empty();
  
  public boolean isEmpty();
  
  /**
	 * Returns a value specific to the object instance.  Not guaranteed
	 * to be unique, but is not affected by the instance's content, i.e.
	 * calling <code>identity()</code> always returns the same value for
	 * a given object.
	 */
  public int identity();
  
  /**
	 * Determine whether a Composite instance is a managed object.  Being managed
	 * means that <code>hashCode()</code> and <code>equals()</code> operate on
	 * the object instance itself rather than the instance's members.  This
	 * means that the instance can be tracked where it is important to ensure
	 * that multiple threads are dealing with physically the same object.
	 * <p>
	 * Most <code>Composite</code> implementations will return <code>false</code>
	 * with identity semantics being bestowed by suitable decorator.  Note that
	 * 
	 * @see com.inqwell.any.identity.HasIdentity
	 */
  public boolean hasIdentity();

  /**
	 * Returns true if this Map represents a homogeneous set of
	 * objects.
	 */
  public Any getNodeSet();
  
  /**
	 * Establish set status of this map
	 */
  public void setNodeSet(Any nodeSet);

	/**
	 * Return the parent of the current composite.  Optional operation since
	 * some implementations permit a node to be referenced by more than one
	 * parent
	 */  
  public Composite getParentAny();

  /**
   * Return the name by which this instance is known in its parent
   * container. Optional operation.
   * @return Any name in parent or null if we have no parent or this
   * implementation does not support the operation.
   */
  public Any getNameInParent();
  
  /**
   * Return the full path of this node to its reachable root.
   * Optional operation.
   * @return path to this node from root or null if we have
   * no parent or this implementation does not support the
   * operation.
   */
  public Any getPath();
  
	/**
	 * Check for whether parentage is allowed by this implementation
	 */
  public boolean isParentable();

  /**
	 * May be implemented to allow structures to establish the parent
	 * relationship
	 */
  public void setParent(Composite parent);
  
  /**
   * An upwards removal operation.  Optional operation
   */
  public void removeInParent();
  
  /**
   * Generic composite remove operation.  Returns original member
   */
  public Any remove(Any id);

  /**
   * Return the <code>Process</code> with which this instance is associated.
   * Optional operation.
   * @return The <code>Process</code> or <code>null</code> if no process
   * is associated or the implementation is degenerate.
   */
  public Process getProcess();
  
  /*
   * Mark an object for deletion at some time when thread
   * policy makes it possible to do so.
   */
  public void markForDelete(Any id);

  /*
   * Check if an object has been marked for deletion.
   */
  public boolean isDeleteMarked(Any id);
  
  public Composite shallowCopyOf();
}
