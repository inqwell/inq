/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Supplier primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            S.suppid        AS \"Supplier\",
            S.name          AS \"Name\",
            S.status        AS \"SupplierStatus\",
            S.addr1         AS \"Addr1\",
            S.addr2         AS \"Addr2\",
            S.city          AS \"City\",
            S.state         AS \"State\",
            S.zip           AS \"ZIP\",
            S.phone         AS \"Phone\"
        FROM supplier S
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE S.suppid = ?
        ",
    "write-sql",
        "
        REPLACE supplier
        SET
            suppid        = ?,
            name          = ?,
            status        = ?,
            addr1         = ?,
            addr2         = ?,
            city          = ?,
            state         = ?,
            zip           = ?,
            phone         = ?
        ",
    "delete-sql",
        "
        DELETE FROM supplier
        WHERE suppid = ?
        "
))

