/**
 * Xylinq  xyCurrency.pkey.sql  include  file
 *
 * Private & Confidential  Copyright  ©  Xylinq Ltd 2004.
 * All rights  reserved.
 */

    auxcfg(  map(
    "prepared",  true,
    "select-stmt",
      "
        select
          Currency,
          Description,
          SettlementDays,
          AccrualDays,
          IntFixDays,
          Active,
          LastUpdated,
          User
        from  XYCurrency
      ",
    "read-sql",
      "
        {select-stmt}
        where  Currency  =  ?
      ",
    "write-sql",
      "
        replace  XYCurrency
        set
          Currency        =  ?,
          Description     =  ?,
          SettlementDays  =  ?,
          AccrualDays     =  ?,
          IntFixDays      =  ?,
          Active          =  ?,
          LastUpdated     =  ?,
          User            =  ?
      ",
    "delete-sql",
      "
        delete from  XYCurrency
        where  Currency  =  ?
      "
    ))
