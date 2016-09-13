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
        AND Parent = ?
        AND Value IS NOT NULL
        "
))
