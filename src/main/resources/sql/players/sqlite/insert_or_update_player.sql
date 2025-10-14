INSERT INTO malts_players(
    uuid, max_vaults, max_warehouse_stock, warehouse_mode
) VALUES (?, ?, ?, ?)
    ON CONFLICT(uuid) DO UPDATE SET
    max_vaults = excluded.max_vaults,
    max_warehouse_stock = excluded.max_warehouse_stock,
    warehouse_mode = excluded.warehouse_mode;