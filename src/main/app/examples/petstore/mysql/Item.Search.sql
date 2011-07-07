/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Item Search key SQL definition
 */

auxcfg(map(
    "prepared", true,
    "read-sql",
        "
        {select-stmt},
            product P

        -- joins
        WHERE I.productid = P.productid

        -- values
        AND (    (? = ''Y'' AND UPPER(I.itemid) LIKE UPPER(CONCAT(?, ''%'')))
              OR (? = ''Y'' AND UPPER(I.attr1)  LIKE UPPER(CONCAT(''%'', ?, ''%'')))
              OR (? = ''Y'' AND UPPER(P.name)   LIKE UPPER(CONCAT(?, ''%'')))
              OR (? = ''Y'' AND UPPER(P.descn)  LIKE UPPER(CONCAT(''%'', ?, ''%'')))
            )
        ",
    "read-order",
        array a = (
            "ItemActive",       "SearchValue",
            "Attr1Active",      "SearchValue",
            "NameActive",       "SearchValue",
            "DescrActive",      "SearchValue"
        )
))
