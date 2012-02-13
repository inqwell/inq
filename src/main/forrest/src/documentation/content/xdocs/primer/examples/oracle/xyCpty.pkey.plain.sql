    //
    // Cpty Primary Key.
    // Oracle version using plain text SQL
    //

    // Points to note regarding Oracle:
    //  1) The result sets always return the column names as upper case
    //     so we alias them to match the typedef field names, which are
    //     case sensitive.
    //  2) The TO_DATE function converts a string representing a date/time
    //     to a database date/time. The Inq format needs to match the Oracle
    //     format to get the desired result.  Notice also how the necessary
    //     quotes for the Oracle format are repeated. This is because the
    //     overall statement is parsed twice by Inq, the first time to
    //     substitute the select-stmt. This removes one set of quotes.

    auxcfg( map(
    "select-stmt",
      "
        select
          C.Cpty             as \"Cpty\",
          C.LongName         as \"LongName\",
          C.Entity           as \"Entity\",
          C.BaseCurrency     as \"BaseCurrency\",
          C.DomicileCountry  as \"DomicileCountry\",
          C.Active           as \"Active\",
          C.LastUpdated      as \"LastUpdated\",
          C.User_            as \"User\"
        from  XYCpty C
      ",
    "read-sql",
      "
        {select-stmt}
        where C.Cpty  = {Cpty}
      ",
    "write-sql",
      "
        merge into XYCpty using dual
          on (Cpty = {Cpty})

          when matched then
                       update set LongName        = {LongName},
                                  Entity          = {Entity},
                                  BaseCurrency    = {BaseCurrency},
                                  DomicileCountry = {DomicileCountry},
                                  Active          = {Active},
                                  LastUpdated     = to_date({LastUpdated,date,yyyy-MM-dd HH:mm:ss},''yyyy-mm-dd HH24:MI:SS''),
                                  User_           = {User}

          when not matched then
                       insert (Cpty,
                               LongName,
                               Entity,
                               BaseCurrency,
                               DomicileCountry,
                               Active,
                               LastUpdated,
                               User_)
                       values ({Cpty},
                               {LongName},
                               {Entity},
                               {BaseCurrency},
                               {DomicileCountry},
                               {Active},
                               to_date({LastUpdated,date,yyyy-MM-dd HH:mm:ss},''yyyy-mm-dd HH24:MI:SS''),
                               {User})
      ",
    "delete-sql",
      "
        delete from XYCpty
        where Cpty  = {Cpty}
      "
    ))


