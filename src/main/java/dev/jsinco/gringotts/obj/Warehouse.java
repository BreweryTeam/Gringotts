package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Warehouse implements CachedObject {

    private static final GuiConfig.WarehouseGui.WarehouseItem guiCfg = ConfigManager.get(GuiConfig.class).warehouseGui().warehouseItem();
    private static final Config.Warehouse cfg = ConfigManager.get(Config.class).warehouse();
    private static final Lang lng = ConfigManager.get(Lang.class);

    @Getter @Setter
    private Long expire;

    @Getter
    private final UUID owner;
    // TODO: Can be EnumMap
    private final Map<Material, Stock> warehouseMap; // Mapped to Material for faster lookup

    public Warehouse(UUID owner, Map<Material, Stock> warehouseMap) {
        this.owner = owner;
        this.warehouseMap = warehouseMap;
    }
    public Warehouse(UUID owner) {
        this.owner = owner;
        this.warehouseMap = new HashMap<>();
    }


    /**
     * Stocks a specified amount of a given material in the warehouse, ensuring that
     * the total stock quantity does not exceed the maximum warehouse stock capacity.
     * If the material is already stocked, the amount is increased. Otherwise, a new stock entry
     * is created for the material.
     *
     * @param material the material to be stocked. Must be a valid item.
     * @param amt the amount of the material to be stocked. Adjusted if it exceeds the remaining capacity.
     * @return the actual amount of the material that was stocked.
     * @throws IllegalArgumentException if the provided material is not a valid item.
     */
    public int stockItem(Material material, int amt) {
        GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(owner);
        int currentStockQuantity = currentStockQuantity();
        int maxWarehouseStock = gringottsPlayer.getCalculatedMaxWarehouseStock();

        if (!material.isItem()) {
            throw new IllegalArgumentException("Material must be an item");
        } else if (currentStockQuantity + amt > maxWarehouseStock) {
            amt = maxWarehouseStock - currentStockQuantity;
        }

        Stock stock = warehouseMap.get(material);
        if (stock != null && amt > 0) {
            stock.increase(amt);
        } else {
            warehouseMap.put(material, new Stock(material, amt));
        }
        return amt;
    }

    @Nullable
    public ItemStack destockItem(Material material, int amt) {
        Stock stock = warehouseMap.get(material);
        if (stock == null || stock.getAmount() < 1) {
            return null;
        } else if (stock.getAmount() < amt) {
            amt = stock.getAmount();
        }

        stock.decrease(amt);
        return ItemStack.of(material, amt);
    }

    public boolean canStock(Material material) {
        return !((cfg.blacklistSingleStackMaterials() && material.getMaxStackSize() == 1) || cfg.blacklistedMaterials().contains(material));
    }

    public boolean hasItem(Material material) {
        return warehouseMap.containsKey(material);
    }

    public int getQuantity(Material material) {
        Stock stock = warehouseMap.get(material);
        return stock != null ? stock.getAmount() : 0;
    }

    public Map<Material, Integer> stockCopy() {
        Map<Material, Integer> newMap = new HashMap<>();
        warehouseMap.forEach((material, stock) -> newMap.put(material, stock.getAmount()));
        return Map.copyOf(newMap);
    }

    public Map<Material, Stock> stock() {
        return Map.copyOf(warehouseMap);
    }

    public int currentStockQuantity() {
        return warehouseMap.values().stream().mapToInt(Stock::getAmount).sum();
    }

    public boolean removeItem(Material material) {
        Stock stock = warehouseMap.get(material);
        if (stock == null || stock.getAmount() == 0) {
            warehouseMap.remove(material);
            return true;
        }
        return false;
    }

    public boolean hasCompartment(Material material) {
        return warehouseMap.containsKey(material);
    }

    public boolean hasCompartment(ItemStack itemStack) {
        if (itemStack.hasItemMeta()) {
            return false;
        }
        return warehouseMap.containsKey(itemStack.getType());
    }


    @Override
    public UUID getUuid() {
        return owner;
    }


    @Override
    public CompletableFuture<Void> save(DataSource dataSource) {
        return dataSource.saveWarehouse(this);
    }


    public List<GuiItem> stockAsGuiItems(int truncate) {
        List<GuiItem> items = new ArrayList<>();
        int i = 0;
        List<Stock> sortedStocks = warehouseMap.values().stream()
                .sorted(Comparator.comparingLong(Stock::getLastUpdate).reversed())
                .toList();
        for (Stock stock : sortedStocks) {
            Material material = stock.getMaterial();
            @SuppressWarnings("unchecked")
            GuiItem guiItem = GuiItem.builder()
                    .itemStack(b -> b
                            .stringReplacements(
                                    Couple.of("{quantity}", String.valueOf(stock.getAmount())),
                                    Couple.of("{material}", Util.formatEnumerator(material.toString()))
                            )
                            .material(material)
                            .displayName(guiCfg.name())
                            .lore(guiCfg.lore())
                    )
                    .action(e -> {
                        if (e.isCancelled()) {
                            return;
                        }
                        Player player = (Player) e.getWhoClicked();
                        PlayerInventory inv = player.getInventory();
                        ItemStack clickedItem = e.getCurrentItem();
                        // TODO: Material overflow out of inventory
                        switch (e.getClick()) {
                            case LEFT -> {
                                ItemStack item = destockItem(material, 1);
                                if (item == null) {
                                    lng.entry(l -> l.warehouse().notEnoughMaterial(), player,
                                            Couple.of("{material}", Util.formatEnumerator(material.toString()))
                                    );
                                } else {
                                    inv.addItem(item);
                                }
                            }
                            case RIGHT -> {
                                ItemStack item = destockItem(material, 64);
                                if (item == null) {
                                    lng.entry(l -> l.warehouse().notEnoughMaterial(), player,
                                            Couple.of("{material}", Util.formatEnumerator(material.toString()))
                                    );
                                } else {
                                    inv.addItem(item);
                                }
                            }
                            case SHIFT_LEFT -> {
                                int invAmt = Util.getAmountInvCanHold(inv, material);
                                if (invAmt == 0) {
                                    lng.entry(l -> l.warehouse().inventoryFull(), player);
                                    return;
                                }

                                ItemStack item = destockItem(material, invAmt);
                                if (item != null) {
                                    inv.addItem(item);
                                } else {
                                    lng.entry(l -> l.warehouse().notEnoughMaterial(), player,
                                            Couple.of("{material}", Util.formatEnumerator(material.toString()))
                                    );
                                }
                            }
                            case SHIFT_RIGHT -> {
                                int invAmt = Util.getMaterialAmount(inv, material);
                                if (invAmt == 0) {
                                    lng.entry(l -> l.warehouse().notEnoughMaterial(), player,
                                            Couple.of("{material}", Util.formatEnumerator(material.toString()))
                                    );
                                    return;
                                }

                                if (!canStock(material)) {
                                    lng.entry(l -> l.warehouse().blacklistedItem(), player, Couple.of("{material}", Util.formatEnumerator(material)));
                                    return;
                                }

                                int diff = stockItem(material, invAmt);
                                if (diff > 0) {
                                    inv.removeItem(new ItemStack(material, diff));
                                } else {
                                    lng.entry(l -> l.warehouse().notEnoughStock(), player);
                                }
                            }
                        }

                        Util.editMeta(clickedItem, meta ->
                                meta.lore(Text.mmlNoItalic(Util.replaceAll(guiCfg.lore(), "{quantity}", String.valueOf(stock.getAmount())), NamedTextColor.WHITE))
                        );
                    }).build();
            items.add(guiItem);
            if (truncate > 0 && ++i >= truncate) {
                break;
            }
        }
        return items;
    }
}
