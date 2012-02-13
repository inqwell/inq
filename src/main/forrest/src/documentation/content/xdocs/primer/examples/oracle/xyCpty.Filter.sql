  /*
   * Cpty Filter Key Example in Oracle
   */
  auxcfg( map(
  "prepared", true,
  "read-sql",
    "
      {select-stmt},
      XYEntity E
      where  C.DomicileCountry = NVL(?, C.DomicileCountry)
      and    C.BaseCurrency    = NVL(?, C.BaseCurrency)
      and    C.Entity          = NVL(?, C.Entity)
      and    C.Entity          = E.Entity
      and    E.Type            = NVL(?, E.EntityType)
      and    E.GlobalLimit    >= NVL(?, E.GlobalLimit)
      and    E.GlobalLimit    <= NVL(?, E.GlobalLimit)
    "
  ))
