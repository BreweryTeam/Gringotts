package dev.jsinco.gringotts.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.utility.FileUtil;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class DataSource {

    protected static final Path DATA_FOLDER = Gringotts.getInstance().getDataPath();

    private final HikariDataSource hikari;

    public abstract HikariConfig hikariConfig() throws IOException;
    public abstract void createTables();

    public abstract CompletableFuture<@Nullable Vault> getVault(UUID owner, int id);
    public abstract CompletableFuture<List<SnapshotVault>> getVaults(UUID owner);
    public abstract void saveVault(Vault vault);
    public abstract void deleteVault(UUID owner, int id);


    public abstract CompletableFuture<Map<Material, Integer>> getWarehouse(UUID owner);

    public abstract CompletableFuture<@Nullable GringottsPlayer> getGringottsPlayer(UUID uuid, Map<Material, Integer> warehouse);



    public CompletableFuture<@Nullable GringottsPlayer> getGringottsPlayer(UUID uuid) {
        return this.getWarehouse(uuid).thenCompose(warehouse -> this.getGringottsPlayer(uuid, warehouse));
    }


    public DataSource() {
        try {
            this.hikari = new HikariDataSource(hikariConfig());
            createTables();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    public Connection connection() throws SQLException {
        return hikari.getConnection();
    }

    public String[] getStatements(String path) {
        String[] statements = FileUtil.readInternalResource("/sql/" + path).split(";");
        // re-append the semicolon to each statement
        for (int i = 0; i < statements.length; i++) {
            statements[i] = statements[i].trim() + ";";
        }
        return statements;
    }

    public String getStatement(String path) {
        return FileUtil.readInternalResource("/sql/" + path);
    }

    @Nullable
    public Vault mapVault(ResultSet rs, UUID owner) throws SQLException {
        if (rs.next()) {
            return new Vault(
                    owner,
                    rs.getInt("id"),
                    rs.getString("inventory"),
                    rs.getString("custom_name"),
                    rs.getString("trusted_players")
            );
        }
        return null;
    }

    public List<SnapshotVault> mapSnapshotVaults(ResultSet rs, UUID owner) throws SQLException {
        List<SnapshotVault> vaults = new ArrayList<>();
        while (rs.next()) {
            vaults.add(
                    new SnapshotVault(
                            owner,
                            rs.getInt("id"),
                            rs.getString("custom_name")
                    )
            );
        }
        return vaults;
    }

    public GringottsPlayer mapGringottsPlayer(ResultSet rs, UUID uuid, Map<Material, Integer> warehouses) throws SQLException {
        if (rs.next()) {
            int maxVaults = rs.getInt("max_vaults");
            int maxWarehouses = rs.getInt("max_warehouses");
            int maxTotalWarehouse = rs.getInt("max_total_warehouse");
            return new GringottsPlayer(uuid, maxVaults, maxWarehouses, maxTotalWarehouse, warehouses);
        }
        return null;
    }
}
