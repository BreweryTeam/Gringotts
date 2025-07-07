CREATE TABLE IF NOT EXISTS vaults(
    owner VARCHAR(36) NOT NULL PRIMARY KEY,
    id INTEGER NOT NULL,
    inventory TEXT NOT NULL,
    custom_name TEXT,
    trusted_players TEXT
);

CREATE TABLE IF NOT EXISTS players(
    uuid VARCHAR(36) NOT NULL PRIMARY KEY,
    max_vaults INTEGER NOT NULL,
    max_warehouses INTEGER NOT NULL,
    max_total_warehouse INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouses(
    owner VARCHAR(36) NOT NULL,
    material VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL,
    PRIMARY KEY (owner, material),
    FOREIGN KEY (owner) REFERENCES players(uuid)
);