/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

import com.inqwell.any.ref.AnyWeakReference;

/**
 * A Map that holds its values as weak references. If not already a weak
 * reference, values are placed inside an AnyWeakReference as they are added to
 * the collection. As such, this class only contains AnyWeakReference instances
 * as its children. The copyFrom method, when acquiring content from another
 * Map, ensures this is so regardless of the source Map type.
 * <p>
 * When keys are tested or values retrieved, the reference is queried. If the
 * referent has been cleared the map entry is removed and treated as if it never
 * existed. It would be possible, then, for <code>contains(key)</code>
 * followed by <code>get(key)</code> to behave inconsistently. This
 * 'optimistic' approach is taken with regard to the expected use of
 * <code>WeakMap</code>: a place to store long-lived application items that
 * will remain in existence until those items are discarded, after which the
 * WeakMap itself will no longer be accessed.
 * 
 * @author tom
 * 
 */
public class WeakMap extends AnyMap
{
  public WeakMap()
  {
    super();
  }

  public void add(Any key, Any value)
  {
    if (beforeAdd(key, value))
    {
      handleDuplicates(key);
      getMap().put(key, wrapWeak(value));
      afterAdd(key, value);
    }
  }

  public Any get(Any key)
  {
    Any a = super.get(key);

    // If we get here then there is a map entry, however it might have
    // been cleared. Check for this and behave as for not-exist if so.
    // This is an optimistic approach, that is we assume the value will
    // be available. If it is not then it would be illegal to query
    // the map for it anyway, as the assumption is that the application
    // has discarded it.
    a = unwrapWeak(key, (AnyWeakReference) a);
    if (a == null)
      handleNotExist(key);

    return a;
  }

  /**
   * Determine if the map contains the given key. The key is considered not
   * present if the reference has been cleared, at which point the map will be
   * tidied.
   * 
   * @return TRUE if a key is present, FALSE otherwise.
   */
  public boolean contains(Any key)
  {
    return getIfContains(key) != null;
  }

  public Any getIfContains(Any key)
  {
    Any a = (Any) super.getIfContains(key);

    if (a != null)
      a = unwrapWeak(key, (AnyWeakReference) a);

    return a;
  }

  public boolean containsValue(Any value)
  {
    AnyWeakReference r = new AnyWeakReference(value);
    return super.containsValue(r);
  }

  public Any copyFrom(Any a)
  {
    // Override to ensure we always contain weak references.
    if (!(a instanceof Map))
      throw new IllegalArgumentException("Cannot copy to a Map from "
          + ((a == null) ? "null" : a.getClass().toString()));

    Map from = (Map) a;

    this.empty();

    Iter i = from.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      Any v = from.get(k);
      if (v instanceof AnyWeakReference)
        this.add(k, v.cloneAny());
      else
        this.add(k, new AnyWeakReference(v.cloneAny()));
    }
    return this;
  }

  public Iter createIterator()
  {
    if (getMap() != null)
      return new WeakIter(Map.I_VALUES);
    else
      return DegenerateIter.i__;
  }

  private Any wrapWeak(Any v)
  {
    if (v instanceof AnyWeakReference)
      return v;

    return new AnyWeakReference(v);
  }

  private Any unwrapWeak(Any key, AnyWeakReference r)
  {
    Any a = r.getAny();

    if (a == null)
      remove(key);

    return a;
  }

  private class WeakIter extends AnyMapIter
  {
    public WeakIter(int mode)
    {
      super(mode);
    }

    public Any next()
    {
      if (iterType_ == I_VALUES)
      {
        // In the values case, override to unwrap the weak reference.
        // Iterating over a WeakMap is not especially well supported: we
        // allow null to be returned in the case of a cleared reference
        // and the map is not cleaned in this case. Again, we are taking
        // an optimistic approach and assuming that the map is discarded
        // at the same time as its content becomes eligible for gc.
        AnyWeakReference r = (AnyWeakReference) i_.next();
        current_ = r.getAny();
        return current_;
      }
      else
        return super.next();
    }
  }
}
