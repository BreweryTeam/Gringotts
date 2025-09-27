package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.storage.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.regex.Pattern;

@Getter
public class GringottsPlayer implements CachedObject {

    private final long cacheTime = System.currentTimeMillis();
    @Setter
    private Long expire;

    private final UUID uuid;
    private int maxVaults;
    private int maxWarehouseStock;


    public GringottsPlayer(UUID uuid) {
        this.uuid = uuid;
        // TODO: Config
        this.maxVaults = 5;
        this.maxWarehouseStock = 100;
    }

    public GringottsPlayer(UUID uuid, int maxVaults, int maxWarehouseStock) {
        this.uuid = uuid;
        this.maxVaults = maxVaults;
        this.maxWarehouseStock = maxWarehouseStock;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }


    public int getCalculatedMaxVaults() {
        // TODO: Include configured default max.
        int maxByPermission = getMaxByPermission("gringotts.maxvaults");
        // Whichever is greater, use that
        return Math.max(maxByPermission, maxVaults);
    }

    public int getCalculatedMaxWarehouseStock() {
        // TODO: Include configured default max.
        int maxByPermission = getMaxByPermission("gringotts.maxstock");
        return Math.max(maxByPermission, maxWarehouseStock);
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
    public void save(DataSource dataSource) {
        dataSource.saveGringottsPlayer(this);
    }
}
