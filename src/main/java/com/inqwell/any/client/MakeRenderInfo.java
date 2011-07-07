/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MakeRenderInfo.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

/**
 * Make an AnyRenderInfo evaluating the argument expressions that it
 * requires. This method of creating AnyRenderInfo objects is used by
 * the parser so that the arguments can be expressions in Inq script.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 */
public class MakeRenderInfo extends    AbstractFunc
                            implements Cloneable
{
	private Any    toRender_;
	private Any    responsible_;
	private String fQName_;
	private String field_;
	private Any    format_;
	private Any    label_;
	private Any    width_;
	private Any    type_;
	private Any    always_;    // actually defunct
	private Any    editable_;
  
  public MakeRenderInfo(Any    toRender,
                        Any    responsible,
                        String fQName,
                        String field,
                        Any    format,
                        Any    label,
                        Any    width,
                        Any    type,
                        Any    always,
                        Any    editable)
	{
		toRender_    = toRender;
		responsible_ = responsible;
		fQName_      = fQName;
		field_       = field;
		format_      = format;
		label_       = label;
		width_       = width;
		type_        = type;
		always_      = always;
		editable_    = editable;
	}
	
	public Any exec(Any a) throws AnyException
	{
    // The parser sees to it that there is always a rendering
    // expression.  For compatibility with the old way of making
    // AnyRenderInfo objects directly in the parser, we don't
    // evaluate this operand, we just clone it (which has already
    // happened in the parent expression). If we used a
    // func holder like other things that take expressions then
    // we would break lots of existing script :-( We can't even
    // check for it explicitly since we can't tell the difference
    // between a NodeRef to a AnyFuncHolder and a AnyFuncHolder
    // itself.
    // Hmmm, well now we do evaluate it and check if the result is
    // a NodeSpecification. If it is then we aggregate to any
    // typedef/field.  See deriveExpr()
		Any toRender = toRender_;
    
    // For this one the parser enforces NodeRef grammar (LocateNode)
    // if not null.
		Locate responsible = (Locate)responsible_;
    
    // The Typedef we might be associated with.  This is expressed
    // as a literal in the syntax (see also field below) and
    // the AnyRenderInfo locates the descriptor
    String fQName = fQName_;

    // The field (of the typedef above) we might be associated with.
    // There is no expression for this as the only supported syntax
    // is the literal [package:]typedef.field
    // As such, the field is already an immutable string and we don't
    // need to evaluate it.
    String field = field_;
    
    toRender = deriveExpr(a, toRender, fQName, field);
                                   
    // The optional format string.  For now (because of
    // RenderInfo.setFormat(String) we will not mandate a string
    // parameter but do toString() later.
    Any format = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   format_);
    
    // The optional label string.  Treated same as format, above
    Any label = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   label_);
    
    // The optional width.  A bit like format - we want an integer
    // but we allow anything that can be converted to a integer
    // number when we do a IntI.copyFrom later
    Any width = EvalExpr.evalFunc(getTransaction(),
                                  a,
                                  width_);
    
    // The (badly named) type argument.  This is a prototypical
    // instance for building the data node that the RenderInfo
    // object can use if it doesn't have any other way to determine
    // the type required (for example it doesn't have a typedef)
    // The syntax is literal and of the form "type=int".
    Any type = type_;
    
    // The optional "always" flag.  Not required any more and maintained
    // for backward compatibility. Used in the past to force
    // re-evaluation of the rendering expression because caching
    // the result was the wrong thing to do.  Now this is detected
    // automatically in the AnyRenderInfo implementation (TBD set
    // result to null in AnyRenderInfo in this case for GC purposes).
    BooleanI always = (BooleanI)EvalExpr.evalFunc(getTransaction(),
                                                  a,
                                                  always_,
                                                  BooleanI.class);

    // Whether this RenderInfo represents editable data    
    BooleanI editable = (BooleanI)EvalExpr.evalFunc(getTransaction(),
                                                    a,
                                                    editable_,
                                                    BooleanI.class);
    
    // Now pick apart what we've got and initialise the renderinfo
    // instance.  There's always a toRender expression
    AnyRenderInfo ari = new AnyRenderInfo(toRender);
    
    // The rest of the arguments are checked in the same order as
    // the old parser code did it.
    
    if (editable != null)
      ari.setEditable(editable.getValue());
    else
      ari.setEditable(false);

    if (format != null)
      ari.setFormat(format.toString());
      
    if (field != null)
      ari.setField(field);

    if (type != null)
      ari.setData(type);

    if (label != null)
      ari.setLabel(label.toString());

    if (always != null)
      ari.setAlwaysEvaluate(always.getValue());

    if (width != null)
    {
      IntI w = new ConstInt(width);
      ari.setWidth(w.getValue());
    }
    
    if (fQName != null)
      ari.setFQName(fQName);
    
    if (responsible != null)
      ari.setResponsibleData(responsible);

    return ari;
	}

  public Object clone () throws CloneNotSupportedException
  {
    MakeRenderInfo m = (MakeRenderInfo)super.clone();
    
    m.toRender_      = AbstractAny.cloneOrNull(toRender_);
    m.responsible_   = AbstractAny.cloneOrNull(responsible_);
    m.format_        = AbstractAny.cloneOrNull(format_);
    m.label_         = AbstractAny.cloneOrNull(label_);
    m.width_         = AbstractAny.cloneOrNull(width_);
    m.type_          = AbstractAny.cloneOrNull(type_);
    m.always_        = AbstractAny.cloneOrNull(always_);
    m.editable_      = AbstractAny.cloneOrNull(editable_);
  
    return m;
  }
  
  protected void setDescriptorFQName(String fQName)
  {
    fQName_ = fQName;
  }
  
  protected void setDescriptorField(String field)
  {
    field_ = field;
  }
  
  private Any deriveExpr(Any    context,
                         Any    toRender,
                         String fQName,
                         String field) throws AnyException
  {
    // If toRender evaluates to a NodeSpecification and fQName/field
    // are provided then generate an expression that concatenates
    // the two.
    
    // The parser ensures that both fQName and field are provided,
    // if at all and/or toRender
    if (fQName == null)
      return checkIndirections(toRender, context);

    if (toRender == null)
      return null;
    
    return checkIndirections(toRender, context);
    /*
    // We've got them both.  Normally, toRender would be the
    // rendering expression itself with the typedef info just
    // providing the label/width/field. Now, implement the
    // possibility that, when evaluated as part of making
    // the renderinfo in the current context, if the expression
    // is a NodeSpecification create the aggregate.

    // We're not interested if it throws.  Apart from the special
    // case we are testing for the expression could very well fail
    // when evaluated 'out of context'.
    
    Any n = null;
    try
    {
      n = EvalExpr.evalFunc(getTransaction(),
                            context,
                            toRender);
    }
    catch(Exception e) {}
    
    // Only do the aggregation magic if the rendering expression is
    // a NodeSpecification.  Otherwise just massage any
    // indirections (in the LocateNode, see checkIndirections !)
    // and leave it as is. (smAdminGUI inspired!).
    if (!(n instanceof NodeSpecification))
      return checkIndirections(toRender, context);
    
    // Yes. Clone and aggregate
    //System.out.println("NS is " + n);
    
		LocateNode ln = new LocateNode(fQName);

		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
																			 Catalog.instance().getCatalog(),
																			 ln,
																			 Descriptor.class);
    Descriptor d = descriptor;
    
    if (d == null)
			throw new AnyException ("Could not resolve Descriptor at " +
															fQName);

    NodeSpecification ns = (NodeSpecification)n.cloneAny();
    
    ns.add(d.getDefaultAlias());
    ns.add(new ConstString(field));
    ln.setNodePath(ns);
    return checkIndirections(ln, context);
    */
  }
  
  private Any checkIndirections(Any toRender, Any context)
  {
    if (toRender instanceof Locate)
    {
      Locate l = (Locate)toRender;
      NodeSpecification n = l.getNodePath();
      n = n.resolveStart(context, getTransaction());
      l.setNodePath(n);
    }
    else if (toRender instanceof com.inqwell.any.MakePath)
    {
      try
      {
        //toRender = toRender.cloneAny();
        NodeSpecification n = (NodeSpecification)EvalExpr.evalFunc
                                          (getTransaction(),
                                           context,
                                           toRender,
                                           NodeSpecification.class);
        toRender = new LocateNode(n);
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    else
    {
      // It's an expression - walk it
      BreadthFirstIter i = new BreadthFirstIter(toRender);
      ResolveNodeSpecs r = new ResolveNodeSpecs(context);
      
      while (i.hasNext())
      {
        Any a = i.next();
        if (a != null)
          a.accept(r);
      }
    }
    return toRender;
  }

  private class ResolveNodeSpecs implements Visitor
  {
    private Any contextNode_;

    ResolveNodeSpecs(Any contextNode)
    {
      contextNode_ = contextNode;
    }

    public void visitMap (Map m)
    {
    }

    public void visitArray (Array a)
    {
    }

    public void visitSet (Set s)
    {
    }

    public void visitUnknown(Any u)
    {
    }

    public void visitAnyBoolean (BooleanI b)
    {
    }

    public void visitAnyByte (ByteI b)
    {
    }

    public void visitAnyChar (CharI c)
    {
    }

    public void visitAnyInt (IntI i)
    {
    }

    public void visitAnyShort (ShortI s)
    {
    }

    public void visitAnyLong (LongI l)
    {
    }

    public void visitAnyFloat (FloatI f)
    {
    }

    public void visitAnyDouble (DoubleI d)
    {
    }

    public void visitDecimal (Decimal d)
    {
    }

    public void visitAnyString (StringI s)
    {
    }

    public void visitAnyDate (DateI d)
    {
    }

    public void visitFunc (Func f)
    {
      // Check if its a LocateNode and if so, fetch its NodeSpecification.
      if (f instanceof LocateNode)
      {
        LocateNode l = (LocateNode)f;
        NodeSpecification ns = l.getNodePath();
        ns = ns.resolveStart(contextNode_, MakeRenderInfo.this.getTransaction());
        l.setNodePath(ns);
      }
    }

    public void visitAnyObject (ObjectI o)
    {
    }

    public void setTransaction(Transaction t)
    {
    }

    public Transaction getTransaction()
    {
      return Transaction.NULL_TRANSACTION;
    }
  }
}
