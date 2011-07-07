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
            O.orderid         AS \"Order\",
            O.userid          AS \"Account\",
            O.orderdate       AS \"OrderDate\",
            O.status          AS \"Status\",
            O.shipaddr1       AS \"ShipAddr1\",
            O.shipaddr2       AS \"ShipAddr2\",
            O.shipcity        AS \"ShipCity\",
            O.shipstate       AS \"ShipState\",
            O.shipzip         AS \"ShipZIP\",
            O.shipcountry     AS \"ShipCountry\",
            O.billaddr1       AS \"BillAddr1\",
            O.billaddr2       AS \"BillAddr2\",
            O.billcity        AS \"BillCity\",
            O.billstate       AS \"BillState\",
            O.billzip         AS \"BillZIP\",
            O.billcountry     AS \"BillCountry\",
            O.courier         AS \"Courier\",
            O.totalprice      AS \"TotalPrice\",
            O.billtofirstname AS \"BillToFirstName\",
            O.billtolastname  AS \"BillToLastName\",
            O.shiptofirstname AS \"ShipToFirstName\",
            O.shiptolastname  AS \"ShipToLastName\",
            O.creditcard      AS \"CreditCard\",
            O.exprdate        AS \"CardExp\",
            O.cardtype        AS \"CardType\",
            O.locale          AS \"Locale\"
        FROM orders O
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE O.orderid = ?
        ",
    "write-sql",
        "
        REPLACE orders
        SET
            orderid         = ?,
            userid          = ?,
            orderdate       = ?,
            status          = ?,
            shipaddr1       = ?,
            shipaddr2       = ?,
            shipcity        = ?,
            shipstate       = ?,
            shipzip         = ?,
            shipcountry     = ?,
            billaddr1       = ?,
            billaddr2       = ?,
            billcity        = ?,
            billstate       = ?,
            billzip         = ?,
            billcountry     = ?,
            courier         = ?,
            totalprice      = ?,
            billtofirstname = ?,
            billtolastname  = ?,
            shiptofirstname = ?,
            shiptolastname  = ?,
            creditcard      = ?,
            exprdate        = ?,
            cardtype        = ?,
            locale          = ?
        ",
    "delete-sql",
        "
        DELETE FROM orders
        WHERE orderid = ?
        "
))


