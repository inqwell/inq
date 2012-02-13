/**
 * Xylinq xyCurrency.pkey.sql include file for ORACLE
 *
 * Private & Confidential Copyright © Xylinq Ltd 2004.
 * All rights reserved.
 */

    auxcfg( map(
    "select-stmt",
      "
        select
          Currency         as \"Currency\",
          Description      as \"Description\",
          SettlementDays   as \"SettlementDays\",
          AccrualDays      as \"AccrualDays\",
          IntFixDays       as \"IntFixDays\",
          Active           as \"Active\",
          LastUpdated      as \"LastUpdated\",
          User_            as \"User\"
        from  XYCurrency
      ",
    "read-sql",
      "
        {select-stmt}
        where Currency  = {Currency}
      ",
    "write-sql",
      "
        merge into XYCurrency using dual
          on (Currency = {Currency})

          when matched then
                       update set Description     = {Description},
                                  SettlementDays  = {SettlementDays,number,#},
                                  AccrualDays     = {AccrualDays,number,#},
                                  IntFixDays      = {IntFixDays,number,#},
                                  Active          = {Active},
                                  LastUpdated     = to_date({LastUpdated,date,yyyy-MM-dd HH:mm:ss},''yyyy-mm-dd HH24:MI:SS''),
                                  User_           = {User}

          when not matched then
                       insert (Currency,
                               Description,
                               SettlementDays,
                               AccrualDays,
                               IntFixDays,
                               Active,
                               LastUpdated,
                               User_)
                       values ({Currency},
                               {Description},
                               {SettlementDays,number,#},
                               {AccrualDays,number,#},
                               {IntFixDays,number,#},
                               {Active},
                               to_date({LastUpdated,date,yyyy-MM-dd HH:mm:ss},''yyyy-mm-dd HH24:MI:SS''),
                               {User})
      ",
    "delete-sql",
      "
        delete from XYCurrency
        where Currency  = {Currency}
      "
    ))
