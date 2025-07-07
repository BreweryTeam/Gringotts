package dev.jsinco.gringotts.storage.sources;

import com.zaxxer.hikari.HikariConfig;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
                return this.mapVault(resultSet, owner);
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
                statement.setString(5, vault.encodeTrusted());
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
    public CompletableFuture<Map<Material, Integer>> getWarehouse(UUID owner) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                Map<Material, Integer> warehouse = new HashMap<>();
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("warehouses/select_warehouse.sql")
                );
                statement.setString(1, owner.toString());
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Material material = Material.matchMaterial(resultSet.getString("material"));
                    int quantity = resultSet.getInt("quantity");
                    warehouse.put(material, quantity);
                }
                return warehouse;
            }
        });
    }

    @Override
    public CompletableFuture<GringottsPlayer> getGringottsPlayer(UUID uuid, Map<Material, Integer> warehouse) {
        return Executors.supplyAsyncWithSQLException(() -> {
            try (Connection connection = this.connection()) {
                PreparedStatement statement = connection.prepareStatement(
                        this.getStatement("players/select_player.sql")
                );
                statement.setString(1, uuid.toString());
                ResultSet resultSet = statement.executeQuery();

                return this.mapGringottsPlayer(resultSet, uuid, warehouse);
            }
        });
    }
}