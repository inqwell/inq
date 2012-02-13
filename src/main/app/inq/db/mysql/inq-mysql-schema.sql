/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * Inqwell
 *
 * Create the Inqwell database
 * Run this as root ie mysql -u root < inq-mysql-schema.sql
 *
 */

drop database if exists inqwell;
create database inqwell;

use inqwell;

-- create an inq user-login for the inqwell database
grant all on inqwell.* to 'inq'@'%' identified by 'inq123';
grant all on inqwell.* to 'inq'@'localhost' identified by 'inq123';
grant all on inqwell.* to 'inq'@'localhost.localdomain' identified by 'inq123';
-- Best way to remove the user is via Mysql Administrator tool!!!

/*
 * Unique Ids table
 */

CREATE TABLE inqUniqueId
(
    Name     VARCHAR(64) BINARY NOT NULL,
    Value    BIGINT             NOT NULL,
    LastUsed DATETIME           NOT NULL
) ENGINE=InnoDB;

ALTER TABLE inqUniqueId
ADD CONSTRAINT pk_inqUniqueId
PRIMARY KEY (Name);

/*
 * JobControl jobs table.
 * 
 * Private & Confidential Copyright © Xylinq Ltd 2004-2009
 * All rights reserved.
 */

CREATE TABLE inqJob
(
    Job             BIGINT             NOT NULL, -- Job id - internally generated
    ShortName       VARCHAR(32) BINARY NOT NULL,
    Description     VARCHAR(64) BINARY     NULL,
    TimerExpr       BLOB                   NULL, -- An expression that yields the inq timer for this job
    FunctionExpr    BLOB                   NULL, -- The expression called to run the job. Null implies Box
    JobOrder        INT                NOT NULL,
    Active          CHAR(1) BINARY     NOT NULL,
    ContinueOnError CHAR(1) BINARY     NOT NULL,
    BoxType         CHAR(1) BINARY     NOT NULL,
    ExitStatus      INT                NOT NULL, -- Last exit status of job
    LastRan         DATETIME               NULL, -- Time of last execution, or null if never run
    NextRuns        DATETIME               NULL, -- Time of next execution, when last evaluated
    LastDuration    BIGINT                 NULL, -- Duration of last execution in milliseconds, or null if never run
    ParentJob       BIGINT                 NULL, -- The box this job is in. Null if not boxed or a top-level box
    LastUpdated     DATETIME           NOT NULL,
    User            VARCHAR(16) BINARY NOT NULL
) ENGINE=InnoDB;

ALTER TABLE inqJob
ADD CONSTRAINT pk_inqJob
PRIMARY KEY (Job);

