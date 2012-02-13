/*
 * Entity Table
 */
drop table if exists XYEntity;

create table IF NOT EXISTS XYEntity
(
  Entity        varchar(12) BINARY NOT NULL,
  LongName      varchar(50) BINARY NOT NULL,
  Type          char(1) BINARY NOT NULL,
  GlobalLimit   decimal(15,2),
  Active        char(1) BINARY NOT NULL, -- Y=Active
  LastUpdated   datetime NOT NULL,
  User          varchar(16) BINARY NOT NULL
);
alter  table XYEntity add unique XYEntity_UC1 (Entity);

