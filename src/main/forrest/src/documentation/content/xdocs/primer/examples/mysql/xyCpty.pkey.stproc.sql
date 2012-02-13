    // Use of a stored proc call
    // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE
    // TYPEDEF AND KEY FIELDS.

    auxcfg( map(
    "stproc", true,
    "read-sql",
      "{ call sp_readCptyUnique( ? ) }",
    "write-sql",
      "{ call sp_writeCpty( ?, ?, ?, ?, ?, ?, ?, ? ) }",
    "delete-sql",
      "{ call sp_deleteCpty( ? ) }"
    ))
