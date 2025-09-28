CREATE TABLE IF NOT EXISTS vaults(
    owner VARCHAR(36) NOT NULL,
    id INTEGER NOT NULL,
    inventory TEXT NOT NULL,
    custom_name TEXT,
    icon TEXT,
    trusted_players TEXT,
    PRIMARY KEY (owner, id)
);

CREATE TABLE IF NOT EXISTS players(
    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
    max_vaults INTEGER NOT NULL,
    max_warehouse_stock INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouses(
    owner VARCHAR(36) NOT NULL,
    material VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL,
    last_update BIGINT NOT NULL,
    PRIMARY KEY (owner, material),
    FOREIGN KEY (owner) REFERENCES players(uuid)
);