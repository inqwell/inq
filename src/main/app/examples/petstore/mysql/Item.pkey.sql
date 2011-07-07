/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Item primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            I.itemid        AS \"Item\",
            I.productid     AS \"Product\",
            I.listprice     AS \"ListPrice\",
            I.unitcost      AS \"UnitCost\",
            I.lastprice     AS \"LastPrice\",
            I.lastpricemove AS \"LsatPriceMove\",
            I.supplier      AS \"Supplier\",
            I.status        AS \"Status\",
            I.attr1         AS \"Attr1\",
            I.attr2         AS \"Attr2\",
            I.attr3         AS \"Attr3\",
            I.attr4         AS \"Attr4\",
            I.attr5         AS \"Attr5\"
        FROM item I
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE I.itemid = ?
        ",
    "write-sql",
        "
        REPLACE item
        SET
            itemid        = ?,
            productid     = ?,
            listprice     = ?,
            unitcost      = ?,
            lastprice     = ?,
            lastpricemove = ?,
            supplier      = ?,
            status        = ?,
            attr1         = ?,
            attr2         = ?,
            attr3         = ?,
            attr4         = ?,
            attr5         = ?
        ",
    "delete-sql",
        "
        DELETE FROM item
        WHERE itemid = ?
        "
))


