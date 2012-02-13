/**
 * xyCpty.ByEntityType.sql include file
 */

    auxcfg( map(
    "prepared", true,
    "read-sql",
      "
        {select-stmt},
        XYEntity E
        and    C.Entity = E.Entity     // Table Join
        and    E.Type   = ?            // Foreign field
      "
    ))
