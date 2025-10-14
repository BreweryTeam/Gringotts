INSERT INTO malts_warehouses (owner, material, quantity, last_update)
VALUES (?, ?, ?, ?)
    ON CONFLICT(owner, material)
DO UPDATE SET
    quantity = excluded.quantity,
    last_update = excluded.last_update;

