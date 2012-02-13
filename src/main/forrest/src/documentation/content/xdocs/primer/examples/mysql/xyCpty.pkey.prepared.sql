    // Use prepared statement so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE
    // TYPEDEF AND KEY FIELDS.

    auxcfg( map(
    "prepared", true,
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
        where C.Cpty = ?
      ",
    "write-sql",
      "
        replace  XYCpty
        set
          Cpty            = ?,
          LongName        = ?,
          Entity          = ?,
          BaseCurrency    = ?,
          DomicileCountry = ?,
          Active          = ?,
          LastUpdated     = ?,
          User            = ?
      ",
    "delete-sql",
      "
        delete from XYCpty
        where Cpty  = ?
      "
    ))
