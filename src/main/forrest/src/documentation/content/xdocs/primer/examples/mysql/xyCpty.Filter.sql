  /*
   * Cpty Filter Key Example in MySql
   */
  auxcfg( map(
  "prepared", true,
  "read-order",       array a = (
                  "DomicileCountry",
                  "DomicileCountry",
                  "BaseCurrency",
                  "BaseCurrency",
                  "Active",
                  "Active",
                  "Entity",
                  "Entity",
                  "EntityType",
                  "EntityType",
                  "GlobalLimitLower",
                  "GlobalLimitLower",
                  "GlobalLimitUpper",
                  "GlobalLimitUpper"
                 ),

  "read-sql",
    "
      {select-stmt},
      XYEntity E
      where  ( C.DomicileCountry = ? OR IsNull(?) )
      and    ( C.BaseCurrency    = ? OR IsNull(?) )
      and    ( C.Active          = ? OR IsNull(?) )
      and    ( C.Entity          = ? OR IsNull(?) )
      and    C.Entity = E.Entity
      and    ( E.Type            = ? OR IsNull(?) )
      and    ( E.GlobalLimit    >= ? OR IsNull(?) )
      and    ( E.GlobalLimit    <= ? OR IsNull(?) )
    "
  ))
