package dev.jsinco.gringotts.storage.sources;

import com.zaxxer.hikari.HikariConfig;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MySQLDataSource extends DataSource {
    @Override
    public HikariConfig hikariConfig() throws IOException {
        return null;
    }

    @Override
    public CompletableFuture<Void> createTables() {
        return null;
    }

    @Override
    public CompletableFuture<Vault> getVault(UUID owner, int id) {
        return null;
    }

    @Override
    public CompletableFuture<List<SnapshotVault>> getVaults(UUID owner) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveVault(Vault vault) {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteVault(UUID owner, int id) {
        return null;
    }

    @Override
    public CompletableFuture<@NotNull Warehouse> getWarehouse(UUID owner) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveWarehouse(Warehouse warehouse) {
        return null;
    }

    @Override
    public CompletableFuture<@NotNull GringottsPlayer> getGringottsPlayer(UUID uuid) {
        return null;
    }

    @Override
    public CompletableFuture<Void> saveGringottsPlayer(GringottsPlayer gringottsPlayer) {
        return null;
    }
}
