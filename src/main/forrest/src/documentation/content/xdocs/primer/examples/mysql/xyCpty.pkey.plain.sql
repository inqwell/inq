    //
    // Cpty Primary Key.
    // MySql version using plain text SQL
    //

    auxcfg( map(
    "select-stmt",
      "
        select
          C.Cpty,
          C.LongName,
          C.Entity,
          C.BaseCurrency,
          C.DomicileCountry,
          C.Active,
          C.LastUpdated,
          C.User
        from  XYCpty C
      ",
    "read-sql",
      "
        {select-stmt}
        where C.Cpty  = {Cpty}
      ",
    "write-sql",
      "
        replace  XYCpty
        set
            Cpty            = {Cpty},
            LongName        = {LongName},
            Entity          = {Entity},
            BaseCurrency    = {BaseCurrency},
            DomicileCountry = {DomicileCountry},
            Active          = {Active},
            LastUpdated     = {LastUpdated,date,yyyy-MM-dd HH:mm:ss},
            User            = {User}
      ",
    "delete-sql",
      "
        delete from XYCpty
        where Cpty  = {Cpty}
      "
    ))
