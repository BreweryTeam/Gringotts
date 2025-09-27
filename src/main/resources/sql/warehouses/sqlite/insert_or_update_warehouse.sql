INSERT INTO warehouses (owner, material, quantity)
VALUES (?, ?, ?)
    ON CONFLICT(owner, material)
DO UPDATE SET quantity = excluded.quantity;

-- Purge items no longer in map.
DELETE FROM warehouses
WHERE owner = ?
  AND material NOT IN (?, ?, ?, ...);


