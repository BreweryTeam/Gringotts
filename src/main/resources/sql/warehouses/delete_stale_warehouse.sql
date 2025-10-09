DELETE FROM gringotts_warehouses
WHERE owner = ?
  AND material NOT IN (?);