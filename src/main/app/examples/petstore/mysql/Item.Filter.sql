/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/**
 * Item Filter include file - mysql
 */

auxcfg(map(
    "prepared", true,
    "read-sql",
        "
        {select-stmt},
            category C,
            product P,
            supplier S

        -- the joins
        WHERE I.productid = P.productid
        AND   P.category  = C.catid     -- fk fields really should have the same names
        AND   I.supplier  = S.suppid    -- ditto

        -- the values
        AND (? IS NULL OR C.catid = ?)
        AND (? IS NULL OR UPPER(P.name) LIKE UPPER(CONCAT(''%'', ?, ''%'')))
        AND (? IS NULL OR I.listprice >= ?)
        AND (? IS NULL OR I.listprice <= ?)
        AND (? IS NULL OR S.name = ?)
        AND (? IS NULL OR I.status = ?)
        ",
    // We need to tell Inq what key fields correspond to the positional
    // prepared statement arguments. They appear more than once so we
    // cannot rely on Inq's default behaviour of setting the values once
    // for each key field and in the order of their declaration.
    // We have laid them out in two columns just because that is the
    // way they appear in the where clause above.
    "read-order",
        array a = (
            "Category",       "Category",
            "Name",           "Name",
            "MinPrice",       "MinPrice",
            "MaxPrice",       "MaxPrice",
            "Supplier",       "Supplier",
            "Status",         "Status"
        )
))


