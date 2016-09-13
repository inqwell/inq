/**
 * Private & Confidential Copyright © Xylinq Ltd 2004.
 * All rights reserved.
 */

auxcfg(map(
    "prepared", true,
    "read-sql",
        "
        {select-stmt}
        WHERE Name = ?
        AND ThisElement = ?
        AND ((? IS NULL AND Parent IS NULL) OR (? IS NOT NULL AND Parent = ?))
        ",
    "read-order",
        array a = (
            "Name",
            "ThisElement",
            "Parent", "Parent", "Parent"
        )
))
