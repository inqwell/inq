/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * LineItem primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            L.orderid       AS \"Order\",
            L.linenum       AS \"LineItem\",
            L.itemid        AS \"Item\",
            L.quantity      AS \"Qty\",
            L.unitprice     AS \"UnitPrice\"
        FROM lineitem L
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE L.orderid = ?
        AND   L.linenum = ?
        ",
    "write-sql",
        "
        REPLACE lineitem
        SET
            orderid       = ?,
            linenum       = ?,
            itemid        = ?,
            quantity      = ?,
            unitprice     = ?
        ",
    "delete-sql",
        "
        DELETE FROM lineitem
        WHERE orderid = ?
        AND   linenum = ?
        "
))


