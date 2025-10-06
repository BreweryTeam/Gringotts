INSERT INTO gringotts_vaults(
    owner, id, inventory, custom_name, icon, trusted_players
) VALUES (?, ?, ?, ?, ?, ?)
    ON CONFLICT(owner, id) DO UPDATE SET
    inventory = excluded.inventory,
    custom_name = excluded.custom_name,
    icon = excluded.icon,
    trusted_players = excluded.trusted_players;