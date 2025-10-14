INSERT INTO malts_players(
    uuid, max_vaults, max_warehouse_stock, warehouse_mode
) VALUES (?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
   max_vaults = VALUES(max_vaults),
   max_warehouse_stock = VALUES(max_warehouse_stock),
   warehouse_mode = VALUES(warehouse_mode);