SELECT material, quantity, last_update
FROM gringotts_warehouses
WHERE owner = ?;