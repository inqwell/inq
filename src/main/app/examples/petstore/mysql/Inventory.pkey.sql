/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Inventory primary key include file - mysql
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
            I.itemid       AS \"Item\",
            I.qty          AS \"Qty\"
        FROM inventory I
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE I.itemid = ?
        ",
    "write-sql",
        "
        REPLACE inventory
        SET
            itemid       = ?,
            qty          = ?
        ",
    "delete-sql",
        "
        DELETE FROM inventory
        WHERE itemid = ?
        "
))


