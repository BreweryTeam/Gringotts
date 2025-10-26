INSERT INTO malts_players(
    uuid, max_vaults, max_warehouse_stock, warehouse_mode, quick_return_click_type
) VALUES (?, ?, ?, ?, ?)
    ON CONFLICT(uuid) DO UPDATE SET
    max_vaults = excluded.max_vaults,
    max_warehouse_stock = excluded.max_warehouse_stock,
    warehouse_mode = excluded.warehouse_mode,
    quick_return_click_type = excluded.quick_return_click_type;