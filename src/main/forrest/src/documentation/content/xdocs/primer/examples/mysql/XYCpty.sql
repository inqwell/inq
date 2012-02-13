/*
 * Counterparty Table
 */
drop table if exists XYCpty;

create table IF NOT EXISTS XYCpty
(
  Cpty             varchar(12) BINARY NOT NULL,
  LongName         varchar(50) BINARY NOT NULL,
  Entity           varchar(12) BINARY NOT NULL,
  BaseCurrency     char(3) BINARY NOT NULL,
  DomicileCountry  char(2) BINARY NOT NULL,
  Active           char(1) BINARY NOT NULL, -- Y=Active
  LastUpdated      datetime NOT NULL,
  User             varchar(16) BINARY NOT NULL
);

alter  table XYCpty add unique XYCpty_UC1 (Cpty);
alter  table XYCpty add index  XYCpty_Entity (Entity);


