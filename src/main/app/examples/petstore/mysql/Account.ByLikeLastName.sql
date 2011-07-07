/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Account.ByLikeLastName.sql include file - mysql
 */
    auxcfg(map(
        "prepared", true,
        "read-sql", // An example of using SQL-specific functions, hence this SQL lives in an included file
            " 
            {select-stmt}
            WHERE UPPER(LastName) LIKE CONCAT(?, ''%'')
            "
        ))

