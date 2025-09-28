package dev.jsinco.gringotts.storage;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.enums.Driver;
import dev.jsinco.gringotts.obj.CachedObject;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Stock;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.utility.FileUtil;
import dev.jsinco.gringotts.utility.Text;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public abstract class DataSource {

    public static final Path DATA_FOLDER = Gringotts.getInstance().getDataPath();

    @Getter
    private static DataSource instance;

    @Getter
    private final HikariDataSource hikari;

    private final ConcurrentLinkedQueue<CachedObject> cachedObjects = new ConcurrentLinkedQueue<>();

    public abstract HikariConfig hikariConfig() throws IOException;
    public abstract void createTables();

    public abstract CompletableFuture<Vault> getVault(UUID owner, int id);
    public abstract CompletableFuture<List<SnapshotVault>> getVaults(UUID owner);
    public abstract void saveVault(Vault vault);
    public abstract void deleteVault(UUID owner, int id);


    public abstract CompletableFuture<@NotNull Warehouse> getWarehouse(UUID owner);
    public abstract void saveWarehouse(Warehouse warehouse);

    public abstract CompletableFuture<@NotNull GringottsPlayer> getGringottsPlayer(UUID uuid);
    public abstract void saveGringottsPlayer(GringottsPlayer gringottsPlayer);




    public DataSource() {
        try {
            this.hikari = new HikariDataSource(hikariConfig());
            createTables();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }

        Bukkit.getAsyncScheduler().runAtFixedRate(Gringotts.getInstance(),task -> {
            System.out.println(cachedObjects);
            for (CachedObject cachedObject : cachedObjects) {
                Long expire = cachedObject.getExpire();
                if (expire != null && expire < System.currentTimeMillis()) {
                    cachedObject.save(this);
                    cachedObjects.remove(cachedObject);
                    Text.debug("Uncached " + cachedObject.getClass().getSimpleName() + ": " + cachedObject.getUuid());
                }
            }
        },0, 1, TimeUnit.MINUTES);
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

    public void close() {
        for (CachedObject cachedObject : cachedObjects) {
            cachedObject.save(this);
        }
        cachedObjects.clear();
        hikari.close();
    }

    @Nullable
    public Vault mapVault(ResultSet rs, UUID owner, int id) throws SQLException {
        if (rs.next()) {
            return new Vault(
                    owner,
                    id,
                    rs.getString("inventory"),
                    rs.getString("custom_name"),
                    Material.getMaterial(rs.getString("icon")),
                    rs.getString("trusted_players")
            );
        }
        return new Vault(owner, id);
    }

    public List<SnapshotVault> mapSnapshotVaults(ResultSet rs, UUID owner) throws SQLException {
        List<SnapshotVault> vaults = new ArrayList<>();
        while (rs.next()) {
            vaults.add(
                    new SnapshotVault(
                            owner,
                            rs.getInt("id"),
                            rs.getString("custom_name"),
                            Material.getMaterial(rs.getString("icon"))
                    )
            );
        }
        return vaults;
    }

    public GringottsPlayer mapGringottsPlayer(ResultSet rs, UUID uuid) throws SQLException {
        if (rs.next()) {
            int maxVaults = rs.getInt("max_vaults");
            int maxWarehouseStock = rs.getInt("max_warehouse_stock");
            return new GringottsPlayer(uuid, maxVaults, maxWarehouseStock);
        }
        return new GringottsPlayer(uuid);
    }

    public Warehouse mapWarehouse(ResultSet rs, UUID uuid) throws SQLException {
        Map<Material, Stock> warehouseMap = new HashMap<>();

        while (rs.next()) {
            String mstring = rs.getString("material");

            Material material = Material.matchMaterial(mstring);
            int quantity = rs.getInt("quantity");
            long lastUpdate = rs.getLong("last_update");

            if (material == null) {
                throw new RuntimeException("Material " + mstring + " does not exist");
            }
            warehouseMap.put(material, new Stock(material, quantity, lastUpdate));
        }

        // TODO: Figure out a way to have default stock
//        if (warehouseMap.isEmpty()) {
//            warehouseMap.put(Material.APPLE, 0);
//            warehouseMap.put(Material.COAL, 0);
//            warehouseMap.put(Material.DIAMOND, 0);
//        }
        return new Warehouse(uuid, warehouseMap);
    }




    public static void createInstance() {
        if (instance != null) {
            throw new IllegalStateException("DataSourceManager instance already created.");
        }
        Driver driver = Driver.SQLITE;
        instance = driver.getSupplier().get();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends CachedObject> T cachedObject(UUID uuid, Class<T> objectClass) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(objectClass, "objectClass cannot be null");


        return (T) cachedObjects.stream().filter(it ->
            it.getClass().equals(objectClass) && it.getUuid().equals(uuid)
        ).findFirst().orElse(null);
    }

    public void cacheObject(CompletableFuture<? extends CachedObject> future, long expire) {
        future.thenAccept(cachedObject -> {
            Text.debug("Caching " + cachedObject.getClass().getSimpleName() + ": " + cachedObject.getUuid() + " until " + expire);
            cachedObject.setExpire(System.currentTimeMillis() + expire);
            cachedObjects.add(cachedObject);
        });
    }

    public void cacheObject(CompletableFuture<? extends CachedObject> future) {
        future.thenAccept(cachedObject -> {
            Text.debug("Caching " + cachedObject.getClass().getSimpleName() + ": " + cachedObject.getUuid());
            cachedObjects.add(cachedObject);
        });
    }

    public void uncacheObject(UUID uuid, Class<? extends CachedObject> objectClass) {
        CachedObject cachedObject = cachedObject(uuid, objectClass);
        if (cachedObject != null) {
            Text.debug("Uncaching " + cachedObject.getClass().getSimpleName() + ": " + cachedObject.getUuid());
            cachedObject.save(this);
            cachedObjects.remove(cachedObject);
        }
    }

    // TODO: Remove
    public Warehouse cachedWarehouse(UUID uuid) {
        return cachedObject(uuid, Warehouse.class);
    }

    public GringottsPlayer cachedGringottsPlayer(UUID uuid) {
        return cachedObject(uuid, GringottsPlayer.class);
    }
}
