package dev.jsinco.malts.obj;

import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.enums.WarehouseMode;
import dev.jsinco.malts.storage.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Getter
@Setter
public class MaltsPlayer implements CachedObject {

    private static final Config cfg = ConfigManager.get(Config.class);


    private Long expire;

    private final UUID uuid;
    private int maxVaults;
    private int maxWarehouseStock;
    private WarehouseMode warehouseMode;


    public MaltsPlayer(UUID uuid) {
        this.uuid = uuid;
        this.maxVaults = 0;
        this.maxWarehouseStock = 0;
        this.warehouseMode = WarehouseMode.NONE;
    }

    public MaltsPlayer(@NotNull UUID uuid, int maxVaults, int maxWarehouseStock, WarehouseMode warehouseMode) {
        this.uuid = uuid;
        this.maxVaults = maxVaults;
        this.maxWarehouseStock = maxWarehouseStock;
        this.warehouseMode = warehouseMode == null ? WarehouseMode.NONE : warehouseMode;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer offlinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public String name() {
        return offlinePlayer().getName();
    }


    public int getCalculatedMaxVaults() {
        int maxByPermission = getMaxByPermission("malts.maxvaults");

        return Math.max(maxByPermission + maxVaults, cfg.vaults().defaultMaxVaults());
    }

    public int getCalculatedMaxWarehouseStock() {
        int maxByPermission = getMaxByPermission("malts.maxstock");

        return Math.max(maxByPermission + maxWarehouseStock, cfg.warehouse().defaultMaxStock());
    }

    private int getMaxByPermission(String permissionPrefix) {
        Player player = getPlayer();
        if (player == null) {
            return 0; // Player is offline or not found
        }
        Pattern maxPermPattern = Pattern.compile(permissionPrefix + "\\.(\\d+)");
        return player.getEffectivePermissions().stream()
                .map(permission -> {
                    var matcher = maxPermPattern.matcher(permission.getPermission());
                    if (matcher.matches()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                    return 0;
                })
                .max(Integer::compareTo)
                .orElse(0);
    }


    @Override
    public CompletableFuture<Void> save(DataSource dataSource) {
        return dataSource.saveMaltsPlayer(this);
    }

    @Override
    public String toString() {
        return "MaltsPlayer{" +
                "uuid=" + uuid +
                ", expire=" + expire +
                ", maxVaults=" + maxVaults +
                ", maxWarehouseStock=" + maxWarehouseStock +
                '}';
    }
}
