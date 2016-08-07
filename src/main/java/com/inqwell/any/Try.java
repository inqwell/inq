/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Try.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 19:59:22 $
 */
package com.inqwell.any;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Open an exception try/catch block.  The first operand is executed
 * inside the try block.  If an exception is generated and the
 * second operand is non-null, it is executed as the catch block and
 * the exception is consumed.  If there is no catch block
 * <code>Try</code> instance is degenerate and any exception is
 * passed to the caller.
 * <p>
 * When the catch block is executed the exception is available as
 * <code>$stack.@exception</code>.  Any exception which occurs while
 * executing the catch block is passed to the caller.  The catch
 * catch block can throw the original exception using
 * the <code>throw</code> tag.
 * <p>
 * An optional third argument forms the finally block
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class Try extends    AbstractFunc
								 implements Cloneable
{
	private static final long serialVersionUID = 1L;

	private Any try_;
	private Any catch_;
	private Any finally_;

	public Try(Any eTry, Any eCatch)
	{
		this(eTry, eCatch, null);
	}

	public Try(Any eTry, Any eCatch, Any eFinally)
	{
		try_     = eTry;
		catch_   = eCatch;
		finally_ = eFinally;
	}

	public Any exec(Any a) throws AnyException
	{
		Any ret = null;

    Transaction t      = getTransaction();
    Transaction parent = null;
    if (isNestedTransaction())
    {
      Transaction child = (Transaction)t.buildNew(null);
      t.setChild(child);
      parent = t;
      t      = child;
      parent.getProcess().setTransaction(child);
    }

    // Assume the exception has been raised in user script, rather
    // then within a transaction commit (whether or not this object
    // represents a nested transaction.
    boolean      commitException = false;

    // If there is already an exception on the stack from an outer
    // try/catch block then this will be set
    ExceptionI outerEx = null;

    // If an exception is raised in the try or transaction block
    // then this will be set.
    ExceptionI curEx   = null;
    
    // For break/continue in the face of try/catch
    FlowControlException flowEx = null;
    
    // Whether we've been killed. If so allow any finally
    // expresswion to be executed.
    boolean killed = false;

    // These are used to return either AnyException, AnyRuntimeException
    // or FlowControlException from the catch block (when there is one)
    AnyException[]         anyEx  = null;
    AnyRuntimeException[]  anyREx = null;
    FlowControlException[] aflowEx = null;

    if (catch_ != null)
    {
      anyEx   = new AnyException[1];
      anyREx  = new AnyRuntimeException[1];
      aflowEx = new FlowControlException[1];

      anyEx[0]   = null;
      anyREx[0]  = null;
      aflowEx[0] = null;
    }

    int throwLine = -1;

		try
		{
			ret    = EvalExpr.evalFunc(t,
																 a,
																 try_);

			// No exception occurred. If we are a nested transaction then
      // commit now, unless there is a finally expression, in
			// which case we defer committing until after then
      if (isNestedTransaction() && finally_ == null)
      {
        commitException = true;  // if an exception is thrown during the commit
        // We want to distinguish between an exception that may
        // have occurred during normal script and one that occurs
        // in the commit phase.
        t.commit();
      }
		}

		// There are a few exception possibilities
		
		// Have we been killed?
		catch(ProcessKilledException pkex)
		{
		  // We have been killed - reset the killed flag
		  // temporarily. The flag is checked at each statement
		  // for rapid exit, but we execute finally blocks on
		  // the way out. We don't execute catch blocks
		  killed = true;
		  ((UserProcess)t.getProcess()).resetKilled(false);
		  // t.abort(); No - abort after any finally block and at the
		  //            level of the transaction
		}
		// break/continue are implemented as exceptions
    catch(FlowControlException flex)
    {
      flowEx = flex;
    }
		catch (AnyException aex)
		{
		  curEx = aex;
			if (catch_ != null)
			{
        // We caught an exception (obviously)
        // If it was during the commit phase then abort the (nested) transaction
        // before executing the catch block.
        if (commitException)
        {
          curEx.setCommit(true);
          commitException = false;
          t.abort();
        }

        // If the exception occurred during the commit phase then the
        // transaction was aborted above. We've dealt with that aspect
        // of the exception and reset the flag, indicating we
        // are "back to normal". Any catch block is free to use the (now
        // cleared down) transaction as it wishes.

        outerEx = pushException(aex, t, true);

        // If the catch expression throws (either a new exception or
        // by rethrowing the one just caught) then the return value from
        // doExpression is that exception. In the Java sense, execution continues
        // as normal for the time being and the Java throw happens in
        // the Java finally...
				ret = doExpression(catch_, a, t, anyEx, anyREx, aflowEx);
				
				// Check for process killed happening while executing the
				// catch block. The flag in the process has already been reset.
				killed = (anyREx[0] instanceof ProcessKilledException);

        // What happens in the case of a nested transaction? If it was
				// a commit exception then we've already aborted it prior to
        // the catch expression though it remains viable and in the
        // process, so the catch expression could have used it.
        // We define that successful execution of the catch expression
        // means we have dealt with the exception (which includes aborting
        // the current nested transaction contents) and that anything
        // now contained within it should be committed. We do this in
        // the (Java!) finally block.

        // Further, if the catch expression above *does* throw
				// then we (may) have a new exception leaving here (after the
				// Java finally and any Inq finally). The Inq line number will be
				// the point at which the catch expression threw, which is OK.
        // If the catch expression rethrew the caught exception then
        // the Inq line number is restored from that held in the
        // exception (see pushException), which is also OK.
				// If there is a finally expression then remember the line number
        // so that after executing it we can restore the line number
        // for the actual throw.
        if (finally_ != null)
          throwLine = t.getLineNumber();
			}
			else
			{
        // We've incurred an exception and there is no catch block.
        // If there is a finally block
        // then remember the line number so we can reinstate it
        // after the finally block has executed and the exception is
        // (finally) thrown
        if (finally_ != null)
          throwLine = t.getLineNumber();

        //System.out.println("**** 1");
        //aex.printStackTrace();

        // If we are a nested transaction then abort it now. It's not
        // relevant whether the exception occurred during script execution
        // or the commit phase as we are going to throw and have made
        // no attempt to recover, but update the exception anyway.
        // Though the transaction has been aborted, any finally block can
        // still use it.
        if (isNestedTransaction())
        {
          curEx.setCommit(commitException);
          commitException = false;
          t.abort();
        }
			}
		}
		catch (AnyRuntimeException arex)
		{
		  curEx = arex;
			if (catch_ != null)
			{
			  // See comments above


        if (commitException)
        {
          curEx.setCommit(true);
          commitException = false;
          t.abort();
        }

        outerEx = pushException(arex, t, true);
				ret = doExpression(catch_, a, t, anyEx, anyREx, aflowEx);
				killed = (anyREx[0] instanceof ProcessKilledException);

        if (finally_ != null)
          throwLine = t.getLineNumber();
			}
			else
			{
        if (finally_ != null)
          throwLine = t.getLineNumber();

        if (isNestedTransaction())
        {
          curEx.setCommit(commitException);
          commitException = false;
          t.abort();
        }
			}
		}
		catch (RuntimeException e)
		{
		  curEx = new RuntimeContainedException(e);

			if (catch_ != null)
			{
        if (commitException)
        {
          curEx.setCommit(true);
          commitException = false;
          t.abort();
        }

        outerEx = pushException(curEx, t, true);

				ret = doExpression(catch_, a, t, anyEx, anyREx, aflowEx);
				killed = (anyREx[0] instanceof ProcessKilledException);

        if (finally_ != null)
          throwLine = t.getLineNumber();
			}
			else
			{
        if (finally_ != null)
          throwLine = t.getLineNumber();

        if (isNestedTransaction())
        {
          curEx.setCommit(commitException);
          commitException = false;
          t.abort();
        }
			}
		}
		finally
		{
      // Something to hold any exception we might get in finally processing
      Exception fe = null;

      // When there was no catch expression, we've already remembered
      // the line number in throwLine before overwriting it in the transaction
      // by executing the finally expression.
      // When there is an exception the curEx variable holds it, regardless of
      // whether there was a catch block. Otherwise it will be null.
      if (finally_ != null)
      {
        try
        {
        	// Check if the process was killed at a lower stack frame
        	// and temporarily reset for the execution of the finally
        	// block.
        	killed |= ((UserProcess)t.getProcess()).resetKilled(false);
        	
          ret    = EvalExpr.evalFunc(t,
                                     a,
                                     finally_);
          if (isNestedTransaction())
          {
          	if (killed                          // already killed
                || (catch_ == null &&           // there is no catch block...
                    curEx != null)              // ...an exception was thrown
                || (catch_ != null &&           // there is a catch block...
                    (anyEx[0] != null ||        // ...it threw an exception
                     anyREx[0] != null)))
          	{
          		t.abort();
          	}
          	else
          	{
              commitException = true;  // if an exception is thrown during the commit
              // We want to distinguish between an exception that may
              // have occurred during normal script and one that occurs
              // in the commit phase.
              t.commit();
          	}
          }
        }
        catch(ProcessKilledException pkex)
        {
          // Process killed during finally execution
          killed = true;
          ((UserProcess)t.getProcess()).resetKilled(true);
          if (isNestedTransaction())
            t.abort();
        }
        catch (FlowControlException flex)
        {
          flowEx = flex;
          // Meh - even messier when someone codes a break/continue/return
          // inside a finally block....
          try
          {
            if (isNestedTransaction())
            {
            	if (killed                          // already killed
                  || (catch_ == null &&           // there is no catch block...
                      curEx != null)              // ...an exception was thrown
                  || (catch_ != null &&           // there is a catch block...
                      (anyEx[0] != null ||        // ...it threw an exception
                       anyREx[0] != null)))
            	{
            		t.abort();
            	}
            	else
            	{
                commitException = true;  // if an exception is thrown during the commit
                // We want to distinguish between an exception that may
                // have occurred during normal script and one that occurs
                // in the commit phase.
                t.commit();
            	}
            }
          }
          catch(Exception ex)
          {
          	fe = ex;
            if (isNestedTransaction())
              t.abort();
          }
        }
        catch (Exception ex)
        {
          // Gawd, the scripted finally expression or the commit (when
          // nested) have thrown.
          // Remember the exception because this is the one we
          // will throw now. Note it masks the original exception
          // unless we add code to arrange to carry it in the new one.
          // NB: fe != null && commitException means a commit exception
          // occurred in this case.
          fe = ex;
          if (isNestedTransaction())
            t.abort();
        }

        // Poping any exception pushed in the optional catch block
        // here (after execution of any finally block) means that it
        // is available to the finally block. Not sure if this is
        // such a great idea as it could be rethrown and that's a
        // little pointless. TODO: Consider this.
        popException(outerEx, curEx, t);
      }
      else  // No Inq finally {}
      {
        popException(outerEx, curEx, t);

        // There is no finally expression. If there is a nested transaction
        // and we are not about to throw an exception (either because there was
        // no catch block, the catch block itself threw or re-threw or the
        // process was killed) then commit it. Otherwise abort it.
        // Its OK to call commit() if the transaction
        // has already been aborted because of some problem earlier.
        // Its also OK to call commit() even when there is nothing in
        // the transaction but the call here is necessary in case anything
        // got added by a successful catch execution.
        if (isNestedTransaction())
        {
          if (killed 
           || (catch_ == null &&           // there is no catch block...
               curEx != null)              // ...an exception was thrown
           || (catch_ != null &&           // there is a catch block...
               (anyEx[0] != null ||        // ...it threw an exception
                anyREx[0] != null)))
          {
            t.abort();
          }
          else
          {
          	try
          	{
              commitException = false;
              t.commit();
            }
            catch (Exception ex)
            {
            	// Nested commit croaked. Remember the exception again
            	// There is only one possible source so we can set the flag here
              commitException = true;
            	fe = ex;
            	t.abort();
            }
          }
        }
      }

      // Very important that we put the parent txn back in the
      // process. I reckon we must always get here.
      if (parent != null)
      {
        parent.getProcess().setTransaction(parent);
        parent.setChild(null);
      }
      
      // Put the killed flag back in for upper frames
      ((UserProcess)t.getProcess()).resetKilled(killed);

      // If we are to throw then it happens now. There are a number of exception
      // sources:
      //  1) an exception occurred and there was no catch block. The exception
      //     is in curEx
      //  2) there is a catch block and it threw an exception. The exception
      //     is in anyEx/anyREx. If anyEx[0]/anyREx[0] == curEx then the
      //     catch block is rethrowing the exception that occurred in the try
      //     block.
      //  3) there is a finally block and it has incurred an exception. The
      //     exception is in fe. When fe is not null we choose to throw it
      //     rather than any exception we might have in curEx or anyEx/anyREx.
      //     TODO: contain any such exception inside a new exception
      //     class so we can carry out, rather than mask it.
      if (fe != null)
      {
        // When it is the finally expression that has thrown the line number
        // prevailing in the process is OK.
        
        RuntimeContainedException rcex = new RuntimeContainedException(fe);
        rcex.setCommit(commitException);
        throw rcex;
      }
      else if (anyEx != null && anyEx[0] != null)
      {
        // Catch block (re)threw.
      	// Restore the line number at the point of error.  If there
        // is no finally exception then we are about to throw it, so
        // put the correct line number back for the Inq stack.
        if (throwLine > 0)
          t.setLineNumber(throwLine);
        
        throw anyEx[0];
      }
      else if (anyREx != null && anyREx[0] != null)
      {
      	// As above
        if (throwLine > 0)
          t.setLineNumber(throwLine);
        
        throw anyREx[0];
      }
      else if (curEx != null && catch_ == null)
      {
      	// Try threw, there is no catch block
        if (throwLine > 0)
          t.setLineNumber(throwLine);
        
        if (curEx instanceof AnyException)
          throw (AnyException)curEx;
        else
          throw (AnyRuntimeException)curEx;
      }
      
      // Check if there was a flow control exception and if so throw it
      // for processing by the containing loop/call. Of course, it doesn't make
      // much sense to have such a statement in both catch and finally
      // when both of these are present, but we give priority to the
      // catch one.
      if (aflowEx != null && aflowEx[0] != null)
        flowEx = aflowEx[0];
      
      if (flowEx != null)
        throw flowEx;
    }

		return ret;
	}

  public Object clone () throws CloneNotSupportedException
  {
    Try t = (Try)super.clone();

    t.try_     = try_.cloneAny();
    t.catch_   = AbstractAny.cloneOrNull(catch_);
    t.finally_ = AbstractAny.cloneOrNull(finally_);

    return t;
  }

  protected boolean isNestedTransaction()
  {
    return false;
  }
  
  protected boolean isCreateEvents()
  {
    return true;
  }
	private ExceptionI pushException(ExceptionI  e,
                                   Transaction t,
                                   boolean     setLineNumber)
	{
    if (setLineNumber)
      e.setLineNumber(t.getLineNumber());

		Map stackFrame = getTransaction().getCurrentStackFrame();

		ExceptionI ret = (ExceptionI)stackFrame.getIfContains(NodeSpecification.atException__);
		Any javaStack = null;

		if (ret != null)
    {
      stackFrame.remove(NodeSpecification.atException__);
      stackFrame.remove(NodeSpecification.atExMsg__);
      stackFrame.remove(NodeSpecification.atExName__);
      stackFrame.remove(NodeSpecification.atExEnviro__);
      javaStack = stackFrame.remove(NodeSpecification.atJavaStack__);
      if (ret.getUserInfo() != null)
        stackFrame.remove(NodeSpecification.atExInfo__);
      if (ret.getCallStack() != null)
        stackFrame.remove(NodeSpecification.atStackTrace__);
      if (ret.isCommit())
        stackFrame.remove(NodeSpecification.atExCommit__);
    }

    if (e != null)
    {
      stackFrame.add(NodeSpecification.atException__, e);
      stackFrame.add(NodeSpecification.atExMsg__, e.getExceptionMessage());
      stackFrame.add(NodeSpecification.atExName__, e.getExceptionName());
      stackFrame.add(NodeSpecification.atExEnviro__, e.isUser() ? AnyBoolean.FALSE : AnyBoolean.TRUE);
      if (javaStack == null)
        javaStack = new JavaStack();
      stackFrame.add(NodeSpecification.atJavaStack__, javaStack);

  		if (e.getUserInfo() != null)
        stackFrame.add(NodeSpecification.atExInfo__, e.getUserInfo());

      Any callStack;
      if ((callStack = e.fillInCallStack(t)) != null)
        stackFrame.add(NodeSpecification.atStackTrace__, callStack);

      if (e.isCommit())
        stackFrame.add(NodeSpecification.atExCommit__, AnyBoolean.TRUE);

    }

    return ret;
	}

  private void popException(ExceptionI  outerEx,
                            ExceptionI  curEx,
                            Transaction t)
  {
    if (outerEx == null && curEx == null)
      return;

    if (outerEx != null)
      pushException(outerEx, t, false);
    else if (curEx != null)
    {
      Map stackFrame = getTransaction().getCurrentStackFrame();
      stackFrame.remove(NodeSpecification.atException__);
      stackFrame.remove(NodeSpecification.atExName__);
      stackFrame.remove(NodeSpecification.atExEnviro__);
      stackFrame.remove(NodeSpecification.atExMsg__);
      stackFrame.remove(NodeSpecification.atJavaStack__);
      if (curEx.getUserInfo() != null)
        stackFrame.remove(NodeSpecification.atExInfo__);
      if (curEx.getCallStack() != null)
        stackFrame.remove(NodeSpecification.atStackTrace__);
    }
  }

  /**
   * Run the given expression trapping any possible exception it may
   * throw. If the expression throws an exception,
   * either because it incurs one or rethrows the one caught, then this
   * is the return value. Otherwise the result of the expression is returned
   * @param expr the expression to execute
   * @param a expression context
   * @param t current transaction
   * @return the expression result or an exception when one is thrown.
   */
	private Any doExpression(Any                    expr,
	                         Any                    a,
	                         Transaction            t,
                           AnyException[]         anyEx,
                           AnyRuntimeException[]  anyREx,
                           FlowControlException[] flowEx) throws AnyException
	{
    Any ret = null;
    try
    {
		  ret = EvalExpr.evalFunc(t,
														  a,
														  expr);
    }
    catch(FlowControlException e)
    {
      flowEx[0] = e;
    }
    catch(AnyException e)
    {
      anyEx[0] = e;
    }
    catch(AnyRuntimeException e)
    {
    	// NB. This could be a ProcessKilledException. Pass
    	// it back through here as a convenience. Reset
    	// the killed flag in any case
      ((UserProcess)t.getProcess()).resetKilled(false);
      anyREx[0] = e;
    }
    catch(Exception e)
    {
      anyREx[0] = new RuntimeContainedException(e);
    }

    return ret;
	}
	
	private class JavaStack extends AbstractFunc
	{
	  public Any exec(Any a) throws AnyException
	  {
      Map stackFrame = getTransaction().getCurrentStackFrame();
      ExceptionI e = (ExceptionI)stackFrame.getIfContains(NodeSpecification.atException__);
      StringI ret = AnyString.NULL;
      if (e != null)
      {
        StringWriter s = new StringWriter();
        PrintWriter  p = new PrintWriter(s);
        e.printStackTrace(p);
        p.close();
        ret = new AnyString(s.toString());
      }
      
	    return ret;
	  }
	}
}
