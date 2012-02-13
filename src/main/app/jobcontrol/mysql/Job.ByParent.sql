/**
 * Inwqell Job Control Package
 *
 * Job Primary Key sql expression
 *
 * Private & Confidential Copyright © Inqwell Ltd 2007.
 * All rights reserved.
 */

    auxcfg( map(
    "prepared", true,
    "read-sql",
      "
        {select-stmt}
        where (IsNull(?) and IsNull(ParentJob)) or
              ParentJob = ?
      ",
    "read-order",
      array a = ("ParentJob",
                 "ParentJob")
    ))
