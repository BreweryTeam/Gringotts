package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.GuiConfig;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import lombok.Setter;
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

public class Warehouse implements CachedObject {

    @Getter
    private final long cacheTime = System.currentTimeMillis();
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
        if (stock != null) {
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

    public boolean contains(Material material) {
        return warehouseMap.containsKey(material);
    }


    @Override
    public UUID getUuid() {
        return owner;
    }


    @Override
    public void save(DataSource dataSource) {
        dataSource.saveWarehouse(this);
    }


    public List<GuiItem> stockAsGuiItems(int truncate) {
        GuiConfig.WarehouseGui.WarehouseItem cfg = ConfigManager.instance().guiConfig().warehouseGui().warehouseItem();
        List<GuiItem> items = new ArrayList<>();
        int i = 0;
        List<Stock> sortedStocks = warehouseMap.values().stream()
                .sorted(Comparator.comparingLong(Stock::getLastUpdate).reversed())
                .toList();
        for (Stock stock : sortedStocks) {
            Material material = stock.getMaterial();
            // TODO: Cleanup
            GuiItem guiItem = GuiItem.builder()
                    .itemStack(b -> b
                            .material(material)
                            .displayName(cfg.title().replace("{material}", Util.formatMaterialName(material.toString())))
                            .lore(
                                    cfg.lore().stream().map(l -> l.replace("{quantity}", String.valueOf(stock.getAmount()))).toList()
                            )
                    )
                    .action(e -> {
                        if (e.isCancelled()) {
                            return;
                        }
                        Player player = (Player) e.getWhoClicked();
                        PlayerInventory inv = player.getInventory();
                        ItemStack clickedItem = e.getCurrentItem();

                        switch (e.getClick()) {
                            case LEFT -> {
                                ItemStack item = destockItem(material, 1);
                                if (item == null) {
                                    player.sendMessage("You do not have 1 of " + material + ".");
                                } else {
                                    inv.addItem(item);
                                }
                            }
                            case RIGHT -> {
                                ItemStack item = destockItem(material, 64);
                                if (item == null) {
                                    player.sendMessage("You do not have 64 of " + material + ".");
                                } else {
                                    inv.addItem(item);
                                }
                            }
                            case SHIFT_LEFT -> {
                                int invAmt = Util.getAmountInvCanHold(inv, material);
                                if (invAmt == 0) {
                                    player.sendMessage("Your inventory is full.");
                                    return;
                                }

                                ItemStack item = destockItem(material, invAmt);
                                if (item != null) {
                                } else {
                                    player.sendMessage("You do not have any" + material + ".");
                                }
                            }
                            case SHIFT_RIGHT -> {
                                int invAmt = Util.getMaterialAmount(inv, material);
                                if (invAmt == 0) {
                                    player.sendMessage("You have none of this material.");
                                    return;
                                }
                                int diff = stockItem(material, invAmt);
                                if (diff > 0) {
                                    inv.removeItem(new ItemStack(material, diff));
                                } else {
                                    player.sendMessage("You're out of storage in your warehouse!");
                                }
                            }
                        }

                        Util.editMeta(clickedItem, meta -> {
                            meta.lore(
                                    Text.mmlNoItalic(cfg.lore().stream().map(l -> l.replace("{quantity}", String.valueOf(stock.getAmount()))).toList())
                            );
                        });
                    }).build();
            items.add(guiItem);
            if (truncate > 0 && ++i >= truncate) {
                break;
            }
        }
        return items;
    }
}
