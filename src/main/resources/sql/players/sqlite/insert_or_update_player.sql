INSERT INTO players(
    uuid, max_vaults, max_warehouse_stock
) VALUES (?, ?, ?)
    ON CONFLICT(uuid) DO UPDATE SET
    max_vaults = excluded.max_vaults,
    max_warehouse_stock = excluded.max_warehouse_stock;