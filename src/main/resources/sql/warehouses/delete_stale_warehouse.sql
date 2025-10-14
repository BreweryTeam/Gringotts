DELETE FROM malts_warehouses
WHERE owner = ?
  AND material NOT IN (?);