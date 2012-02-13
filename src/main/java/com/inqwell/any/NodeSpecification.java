/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NodeSpecification.java $
 * $Author: sanderst $
 * $Revision: 1.14 $
 * $Date: 2011-05-06 22:27:18 $
 */

package com.inqwell.any;


/**
 * A array whose elements specify a path through an arbitrarily
 * nested composite to a target node within that composite.
 * A NodeSpecification may be applied to a composite to return a target node
 * by using the LocateNode class.  Although the semantics of node location
 * are defined by that class they are also documented here:
 * <p>
 * A <code>NodeSpecification</code> uses a string delimited with the
 * characters <code>"."</code> and <code>"*"</code>.
 * @see com.inqwell.any.LocateNode
 * @author $Author: sanderst $
 * @version $Revision: 1.14 $
 */
public class NodeSpecification extends AnyQueue
{
	private static final long serialVersionUID = 1L;

	public final static NodeSpecification NULLNS = new NodeSpecification(new ConstString());

	// The content of separators__ are the characters which delimit
	// components of a locate path.  They control the strict-ness of
	// LocateNode: "." == strict; "*" == not strict.  This can change
	// during the navigation of the path.
  public  final static String separators__  = ".*[]{}()|,";
  public  final static String root__        = "$root";
  public  final static String parent__      = "$parent";
  public  final static String catalog__     = "$catalog";
  public  final static String stack__       = "$stack";
  public  final static String context__     = "$context";
  public  final static String process__     = "$process";
  public  final static String uidefaults__  = "$uidefaults";
  public  final static String contextPath__ = "$path";
  public  final static String null__        = "$null";
  public  final static String param__       = "$param";
  public  final static String properties__  = "$properties";
  public  final static String loop__        = "$loop";
  public  final static String this__        = "$this";  // parser only
  public  final static String first__       = "first";
  public  final static String last__        = "last";

  private final static Any    athis__       = AbstractValue.flyweightString(this__);
  private final static Any    astack__      = AbstractValue.flyweightString(stack__);

  public  final static Any strict__            = AbstractValue.flyweightString(".");
  public  final static Any lazy__              = AbstractValue.flyweightString("*");
  public  final static Any veryLazy__          = AbstractValue.flyweightString("**");
  public  final static Any group__             = AbstractValue.flyweightString(",");
  public  final static Any indexOpen__         = AbstractValue.flyweightString("[");
  public  final static Any indexClose__        = AbstractValue.flyweightString("]");
  public  final static Any indexFirst__        = AbstractValue.flyweightString(first__);
  public  final static Any indexLast__         = AbstractValue.flyweightString(last__);
  public  final static Any indirectKeyOpen__   = AbstractValue.flyweightString("{");
  public  final static Any indirectKeyClose__  = AbstractValue.flyweightString("}");
  public  final static Any indirectNodeOpen__  = AbstractValue.flyweightString("(");
  public  final static Any indirectNodeClose__ = AbstractValue.flyweightString(")");
  public  final static Any groupFieldOpen__    = AbstractValue.flyweightString("|");
  public  final static Any groupFieldClose__   = AbstractValue.flyweightString("|");
  public  final static Any equals__            = AbstractValue.flyweightString("#");
  public  final static Any thisEquals__        = AbstractValue.flyweightString("%");
	// not really key tokens
  public  final static Any stackFrame__  = AbstractValue.flyweightString("stack__");

	// some key fields for (amongst other things) complex values
	// to build and set into components
	public final static Any atWho__ = AbstractValue.flyweightString("@who");
	public final static Any who__   = AbstractValue.flyweightString("who__");

	public final static Any atTxt__ = AbstractValue.flyweightString("@txt");
	public final static Any txt__   = AbstractValue.flyweightString("txt__");

	public final static Any atIco__ = AbstractValue.flyweightString("@ico");
	public final static Any ico__   = AbstractValue.flyweightString("ico__");

	public final static Any atIcoLeaf__ = AbstractValue.flyweightString("@icoLeaf");
	public final static Any icoLeaf__   = AbstractValue.flyweightString("icoLeaf__");

	public final static Any atIcoOpen__ = AbstractValue.flyweightString("@icoOpen");
	public final static Any icoOpen__   = AbstractValue.flyweightString("icoOpen__");

	public final static Any atIcoClosed__ = AbstractValue.flyweightString("@icoClosed");
	public final static Any icoClosed__   = AbstractValue.flyweightString("icoClosed__");

	public final static Any atBg__ = AbstractValue.flyweightString("@bg");
	public final static Any bg__   = AbstractValue.flyweightString("bg__");

	public final static Any atFg__ = AbstractValue.flyweightString("@fg");
	public final static Any fg__   = AbstractValue.flyweightString("fg__");

  public final static Any atBold__ = AbstractValue.flyweightString("@bold");
  public final static Any bold__   = AbstractValue.flyweightString("bold__");

  public final static Any atFontFamily__ = AbstractValue.flyweightString("@fontFamily");
  public final static Any fontFamily__   = AbstractValue.flyweightString("fontFamily__");

  public final static Any atFormat__   = AbstractValue.flyweightString("@format");

  public final static Any atFontSize__ = AbstractValue.flyweightString("@fontSize");
  public final static Any fontSize__   = AbstractValue.flyweightString("fontSize__");

  public final static Any atItalic__ = AbstractValue.flyweightString("@italic");
  public final static Any italic__   = AbstractValue.flyweightString("italic__");

  public final static Any atStrikeThrough__ = AbstractValue.flyweightString("@strikeThrough");
  public final static Any strikeThrough__   = AbstractValue.flyweightString("strikeThrough__");

  public final static Any atUnderline__ = AbstractValue.flyweightString("@underline");
  public final static Any underline__   = AbstractValue.flyweightString("underline__");

  public final static Any atFirstIndent__ = AbstractValue.flyweightString("@firstIndent");
  public final static Any firstIndent__   = AbstractValue.flyweightString("firstIndent__");

  public final static Any atLeftIndent__ = AbstractValue.flyweightString("@leftIndent");
  public final static Any leftIndent__   = AbstractValue.flyweightString("leftIndent__");

  public final static Any atRightIndent__ = AbstractValue.flyweightString("@rightIndent");
  public final static Any rightIndent__   = AbstractValue.flyweightString("rightIndent__");

  public final static Any atAlignment__ = AbstractValue.flyweightString("@alignment");
  public final static Any alignment__   = AbstractValue.flyweightString("alignment__");

  public final static Any atLineSpacing__ = AbstractValue.flyweightString("@lineSpacing");
  public final static Any lineSpacing__   = AbstractValue.flyweightString("lineSpacing__");

  public final static Any atSpaceAbove__ = AbstractValue.flyweightString("@spaceAbove");
  public final static Any spaceAbove__   = AbstractValue.flyweightString("spaceAbove__");

  public final static Any atSpaceBelow__ = AbstractValue.flyweightString("@spaceBelow");
  public final static Any spaceBelow__   = AbstractValue.flyweightString("spaceBelow__");

  public final static Any atSubscript__ = AbstractValue.flyweightString("@subscript");
  public final static Any subscript__   = AbstractValue.flyweightString("subscript__");

  public final static Any atSuperscript__ = AbstractValue.flyweightString("@superscript");
  public final static Any superscript__   = AbstractValue.flyweightString("superscript__");

	public final static Any atTabStops__   = AbstractValue.flyweightString("@tabStops");
	public final static Any tabStops__     = AbstractValue.flyweightString("tabSops__");

	public final static Any atTabPos__   = AbstractValue.flyweightString("@tabPos");
	public final static Any tabPos__     = AbstractValue.flyweightString("tabPos__");

	public final static Any atTabAlign__   = AbstractValue.flyweightString("@tabAlign");
	public final static Any tabAlign__     = AbstractValue.flyweightString("tabAlign__");

	public final static Any atTabLead__   = AbstractValue.flyweightString("@tabLead");
	public final static Any tabLead__     = AbstractValue.flyweightString("tabLead__");

	public final static Any atUrlRef__   = AbstractValue.flyweightString("@urlRef");
	public final static Any urlRef__     = AbstractValue.flyweightString("urlRef__");

	public final static Any atUrlAction__   = AbstractValue.flyweightString("@urlAction");
	public final static Any urlAction__     = AbstractValue.flyweightString("uA__");

	public final static Any atUser__   = AbstractValue.flyweightString("@user");
	public final static Any user__     = AbstractValue.flyweightString("user__");

	public final static Any atStyle__   = AbstractValue.flyweightString("@style");
	public final static Any style__     = AbstractValue.flyweightString("style__");

	public final static Any atName__    = AbstractValue.flyweightString("@name");
	public final static Any name__      = AbstractValue.flyweightString("name__");

	public final static Any atBorderWidth__ = AbstractValue.flyweightString("@borderWidth");
	public final static Any borderWidth__   = AbstractValue.flyweightString("borderWidth__");

	public final static Any atCellPadding__ = AbstractValue.flyweightString("@cellPadding");
	public final static Any cellPadding__   = AbstractValue.flyweightString("cellPadding__");

	public final static Any atColumnSpan__ = AbstractValue.flyweightString("@columnSpan");
	public final static Any columnSpan__   = AbstractValue.flyweightString("columnSpan__");

	public final static Any atRowSpan__ = AbstractValue.flyweightString("@rowSpan");
	public final static Any rowSpan__   = AbstractValue.flyweightString("rowSpan__");

  // See UserProcess.java
	public final static Any atPasswd__   = AbstractValue.flyweightString("@passwd");
	public final static Any atPkg__      = AbstractValue.flyweightString("@package");
	public final static Any atURL__      = AbstractValue.flyweightString("@url");
	public final static Any atServer__   = AbstractValue.flyweightString("@server");

	public final static Any atDomain__ = AbstractValue.flyweightString("@domain");
	public final static Any domain__   = AbstractValue.flyweightString("domain__");

  public final static Any atException__   = AbstractValue.flyweightString("@exception");
  public final static Any atExName__      = AbstractValue.flyweightString("@exname");
	public final static Any atExMsg__       = AbstractValue.flyweightString("@exmsg");
  public final static Any atExInfo__      = AbstractValue.flyweightString("@exinfo");
  public final static Any atStackTrace__  = AbstractValue.flyweightString("@stackTrace");
  public final static Any atJavaStack__   = AbstractValue.flyweightString("@javaStack");
  public final static Any atExCommit__    = AbstractValue.flyweightString("@excommit");

  public final static Any atChannel__    = AbstractValue.flyweightString("@channel");

  // See EventConstants.java
	//public static Any atEvent__     = AbstractValue.flyweightString("@event");
	//public static Any atEventData__ = AbstractValue.flyweightString("@eventData");
	//public static Any atEventId__   = AbstractValue.flyweightString("@eventId");

	public final static Any atIterCount__   = AbstractValue.flyweightString("@count");
	public final static Any atIterName__    = AbstractValue.flyweightString("@name");
	public final static Any atIterFirst__   = AbstractValue.flyweightString("@first");
  public final static Any atIterLast__    = AbstractValue.flyweightString("@last");
  public final static Any atOld__         = AbstractValue.flyweightString("@old");
  public final static Any atCapped__      = AbstractValue.flyweightString("@capped");
  public final static Any atModel__       = AbstractValue.flyweightString("@model");

	public final static Any atCellAlign__ = AbstractValue.flyweightString("@cellAlign");
  public final static Any atCellWidth__ = AbstractValue.flyweightString("@cellWidth");
  public final static Any atCellFill__  = AbstractValue.flyweightString("@cellFill");
  public final static Any atToolTip__   = AbstractValue.flyweightString("@toolTip");

  public final static Any atMax__               = AbstractValue.flyweightString("@max");
  public final static Any atSyncGUI__           = AbstractValue.flyweightString("@syncgui");

  private final static Map fixedTokens__;
	public  final static Set prefices__;
  public  final static Set specialTokens_;

	// Retain the original specification if there is one
	//private StringI pathSpec_;

	// only used by equals()
	private Any       lastControl_     = strict__;

	private boolean   mustShallowCopy_ = false;

	static
	{
		fixedTokens__ = AbstractComposite.simpleMap();
		fixedTokens__.add(atWho__,  who__);
		//fixedTokens__.add(atTxt__,  txt__);
		fixedTokens__.add(atIco__,  ico__);
		fixedTokens__.add(atIcoLeaf__,  icoLeaf__);
		fixedTokens__.add(atIcoOpen__,  icoOpen__);
		fixedTokens__.add(atIcoClosed__,  icoClosed__);
		fixedTokens__.add(atBg__,   bg__);
		fixedTokens__.add(atFg__,   fg__);
		fixedTokens__.add(atBold__,   bold__);
		fixedTokens__.add(atFontFamily__,   fontFamily__);
		fixedTokens__.add(atFontSize__,   fontSize__);
		fixedTokens__.add(atItalic__,   italic__);
		fixedTokens__.add(atStrikeThrough__,   strikeThrough__);
		fixedTokens__.add(atUnderline__,   underline__);
		fixedTokens__.add(atFirstIndent__,   firstIndent__);
		fixedTokens__.add(atLeftIndent__,   leftIndent__);
		fixedTokens__.add(atRightIndent__,   rightIndent__);
		fixedTokens__.add(atAlignment__,   alignment__);
		fixedTokens__.add(atLineSpacing__,   lineSpacing__);
		fixedTokens__.add(atSpaceAbove__,   spaceAbove__);
		fixedTokens__.add(atSpaceBelow__,   spaceBelow__);
		fixedTokens__.add(atSubscript__,   subscript__);
		fixedTokens__.add(atSuperscript__,   superscript__);
		fixedTokens__.add(atStyle__, style__);
		fixedTokens__.add(atName__, name__);
		fixedTokens__.add(atBorderWidth__, borderWidth__);
		fixedTokens__.add(atCellPadding__, cellPadding__);
		fixedTokens__.add(atRowSpan__, rowSpan__);
		fixedTokens__.add(atColumnSpan__, columnSpan__);
		fixedTokens__.add(atTabStops__,   tabStops__);
		fixedTokens__.add(atTabPos__,   tabPos__);
		fixedTokens__.add(atTabAlign__,   tabAlign__);
		fixedTokens__.add(atTabLead__,   tabLead__);
		fixedTokens__.add(atUrlRef__,   urlRef__);
		fixedTokens__.add(atUrlAction__,   urlAction__);
		fixedTokens__.add(atUser__, user__);
		fixedTokens__.add(atPasswd__, UserProcess.passwd__);
		fixedTokens__.add(atPkg__, UserProcess.package__);
		fixedTokens__.add(atURL__, UserProcess.url__);
		fixedTokens__.add(atServer__, UserProcess.host__);

		fixedTokens__.add(atDomain__,    domain__);
		fixedTokens__.add(equals__, AnyAlwaysEquals.instance());
		//fixedTokens__.add(atEvent__, EventConstants.EVENT);
		//fixedTokens__.add(atEventData__, EventConstants.EVENT_CONTEXT);
		//fixedTokens__.add(atEventId__, EventConstants.EVENT_ID);

		prefices__ = AbstractComposite.set();
		prefices__.add(AbstractValue.flyweightString(root__));
		prefices__.add(AbstractValue.flyweightString(parent__));
		prefices__.add(AbstractValue.flyweightString(catalog__));
		prefices__.add(AbstractValue.flyweightString(uidefaults__));
		prefices__.add(AbstractValue.flyweightString(stack__));
		prefices__.add(AbstractValue.flyweightString(process__));
		prefices__.add(AbstractValue.flyweightString(contextPath__));
		prefices__.add(AbstractValue.flyweightString(null__));
		prefices__.add(AbstractValue.flyweightString(context__));
		prefices__.add(AbstractValue.flyweightString(param__));
    prefices__.add(AbstractValue.flyweightString(properties__));
    prefices__.add(AbstractValue.flyweightString(loop__));
    
    specialTokens_ = AbstractComposite.set();
    specialTokens_.add(thisEquals__);
    specialTokens_.add(equals__);
    specialTokens_.add(strict__);
    specialTokens_.add(lazy__);
	}

  public static NodeSpecification setPrefices(NodeSpecification n)
  {
    if (n.entries() == 0)
      return n;

    Any first = n.getFirst();

    // explicit prefix (not including $this) leave as it is
    if (prefices__.contains(first))
      return n;

    // prefix is $this - remove.  Examine next token and if it is the
    // strict control token then remove that as well.
    if (first.equals(athis__))
    {
      n.removeFirst();

      int entries = n.entries();

      if (entries < 2)
        n.addFirst(strict__);
      else if (n.getFirst().equals(strict__))
        n.removeFirst();

      return n;
    }

    // Either a control token or path element.  Add $stack prefix
    n.addFirst(strict__);
    n.addFirst(astack__);

    return n;
  }

	public static boolean isStrict(Any a)
	{
		return a.equals(strict__);
	}

	public static boolean isLazy(Any a)
	{
		return a.equals(lazy__) || a.equals(veryLazy__);
	}

  public static boolean isVeryLazy(Any a)
  {
    return a.equals(veryLazy__);
  }
  
	public static boolean isControl(Any a)
	{
    if (AnyAlwaysEquals.isAlwaysEquals(a))
      return false;
		return (a.equals(lazy__) || a.equals(strict__) || a.equals(veryLazy__));
	}
  
  public static boolean isSpecial(Any a)
  {
    return specialTokens_.contains(a);
  }

	public static Composite processRoot(Composite c)
	{
		// try to navigate up to the root.  May fail as parent
		// retrieval is an optional operation
		Composite root = c;
		Composite curr = c;
		while (curr != null)
		{
			root = curr;
			curr = curr.getParentAny();
		}
		return root;
	}

	public static Composite processParent(Composite c)
	{
		Composite parent = c.getParentAny();
		if(parent == null)
		  parent = c;
		return c;
	}

	public static Composite processCatalog()
	{
		Composite c = Catalog.instance().getCatalog();
		return c;
	}

  public static Composite processStack(Transaction t)
  {
    Composite c = t.getCurrentStackFrame();
    return c;
  }

  public static Composite processLoop(Transaction t)
  {
    Composite c = (Composite)t.getLoop();
    if (c == null)
      throw new AnyRuntimeException("No current $loop");
    return c;
  }

	public static Any processProcess(Transaction t)
	{
		Any a = t.getProcess();
		return a;
	}

	public static Map processContext(Transaction t)
	{
		Map m = t.getContext();
		return m;
	}

  /**
   * Generate a node specification out of the given path of the form "a.b.c"
   */
  public NodeSpecification(String s)
  {
		//pathSpec_ = new ConstString(s);
    //System.out.println("NodeSpecification " + s);
    AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
    parsePath (this, t, null);
  }

  /**
   * Generate a node specification out of the given path of the form "a.b.c"
   */
  public NodeSpecification(StringI s)
  {
		//pathSpec_ = s;
    AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
    parsePath (this, t, null);
  }

  /**
   * Generate a node specification from an Array of paths.  This is a bit weak (is it
   * still needed?) as the array of strings alone has no separators and thus no control
   * of strict-ness of location the separartors provide.  Assume strict throughout.
  public NodeSpecification(Array a)
  {
    Iter i = a.createIterator();
    while (i.hasNext())
    {
      Any n = i.next().cloneAny();
      addToken(n);
    }
  }
   */

  public NodeSpecification()
	{
		//pathSpec_ = new ConstString();
	}

/*
	public StringI getPathSpec ()
	{
		return pathSpec_;
	}
*/

	public NodeSpecification setPathSpec(String s)
	{
		AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
		//pathSpec_ = new ConstString(s);
		this.empty();
    parsePath (this, t, null);
    return this;
	}

	public boolean mustShallowCopy()
	{
		return mustShallowCopy_;
  }

  /**
   * Check if <code>this</code> contains any <code>{}</code> style
   * references and if so resolve them with respect to the given
   * context and transaction.  If there are no indirections
   * then <code>this</code> is returned.  Otherwise returns a new
   * <code>NodeSpecification</code> leaving <code>this</code>
   * unchanged.
   */
  public NodeSpecification resolveIndirections(Any context, Transaction t)
  {
    NodeSpecification ret = this;

    if (mustShallowCopy())
    {
      ret = new NodeSpecification();
      for (int i = 0; i < entries(); i++)
      {
        Any item = get(i);
        if (item instanceof LocateNode)
        {
          LocateNode l = (LocateNode)item.cloneAny();
          l.setTransaction(t);
          Any resolved = null;
          try
          {
            resolved = l.exec(context);
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(e);
          }

          if (resolved == null)
            continue;

          if (resolved instanceof StringI)
          {
            resolved = new NodeSpecification(resolved.toString());
          }
          if (!(resolved instanceof NodeSpecification))
          {
            ret.addToken(resolved);
          }
          else
          {
            NodeSpecification n = (NodeSpecification)resolved;
            for (int j = 0; j < n.entries(); j++)
              ret.addToken(n.get(j));
          }
        }
        else
        {
          ret.addToken(item);
        }
      }
    }
    return ret;
  }

  public NodeSpecification resolveStart(Any context, Transaction t)
  {
    NodeSpecification ret = this;

    if (mustShallowCopy())
    {
      ret = new NodeSpecification();
      ResolveIndirections r = new ResolveIndirections(ret, context, t);
      resolve(r, this, false);
    }

    return ret;
  }

  static private void resolve(ResolveIndirections r,
                              NodeSpecification   n,
                              boolean nested)
  {
    for (int i = 0; i < n.entries(); i++)
    {
      Any item = n.get(i);
      if (nested && i == 0)
      {
        // If its a nested NodeSpecification then ignore any $stack
        // prefix. Any other kind ($this is implicit, so OK) causes
        // an error.
        if (prefices__.contains(item) && !item.equals(astack__))
          throw new AnyRuntimeException("Illegal nested path " + n);

        if (item.equals(astack__))
          continue;
      }
      //System.out.println(nested + " accepting on " + item);
      item.accept(r);
    }
  }

	public Object clone() throws CloneNotSupportedException
	{
    // Strictly speaking we only need a shallow copy of the
    // underlying queue but most instances are contained
    // within a LocateNode and that doesn't clone its
    // NodeSpecification. Otherwise
		NodeSpecification n = (NodeSpecification)super.clone();
    // pathSpec_ is read-only
		//n.pathSpec_         = (StringI)pathSpec_.cloneAny();
		n.lastControl_    = strict__;
		return n;
	}

	public String toString()
	{
//    if (pathSpec_ != null)
//      return pathSpec_.toString();

    StringBuffer s = new StringBuffer(32);
    for (int i = 0; i < entries(); i++)
    {
      Any item = get(i);
      if (i == 0 && !prefices__.contains(item))
      {
        s.append("$this");
        if (!isControl(item));
        {
          s.append(".");
        }
      }
      s.append(item.toString());
    }
    
    if (s.length() == 0)
      s.append("$this");

		return s.toString();
    /*
    Any first = this.get(0);
    if (!prefices__.contains(first))
      return ("$this." + pathSpec_.toString());
    else
      return first.toString() + pathSpec_.toString();
      */
	}

	/**
	 * Handle special equality semantics for <code>NodeSpecification</code>s.
	 * Two <code>NodeSpecification</code>s are considered equal if they
	 * could locate the same node when applied to a structure.
	 * If the specifications being compared are always <code>strict</code>,
	 * that is they are of the form <code>x.y.z</code>, then they must
	 * be identical to be equal.  If, however, one or other contains
	 * the lazy control token <code>"*"</code> then they must contain
	 * the same trailing components.
	 * <p>
	 * For example, the paths <code>*Product</code> would be deemed
	 * equal to <code>x.y.Product</code>; <code>x.y.Product</code>
	 * is also equal to <code>x*Product</code> but <code>*x.Product</code>
	 * is not equal to <code>*y.Product</code>
	 * <p>
	 *
	 */
	public boolean equals(Any a)
	{
		if (a == this)
			return true;

		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

		if (!(a instanceof NodeSpecification))
			return false;

		boolean ret = true;
		NodeSpecification ns = (NodeSpecification)a;
		int nsi1 = 0;
		int nsi2 = 0;
		Any t1 = null;
		Any t2 = null;
    lastControl_    = strict__;
    ns.lastControl_ = strict__;

    // Fetch initial non-control token indices
		nsi1 = this.nextToken(nsi1, null);
		nsi2 = ns.nextToken(nsi2, null);

		//System.out.println ("NodeSpecification.equals() starting.....");
		while ((nsi1 < this.entries()) &&
					 (nsi2 < ns.entries()))
		{
      // Get current non-control tokens
			t1 = this.get(nsi1++);
			t2 = ns.get(nsi2++);

      // Check for equality of current tokens or whether
      // either is the single component wild-card
			if (t1.equals(t2) ||
			    t1.equals(thisEquals__) ||
			    t2.equals(thisEquals__))
			{
        // The tokens are equal but if one of the node specs is "very lazy"
        // find the last occurrance of the current token (which could be
        // the current occurrance) in the other one.
        if (isVeryLazy(ns.lastControl_) && !isVeryLazy(lastControl_))
        {
				  nsi1 += this.nextToken(nsi1, t1);
          
        }
        else if (!isVeryLazy(ns.lastControl_) && isVeryLazy(lastControl_))
        {
				  nsi2 += ns.nextToken(nsi2, t2);
        }
        
				ret = true;

        nsi1 += this.nextToken(nsi1, null);
        nsi2 += ns.nextToken(nsi2, null);

        if (t1 == AnyAlwaysEquals.instance() ||
				    t2 == AnyAlwaysEquals.instance())
				{
					// If either specification contains AnyAlwaysEquals (the '#'
					// character) then we consider them to be equal.  Set the
					// termination condition
					nsi1 = this.entries();
					nsi2 = ns.entries();
				}
			}
			else
			{
				ret = false;
				if (isLazy(this.lastControl_) || isLazy(ns.lastControl_))
				{
					// consume tokens from the strict specification
					// until match or end
					if (isStrict(this.lastControl_))
					{
            // Forward searches are done here, reverse searches are done
            // by nextToken...
            if (isVeryLazy(ns.lastControl_))
            {
              nsi1 += this.nextToken(nsi1, t2);
              ret = nsi1++ < this.entries();
            }
            else
            {
  						nsi1 += this.nextToken(nsi1, null);
  						while (!(ret = t1.equals(t2)) && nsi1 < this.entries())
  						{
  							t1 = this.get(nsi1++);
  							nsi1 += this.nextToken(nsi1, null);
  							//System.out.println ("t1 is " + t1);
  							//System.out.println ("t2 is " + t2);
  						}
            }
						//System.out.println ("dropped out with t1,t2 " + t1);
					}
					else
					{
            if (isVeryLazy(lastControl_))
            {
              nsi2 += ns.nextToken(nsi2, t1);
              ret = nsi2++ < ns.entries();
            }
            else
            {
  						nsi2 += ns.nextToken(nsi2, null);
  						while (!(ret = t1.equals(t2)) && nsi2 < ns.entries())
  						{
  							t2 = ns.get(nsi2++);
  							nsi2 += ns.nextToken(nsi2, null);
  							//System.out.println ("t2 is " + t2);
  							//System.out.println ("t1 is " + t1);
  						}
            }
						//System.out.println ("dropped out with t2,t1 " + t1);
					}
					nsi1 += this.nextToken(nsi1, null);
					nsi2 += ns.nextToken(nsi2, null);
				}
				else
				{
					// tokens not equal and both specs are currently strict
					// Fail.
					break;
				}
			}
		}

		if (nsi1 < this.entries() || nsi2 < ns.entries())
		{
			// If either specification is not exhausted then they can't
			// be equal *unless* there is a trailing alwaysequals on one
      // of them.
      if (nsi2 < ns.entries() && ns.get(nsi2) == AnyAlwaysEquals.instance())
        ret = true;
      else if(nsi1 < this.entries() && this.get(nsi1) == AnyAlwaysEquals.instance())
        ret = true;
      else
			  ret = false;
		}
		return ret;
	}

	public int hashCode()
	{
		return 2;
	}
  
	public Iter createPathItemsIter()
	{
		return new PathItemsIter();
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
    	if (a instanceof NodeSpecification)
    	{
    		NodeSpecification n = (NodeSpecification)a;
        this.empty();
        this.addAll(n);
    		//pathSpec_ = (StringI)n.pathSpec_.cloneAny();
  	    //AnyStringTokenizer t = new AnyStringTokenizer(pathSpec_, separators__, true);
  	    //parsePath (this, t, null);
    	}
    	else if (a instanceof StringI)
    	{
        //StringI s = (StringI)a.cloneAny();
        StringI s = (StringI)a;
    		//pathSpec_ = s;
  	    AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
  	    this.empty();
  	    parsePath (this, t, null);
    	}
    	else
    	  throw new AnyRuntimeException("Not supported from " + a.getClass());
    }

    return this;
  }

  /**
   * Appends to this node specification, tokenising a string if necessary.
   */
  public void add (Any element)
  {
    if (element instanceof StringI)
    {
      StringI s = (StringI)element;
      AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
      if (t.hasMoreTokens())
      {
        String str = t.nextToken();
        if (!isControl(AbstractValue.flyweightString(str)) &&
            this.entries() > 0 &&
            !isControl(this.getLast()))
          this.addToken(strict__);
        parsePath (this, t, str);
      }
    }
    else
      addToken(element);
  }
  
  public void addLiteral(Any element)
  {
    addToken(element);
  }
  
	// Find the next non-control token at or after the given
	// start point.  Sets the lastControl_ member as a
	// side-effect for use by equals(). If seekToken is
  // not null then the last occurrance of seekToken must
  // be found or the node spec will be exhausted.
  // Returns the number of tokens overall consumed.
	private int nextToken(int start, Any seekToken)
	{
		if (start >= entries())
			return 0;

		int pos   = start;
		Any token = null;
    if (seekToken == null)
    {
  		while (pos < entries() &&
             NodeSpecification.isControl((token = get(pos))))
  		{
  			lastControl_ = token;
  			pos++;
  		}
    }
    else
    {
      // Start from the back looking for seekToken until 'start'
      pos = entries();
      while (--pos >= start && !(token = get(pos)).equals(seekToken));
      if (pos < start)
      {
        // Didn't find seekToken at all. Consume all tokens.
        pos = entries();
      }
      else
      {
        // Found it at pos. Take the trouble to find the previous control token
        // just to keep lastControl_ correct. Advance pos as we have consumed
        // this token.
        int ctrl = pos;
        while (--ctrl >= 0  &&
               !NodeSpecification.isControl((token = get(ctrl))));
        
        if (ctrl >= 0)
          lastControl_ = token;
      }
    }
		return pos - start;
	}
	/**
	 * Interprets the given string as more node specification and appends to
	 * current.  No delimiter is added to the internal path specification
	 * although strict is assumed in the string version.
	 */
	 /* Temporary removed!!
	public void add(String s)
	{
		AnyStringTokenizer t = new AnyStringTokenizer(s, separators__, true);
		parsePath(t);
		pathSpec_.setValue(pathSpec_.getValue().concat(".").concat(s));
  }
  */

  /**
   * Extract each token from given tokenizer and generate the node specification
   * structure
   */
  static private void parsePath (NodeSpecification  ns,
                                 AnyStringTokenizer t,
                                 String             first)
  {
    if (first != null)
    {
      StringI f = new ConstString(first);
      if (fixedTokens__.contains(f))
      {
        ns.addToken(fixedTokens__.get(f));
      }
      else
        ns.addToken(AbstractValue.flyweightString(first));
    }

    while (t.hasMoreTokens())
    {
      String ss = t.nextToken();
      StringI s = new ConstString (ss);
      if (fixedTokens__.contains(s))
      {
        ns.addToken(fixedTokens__.get(s));
      }
      else if (s.equals(indexOpen__))
      {
        processIndex(ns, t);
      }
      else if (s.equals(indirectKeyOpen__))
      {
        processIndirectKey(ns, t);
        ns.mustShallowCopy_ = true;
      }
      else if (s.equals(indirectNodeOpen__))
      {
        processIndirectNode(ns, t);
      }
      else if (s.equals(indexClose__))
      {
        if (first != null)
          return;
        else
          continue;
      }
      else if (s.equals(indirectKeyClose__))  // should only happen at outermost level
      {
        continue;
      }
      else if (s.equals(indirectNodeClose__))  // should only happen at outermost level
      {
        continue;
      }
      else if (s.equals(strict__))
      {
        ns.addToken(strict__);
      }
      else if (s.equals(lazy__))
      {
        // Check if the last token was lazy and if so replace it
        // with very lazy
        if (ns.getLast().equals(lazy__))
        {
          ns.removeLast();
          ns.addToken(veryLazy__);
        }
        else
        {
          ns.addToken(lazy__);
        }
      }
      else
      {
        ns.addToken(AbstractValue.flyweightString(ss));
      }
    }
  }

  // The next token must be 'first', 'last' or a number, else
  // use an Index func.
  static private void processIndex(NodeSpecification  outer,
                                   AnyStringTokenizer t)
  {
    //System.out.println("processIndex start");
    String s = t.nextToken();
    //System.out.println("processIndex token " + s);

    if (s.equals(indexOpen__.toString()))
      throw new IllegalArgumentException("Nested [] is invalid. Use [] []");

    if (s.equals(indirectKeyOpen__.toString()))
      throw new IllegalArgumentException("Found { expected path or integer");

    if (Character.isDigit(s.charAt(0)))
    {
      outer.addToken(new LocateNode.Index
          (AbstractValue.flyweightConst(new ConstInt(Integer.parseInt(s)))));
	  }
    else if (s.equals(first__) || s.equals(atIterFirst__.toString()))
    {
      outer.addToken(new LocateNode.Index(indexFirst__));
    }
    else if (s.equals(last__) || s.equals(atIterLast__.toString()))
    {
      outer.addToken(new LocateNode.Index(indexLast__));
    }
    else
    {
      // Assume path.  Carry on tokenising but in a new NodeSpecification
      NodeSpecification n = new NodeSpecification();
      parsePath(n, t, s);
      outer.addToken(new LocateNode.Index(new LocateNode(n)));
    }

//    System.out.println("processIndex end");
  }

  // Recursively process indirections by adding LocateNodes to the
  // current NS.
  static private Any processIndirectKey(NodeSpecification  outer,
                                        AnyStringTokenizer t)
  {
    NodeSpecification n = new NodeSpecification();

    while (t.hasMoreTokens())
    {
      String s = t.nextToken();

      if (s.equals(indirectKeyOpen__.toString()))
      {
        // nested key indirection
        n.mustShallowCopy_ = true;
        processIndirectKey(n, t);
      }
      else if (s.equals(indexOpen__.toString()))
      {
        // nested index
        processIndex(n, t);
      }
      else if (s.equals(indirectKeyClose__.toString()))
      {
        LocateNode ret = new LocateNode(n)
        {
        	public String toString()
        	{
        		StringBuffer s = new StringBuffer();
        		s.append(indirectKeyOpen__.toString());
        		s.append(super.toString());
        		s.append(indirectKeyClose__.toString());
        		return s.toString();
        	}
        };
        if (outer != null)
          outer.addToken(ret);
        return ret;
      }
      else
      {
        StringI as = new ConstString(s);
        if (fixedTokens__.contains(as))
          n.addToken(fixedTokens__.get(as));
        else
          n.addToken(as);
      }
    }
    throw new IllegalArgumentException("Missing }");
  }

  // Recursively process indirections by adding LocateNodes to the
  // current NS.
  static private Any processIndirectNode(NodeSpecification  outer,
                                         AnyStringTokenizer t)
  {
    NodeSpecification n = new NodeSpecification();

    while (t.hasMoreTokens())
    {
      String s = t.nextToken();

      if (s.equals(indirectKeyOpen__.toString()))
      {
        // nested key indirection
        processIndirectKey(n, t);
      }
      else if (s.equals(indexOpen__.toString()))
      {
        // nested index
        processIndex(n, t);
      }
      else if (s.equals(indirectNodeOpen__.toString()))
      {
        // nested index
        processIndirectNode(n, t);
      }
      else if (s.equals(indirectNodeClose__.toString()))
      {
        n = setPrefices(n);
        LocateNode.Indirect ret = new LocateNode.Indirect(new LocateNode(n));
        if (outer != null)
          outer.addToken(ret);
        return ret;
      }
      else
        n.addToken(new ConstString(s));
    }
    throw new IllegalArgumentException("Missing )");
  }

	// An iterator which only returns the path items and not the
	// separators
  private class PathItemsIter extends AbstractIter implements Iter
  {
		private static final long serialVersionUID = 1L;

		private Iter i_ = NodeSpecification.this.createIterator();
		private Any  last_;

		public boolean hasNext()
		{
      if (last_ != null)
        return true;

			while (i_.hasNext())
			{
				last_ = i_.next();

				if (!NodeSpecification.isControl(last_))
					return true;

			}
			last_ = null;
			return false;
		}

		public Any next()
		{
      Any ret = last_;
      last_ = null;
			return ret;
		}

		public void remove()
		{
			i_.remove();
		}
  }

  private class ResolveIndirections extends AbstractVisitor
  {
		private static final long serialVersionUID = 1L;

		private NodeSpecification result_;
    private Any               context_;
    private boolean           resolving_;

    private ResolveIndirections(NodeSpecification n,
                                Any               context,
                                Transaction       t)
    {
      setTransaction(t);
      result_  = n;
      context_ = context;
    }

    public void visitAnyBoolean (BooleanI b)
    {
      result_.addToken(b);
    }

    public void visitAnyByte (ByteI b)
    {
      result_.addToken(b);
    }

    public void visitAnyChar (CharI c)
    {
      result_.addToken(c);
    }

    public void visitAnyInt (IntI i)
    {
      result_.addToken(i);
    }

    public void visitAnyShort (ShortI s)
    {
      result_.addToken(s);
    }

    public void visitAnyLong (LongI l)
    {
      result_.addToken(l);
    }

    public void visitAnyFloat (FloatI f)
    {
      result_.addToken(f);
    }

    public void visitAnyDouble (DoubleI d)
    {
      result_.addToken(d);
    }

    public void visitDecimal (Decimal d)
    {
      result_.addToken(d);
    }

    public void visitAnyString (StringI s)
    {
      if (resolving_)
      {
        NodeSpecification n = new NodeSpecification(s);
        // Probably not that important to preserve the state of the
        // resolving_ flag but in any case...
        resolving_ = false;
        resolve(this, n, false);
        resolving_ = true;
      }
      else
        result_.addToken(s);
    }

    public void visitAnyDate (DateI d)
    {
      result_.addToken(d);
    }

    public void visitMap (Map m)
    {
      result_.addToken(m);
    }

    public void visitArray (Array a)
    {
      // if its a NodeSpecification then resolve it. Otherwise just
      // use as the next element
      if (a instanceof NodeSpecification)
      {
        NodeSpecification n = (NodeSpecification)a;

        // If the nested NodeSpecification has embedded functions
        // then we will clone them when we hit them.  Otherwise
        // its read-only
        resolve(this, n, true);
      }
      else
        result_.addToken(a);
    }

    public void visitSet (Set s)
    {
      result_.addToken(s);
    }

    public void visitFunc (Func f)
    {
      try
      {
        Func ff = f;
        f = (Func)f.cloneAny();
        f.setTransaction(getTransaction());
        Any a     = f.execFunc(context_);
        if (a != null && a.isTransactional())
          a = f.doTransactionHandling(context_, a);
        resolving_ = true;
        if (a == null)
        {
          // If the function doesn't resolve then just leave it in
          // the NodeSpecification. This seems iffy but we are assuming
          // that this has occurred because the resolution is happening
          // in the wrong context, such as gui setup, and will be
          // resolved again later when the correct context is available.
          result_.addToken(ff);
          result_.mustShallowCopy_ = true;
          return;
        }
        a.accept(this);
        resolving_ = false;
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    public void visitAnyObject (ObjectI o)
    {
      result_.addToken(o);
    }

    public void visitUnknown(Any o)
    {
      result_.addToken(o);
    }

  }
}
