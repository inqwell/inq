/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Account primary key include file - mysql
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP KEY FIELDS IN THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            A.userid        AS \"Account\",
            A.email         AS \"Email\",
            A.firstname     AS \"FirstName\",
            A.lastname      AS \"LastName\",
            A.status        AS \"Status\",
            A.addr1         AS \"Addr1\",
            A.addr2         AS \"Addr2\",
            A.city          AS \"City\",
            A.state         AS \"State\",
            A.zip           AS \"ZIP\",
            A.country       AS \"Country\",
            A.phone         AS \"Phone\"
        FROM account A
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE A.userid = ?
        ",
    "write-sql",
        "
        REPLACE account
        SET
            userid        = ?,
            email         = ?,
            firstname     = ?,
            lastname      = ?,
            status        = ?,
            addr1         = ?,
            addr2         = ?,
            city          = ?,
            state         = ?,
            zip           = ?,
            country       = ?,
            phone         = ?
        ",
    "delete-sql",
        "
        DELETE FROM account
        WHERE userid = ?
        "
))

