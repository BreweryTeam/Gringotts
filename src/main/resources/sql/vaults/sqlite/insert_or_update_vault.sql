INSERT INTO vaults(
    owner, id, inventory, custom_name, trusted_players
) VALUES (?, ?, ?, ?, ?)
    ON CONFLICT(owner) DO UPDATE SET
    inventory = excluded.inventory,
    custom_name = excluded.custom_name,
    trusted_players = excluded.trusted_players;
);