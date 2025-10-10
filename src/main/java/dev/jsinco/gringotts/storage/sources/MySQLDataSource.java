package dev.jsinco.gringotts.storage.sources;

import com.zaxxer.hikari.HikariConfig;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Stock;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLDataSource extends DataSource {

    public MySQLDataSource(Config.Storage config) {
        super(config);
    }

    @Override
    public HikariConfig hikariConfig(Config.Storage config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("GringottsMySQL");
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database() + config.jdbcFlags());
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(10);
        return hikariConfig;
    }

    @Override
    public CompletableFuture<Void> createTables() {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                for (String statement : this.getStatements("tables/mysql/create_tables.sql")) {
                    connection.prepareStatement(statement).execute();
                }
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Vault> getVault(UUID owner, int id) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/select_vault.sql")
                );
                statement.setString(1, owner.toString());
                statement.setInt(2, id);
                ResultSet resultSet = statement.executeQuery();
                return this.mapVault(resultSet, owner, id);
            }
        });
    }

    @Override
    public CompletableFuture<List<SnapshotVault>> getVaults(UUID owner) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/select_owned_vaults.sql")
                );
                statement.setString(1, owner.toString());
                ResultSet resultSet = statement.executeQuery();
                return this.mapSnapshotVaults(resultSet, owner);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveVault(Vault vault) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/mysql/insert_or_update_vault.sql")
                );
                statement.setString(1, vault.getOwner().toString());
                statement.setInt(2, vault.getId());
                statement.setString(3, vault.encodeInventory());
                statement.setString(4, vault.getCustomName());
                statement.setString(5, vault.getIcon().toString());
                statement.setString(6, vault.encodeTrusted());
                statement.executeUpdate();
                Text.debug("Saved vault: " + vault.getOwner() + " #" + vault.getId());
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteVault(UUID owner, int id) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/delete_vault.sql")
                );
                statement.setString(1, owner.toString());
                statement.setInt(2, id);
                int rowsAffected = statement.executeUpdate();


                Text.debug("Attempted to delete vault: " + owner + " #" + id);
                return rowsAffected > 0;
            }
        });
    }

    @Override
    public CompletableFuture<@NotNull Warehouse> getWarehouse(UUID owner) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {

                PreparedStatement warehouseStatement = connection.prepareStatement(
                        this.getStatement("warehouses/select_warehouse.sql")
                );

                warehouseStatement.setString(1, owner.toString());

                ResultSet resultSet = warehouseStatement.executeQuery();
                return this.mapWarehouse(resultSet, owner);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveWarehouse(Warehouse warehouse) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                UUID owner = warehouse.getOwner();
                Map<Material, Stock> map = warehouse.stock();

                // Execute INSERT/UPDATE
                try (PreparedStatement ps = connection.prepareStatement(
                        this.getStatement("warehouses/mysql/insert_or_update_warehouse.sql")
                )) {
                    for (Map.Entry<Material, Stock> entry : map.entrySet()) {
                        ps.setString(1, owner.toString());
                        ps.setString(2, entry.getKey().name());
                        ps.setInt(3, entry.getValue().getAmount());
                        ps.setLong(4, entry.getValue().getLastUpdate());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Execute DELETE for removed items
                if (map.isEmpty()) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            this.getStatement("warehouses/delete_all_warehouse.sql")
                    )) {
                        ps.setString(1, owner.toString());
                        ps.executeUpdate();
                    }
                } else {
                    String placeholders = String.join(", ", Collections.nCopies(map.size(), "?"));
                    String deleteSql = this.getStatement("warehouses/delete_stale_warehouse.sql")
                            .replace("(?)", "(" + placeholders + ")");

                    try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
                        ps.setString(1, owner.toString());
                        int i = 2;
                        for (Material m : map.keySet()) {
                            ps.setString(i++, m.name());
                        }
                        ps.executeUpdate();
                    }
                }
            }
            Text.debug("Saved warehouse: " + warehouse.getOwner());
            return null;
        });
    }

    @Override
    public CompletableFuture<@NotNull GringottsPlayer> getGringottsPlayer(UUID uuid) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("players/select_player.sql")
                );
                statement.setString(1, uuid.toString());
                ResultSet resultSet = statement.executeQuery();
                return this.mapGringottsPlayer(resultSet, uuid);
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveGringottsPlayer(GringottsPlayer gringottsPlayer) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("players/mysql/insert_or_update_player.sql")
                );
                statement.setString(1, gringottsPlayer.getUuid().toString());
                statement.setInt(2, gringottsPlayer.getMaxVaults());
                statement.setInt(3, gringottsPlayer.getMaxWarehouseStock());
                statement.setString(4, gringottsPlayer.getWarehouseMode().name());
                statement.executeUpdate();
                Text.debug("Saved Gringotts player: " + gringottsPlayer.getUuid());
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Integer> getTotalVaultCount() {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/total_vault_count.sql")
                );
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        });
    }

    @Override
    public CompletableFuture<Integer> getTotalWarehouseStockCount() {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("warehouses/total_warehouse_stock.sql")
                );
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        });
    }
}
