INSERT INTO gringotts_vaults(
    owner, id, inventory, custom_name, icon, trusted_players
) VALUES (?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
   inventory = VALUES(inventory),
   custom_name = VALUES(custom_name),
   icon = VALUES(icon),
   trusted_players = VALUES(trusted_players);