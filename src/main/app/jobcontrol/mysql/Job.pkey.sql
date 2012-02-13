/**
 * Inwqell Job Control Package
 *
 * Job Primary Key sql expression
 *
 * Private & Confidential Copyright © Inqwell Ltd 2007.
 * All rights reserved.
 */

    auxcfg( map(
	  // use prepared stmt so that we can use '?' notation for column values
	  // instead of having to define all the formatting.
	  // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE
	  // TYPEDEF AND KEY FIELDS.
	  "prepared", true,
	  "select-stmt",
	    "
	      select
          J.Job,
          J.ShortName,
        	J.Description,
          J.TimerExpr,
          J.FunctionExpr,
          J.JobOrder,
          J.Active,
          J.ContinueOnError,
          J.BoxType,
          J.ExitStatus,
          J.LastRan,
          J.NextRuns,
          J.LastDuration,
          J.ParentJob,
        	J.LastUpdated,
        	J.User
	      from  inqJob J
	    ",
		"read-sql",
			"
			  {select-stmt}
				where Job  = ?
			",
		"write-sql",
			"
			  replace	inqJob
			  set
        	Job             = ?,
        	ShortName       = ?,
        	Description     = ?,
        	TimerExpr       = ?,
        	FunctionExpr    = ?,
        	JobOrder        = ?,
        	Active          = ?,
        	ContinueOnError = ?,
        	BoxType         = ?,
        	ExitStatus      = ?,
        	LastRan         = ?,
        	NextRuns        = ?,
        	LastDuration    = ?,
        	ParentJob       = ?,
        	LastUpdated     = ?,
        	User            = ?
			",
		"delete-sql",
			"
				delete from inqJob
				where Job  = ?
			"
	  ))
