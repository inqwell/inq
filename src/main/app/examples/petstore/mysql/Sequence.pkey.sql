/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Sequence primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            S.name          AS \"Sequence\",
            S.nextid        AS \"Value\"
        FROM sequence S
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE S.name = ?
        ",
    "write-sql",
        "
        REPLACE sequence
        SET
            name        = ?,
            nextid      = ?
        ",
    "delete-sql",
        "
        DELETE FROM sequence
        WHERE name = ?
        "
))


