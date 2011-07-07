/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Category primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE PKEY
    //       AND REPLACE FIELDS IN SAME ORDER AS TYPEDEF
    "prepared", true,
    "select-stmt",
        "
        SELECT
            C.catid        AS \"Category\",
            C.name         AS \"Name\",
            C.icon         AS \"Icon\",
            C.descn        AS \"Description\"
        FROM category C
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE C.catid = ?
        ",
    "write-sql",
        "
        REPLACE category
        SET
            catid        = ?,
            name         = ?,
            icon         = ?,
            descn        = ?
        ",
    "delete-sql",
        "
        DELETE FROM category
        WHERE catid = ?
        "
))


