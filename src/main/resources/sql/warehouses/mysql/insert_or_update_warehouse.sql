INSERT INTO malts_warehouses (owner, material, quantity, last_update)
VALUES (?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE
    quantity = VALUES(quantity),
    last_update = VALUES(last_update);