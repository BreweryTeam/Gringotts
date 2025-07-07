package dev.jsinco.gringotts.obj;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
public class GringottsPlayer {

    private final UUID uuid;
    private int maxVaults;
    private int maxWarehouses;
    private int maxTotalWarehouse;
    private final Map<Material, Integer> warehouse;

    public GringottsPlayer(UUID uuid, int maxVaults, int maxWarehouses, int maxTotalWarehouse, Map<Material, Integer> warehouse) {
        this.uuid = uuid;
        this.maxVaults = maxVaults;
        this.maxWarehouses = maxWarehouses;
        this.maxTotalWarehouse = maxTotalWarehouse;
        this.warehouse = warehouse;
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }


    public int getMaxVaults() {
        // TODO: Include configured default max.
        int maxByPermission = getMaxByPermission("gringotts.maxvaults");
        // Whichever is greater, use that
        return Math.max(maxByPermission, maxVaults);
    }

    public int getMaxWarehouses() {
        // TODO: Include configured default max.
        int maxByPermission = getMaxByPermission("gringotts.maxwarehouses");
        return Math.max(maxByPermission, maxWarehouses);
    }

    public int getMaxTotalWarehouse() {
        // TODO: Include configured default max.
        int maxByPermission = getMaxByPermission("gringotts.maxtotalwarehouse");
        return Math.max(maxByPermission, maxTotalWarehouse);
    }

    public boolean addWarehouseItem(Material material) {
        if (warehouse.containsKey(material)) {
            return  false; // Item already exists in warehouse
        }

        int maxWarehouses = getMaxWarehouses();
        if (warehouse.size() >= maxWarehouses) {
            return false; // Warehouse is full
        }

        warehouse.put(material, 0); // Initialize with 0 quantity
        return true; // Item added successfully
    }

    public int removeWarehouseItem(Material material) {
        if (!warehouse.containsKey(material)) {
            return -1; // Item does not exist in warehouse
        }

        int quantity = warehouse.get(material);
        warehouse.remove(material);
        return quantity; // Item removed successfully
    }

    public boolean updateWarehouseItemQuantity(Material material, int quantity) {
        if (!warehouse.containsKey(material)) {
            return false; // Item does not exist in warehouse
        }
        warehouse.put(material, quantity);
        return true; // Quantity updated successfully
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
}
