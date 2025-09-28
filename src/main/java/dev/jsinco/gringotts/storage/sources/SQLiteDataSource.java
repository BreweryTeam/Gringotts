package dev.jsinco.gringotts.storage.sources;

import com.zaxxer.hikari.HikariConfig;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SQLiteDataSource extends DataSource {

    @Override
    public HikariConfig hikariConfig() throws IOException {
        File file = DATA_FOLDER.resolve("gringotts.db").toFile();
        if (!file.exists() && !file.getParentFile().mkdirs() && !file.createNewFile()) {
            throw new IOException("Could not create file or dirs");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("GringottsSQLite");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + file);
        hikariConfig.setMaximumPoolSize(10);
        return hikariConfig;
    }

    @Override
    public void createTables() {
        Executors.runAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                for (String statement : this.getStatements("tables/sqlite/create_tables.sql")) {
                    connection.prepareStatement(statement).execute();
                }
            }
        });
    }

    @Override
    public @Nullable CompletableFuture<Vault> getVault(UUID owner, int id) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/select_vault.sql")
                );
                statement.setString(1, owner.toString());
                statement.setInt(2, id);
                ResultSet resultSet = statement.executeQuery();
                return this.mapVault(resultSet, owner, id);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
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
    public void saveVault(Vault vault) {
        Executors.runAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/sqlite/insert_or_update_vault.sql")
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
        });
    }

    @Override
    public void deleteVault(UUID owner, int id) {
        Executors.runAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("vaults/delete_vault.sql")
                );
                statement.setString(1, owner.toString());
                statement.setInt(2, id);
                statement.executeUpdate();
                Text.debug("Deleted vault: " + owner + " #" + id);
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

    // TODO: Redo this method
    @Override
    public void saveWarehouse(Warehouse warehouse) {
        Executors.runAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                UUID owner = warehouse.getOwner();
                Map<Material, Stock> map = warehouse.stock();

                for (String sql : this.getStatements("warehouses/sqlite/insert_or_update_warehouse.sql")) {
                    if (sql.trim().startsWith("INSERT")) {
                        try (PreparedStatement ps = connection.prepareStatement(sql)) {
                            for (Map.Entry<Material, Stock> entry : map.entrySet()) {
                                ps.setString(1, owner.toString());
                                ps.setString(2, entry.getKey().name());
                                ps.setInt(3, entry.getValue().getAmount());
                                ps.setLong(4, entry.getValue().getLastUpdate());
                                ps.addBatch();
                            }
                            ps.executeBatch();
                        }
                    }
                    else if (sql.trim().startsWith("DELETE")) {
                        if (!map.isEmpty()) {
                            // Build placeholders dynamically
                            String placeholders = map.keySet().stream()
                                    .map(m -> "?")
                                    .collect(Collectors.joining(", "));

                            String purgeSql = sql.replace("(?, ?, ?, ...)", "(" + placeholders + ")");

                            try (PreparedStatement ps = connection.prepareStatement(purgeSql)) {
                                ps.setString(1, owner.toString());
                                int i = 2;
                                for (Material m : map.keySet()) {
                                    ps.setString(i++, m.name());
                                }
                                ps.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement ps = connection.prepareStatement(
                                    "DELETE FROM warehouses WHERE owner = ?")) {
                                ps.setString(1, owner.toString());
                                ps.executeUpdate();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public CompletableFuture<GringottsPlayer> getGringottsPlayer(UUID uuid) {
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
    public void saveGringottsPlayer(GringottsPlayer gringottsPlayer) {
        Executors.runAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("players/sqlite/insert_or_update_player.sql")
                );
                statement.setString(1, gringottsPlayer.getUuid().toString());
                statement.setInt(2, gringottsPlayer.getMaxVaults());
                statement.setInt(3, gringottsPlayer.getMaxWarehouseStock());
                statement.executeUpdate();
                Text.debug("Saved gringotts player: " + gringottsPlayer.getUuid());
            }
        });
    }
}