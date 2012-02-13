    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE
    // TYPEDEF AND KEY FIELDS.

    // Further points to note regarding Oracle:
    //  1) The result sets always return the column names as upper case
    //     so we alias them to match the typedef field names, which are
    //     case sensitive.
    //  2) Update/insert requires one clause for each, depending on
    //     whether the row exists or not. Every time a ? appears in
    //     the syntax we consume another input parameter so the natural
    //     ordering of the fields cannot be used. The explicit write-order
    //     is used to identify the arguments in order and can refer
    //     to fields as often as necessary.
    auxcfg( map(
    "prepared", true,
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
        where Cpty  = ?
      ",
    "write-sql",
      "
        merge into XYCpty using dual
          on (Cpty = ?)

          when matched then
                       update set LongName        = ?,
                                  Entity          = ?,
                                  BaseCurrency    = ?,
                                  DomicileCountry = ?,
                                  Active          = ?,
                                  LastUpdated     = ?,
                                  User_           = ?

          when not matched then
                       insert (
                               Cpty,
                               LongName,
                               Entity,
                               BaseCurrency,
                               DomicileCountry,
                               Active,
                               LastUpdated,
                               User_
                              )
                       values (?,
                               ?,
                               ?,
                               ?,
                               ?,
                               ?,
                               ?,
                               ?)
      ",
    // Due to Oracle syntax we override the field order in the typedef
    // with an explicit write-order because the fields need to appear so
    // many times.
    "write-order",
      array a = (
                  "Cpty",
                  "LongName",
                  "Entity",
                  "BaseCurrency",
                  "DomicileCountry",
                  "Active",
                  "LastUpdated",
                  "User",
                  "Cpty",
                  "LongName",
                  "Entity",
                  "BaseCurrency",
                  "DomicileCountry",
                  "Active",
                  "LastUpdated",
                  "User"
                 ),
    "delete-sql",
      "
        delete from XYCpty
        where Cpty  = ?
      "
    ))
