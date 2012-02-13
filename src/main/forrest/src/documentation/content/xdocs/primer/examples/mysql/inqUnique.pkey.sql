/**
 * Inqwell Unique Functions
 *
 * Generate Unique Ids.
 *
 * Private & Confidential Copyright © Inqwell Ltd 2004.
 * All rights reserved.
 */

    auxcfg( map(
    "prepared", true,
    "select-stmt",
      "
            select
              Name,
              Value,
              LastUsed
            from    UniqueId
    ",
    "read-sql",
      "
        {select-stmt}
            where    Name   = ?
      ",
    "write-sql",
      "
            replace  UniqueId set
              Name          = ?,
              Value         = ?,
              LastUsed      = ?
      "
      // can't delete them
      ))
