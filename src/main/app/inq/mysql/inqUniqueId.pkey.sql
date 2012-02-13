/*
 * Xylinq pkey file for inqUniqueId.
 */

auxcfg(map(
    // Use prepared stmt so that we can use '?' notation for column values
    // instead of having to define all the formatting.
    // NOTE: MAKE SURE WE KEEP TO THE SAME ORDER AS DEFINED IN THE TYPEDEF AND KEY FIELDS.
    "prepared", true,
    "select-stmt",
        "
        SELECT
            XY.Name     AS \"Name\",
            XY.Value    AS \"Value\",
            XY.LastUsed AS \"LastUsed\"
        FROM inqUniqueId XY
        ",
    "read-sql",
        "
        {select-stmt}
        WHERE Name = ?
        ",
    "write-sql",
        "
        REPLACE inqUniqueId
        SET
            Name     = ?,
            Value    = ?,
            LastUsed = ?
        ",
    "delete-sql",
        "
        DELETE FROM inqUniqueId
        WHERE Name = ?
        "
))