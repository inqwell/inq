/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Product primary key include file - mysql
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
            P.productid    AS \"Product\",
            P.category     AS \"Category\",
            P.name         AS \"Name\",
            P.icon         AS \"Icon\",
            P.descn        AS \"Description\"
        FROM product P
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE P.productid = ?
        ",
    "write-sql",
        "
        REPLACE product
        SET
            productid    = ?,
            category     = ?,
            name         = ?,
            icon         = ?,
            descn        = ?
        ",
    "delete-sql",
        "
        DELETE FROM product
        WHERE productid = ?
        "
))


