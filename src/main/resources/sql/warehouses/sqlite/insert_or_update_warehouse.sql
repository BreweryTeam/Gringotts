INSERT INTO gringotts_warehouses (owner, material, quantity, last_update)
VALUES (?, ?, ?, ?)
    ON CONFLICT(owner, material)
DO UPDATE SET
    quantity = excluded.quantity,
    last_update = excluded.last_update;

-- Purge items no longer in map.
DELETE FROM warehouses
WHERE owner = ?
  AND material NOT IN (?, ?, ?, ...);


