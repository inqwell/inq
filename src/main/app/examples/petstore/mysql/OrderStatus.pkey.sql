/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * OrderStatus primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            OS.orderid       AS \"Order\",
            OS.linenum       AS \"LineItem\",
            OS.timestamp     AS \"Timestamp\",
            OS.status        AS \"Status\"
        FROM orderstatus OS
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE OS.orderid = ?
        AND   OS.linenum = ?
        ",
    "write-sql",
        "
        REPLACE orderstatus
        SET
            orderid       = ?,
            linenum       = ?,
            timestamp     = ?,
            status        = ?
        ",
    "delete-sql",
        "
        DELETE FROM orderstatus
        WHERE orderid = ?
        AND   linenum = ?
        "
))


