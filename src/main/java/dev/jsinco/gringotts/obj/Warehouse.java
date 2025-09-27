package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.storage.DataSource;
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
    private final Map<Material, Integer> warehouseMap;

    public Warehouse(UUID owner, Map<Material, Integer> warehouseMap) {
        this.owner = owner;
        this.warehouseMap = warehouseMap;
    }
    public Warehouse(UUID owner) {
        this.owner = owner;
        this.warehouseMap = new HashMap<>();
    }

    public boolean stockItem(Material material, int amt) {
        GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(owner);

        if (!material.isItem()) {
            throw new IllegalArgumentException("Material must be an item");
        } else if (currentStockQuantity() + amt > gringottsPlayer.getCalculatedMaxWarehouseStock()) {
            return false;
        }

        int currentAmt = warehouseMap.getOrDefault(material, 0);
        warehouseMap.put(material, currentAmt + amt);
        return true;
    }

    @Nullable
    public ItemStack destockItem(Material material, int amt) {
        int currentAmt = warehouseMap.getOrDefault(material, 0);
        if (currentAmt < 1) {
            return null;
        } else if (currentAmt < amt) {
            amt = currentAmt;
        }

        warehouseMap.put(material, currentAmt - amt);
        return ItemStack.of(material, amt);
    }

    public boolean hasItem(Material material) {
        return warehouseMap.containsKey(material);
    }

    public int getQuantity(Material material) {
        return warehouseMap.getOrDefault(material, 0);
    }

    public Map<Material, Integer> stockCopy() {
        return Map.copyOf(warehouseMap);
    }

    public void setStock(Map<Material, Integer> stock) {
        warehouseMap.clear();
        warehouseMap.putAll(stock);
    }

    public int currentStockQuantity() {
        return warehouseMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    public List<GuiItem> stockAsGuiItems(int truncate) {
        List<GuiItem> items = new ArrayList<>();
        int i = 0;
        for (var entry : warehouseMap.entrySet()) {
            Material material = entry.getKey();
            // TODO: Cleanup
            GuiItem guiItem = GuiItem.builder()
                    .itemStack(b -> b
                            .material(material)
                            .displayName("<aqua><b>" + Util.formatMaterialName(material.toString()))
                            .colorIfAbsentLore(NamedTextColor.GRAY)
                            .lore(guiItemLore(entry.getValue()))
                    )
                    .action(e -> {
                        Player player = (Player) e.getWhoClicked();
                        PlayerInventory inv = player.getInventory();
                        ItemStack clickedItem = e.getCurrentItem();

                        switch (e.getClick()) {
                            case LEFT -> {
                                ItemStack item = destockItem(material, 10);
                                if (item == null) {
                                    player.sendMessage("You do not have 10 of " + material + ".");
                                } else {
                                    inv.addItem(item);
                                }
                            }
                            case SHIFT_LEFT -> {
                                if (inv.contains(material, 10) && stockItem(material, 10)) {
                                    inv.removeItemAnySlot(new ItemStack(material, 10));
                                } else {
                                    player.sendMessage("You do not have 10 of " + material + ". (Or you're at your max storage size...)");
                                }
                            }
                            case RIGHT, SHIFT_RIGHT -> {
                                player.sendMessage("Not yet implemented");
                            }
                        }

                        Util.editMeta(clickedItem, meta -> {
                            meta.lore(
                                    Text.mmlNoItalic(guiItemLore(entry.getValue()), NamedTextColor.GRAY)
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

    @Override
    public UUID getUuid() {
        return owner;
    }


    @Override
    public void save(DataSource dataSource) {
        dataSource.saveWarehouse(this);
    }

    private static List<String> guiItemLore(int quantity) {
        List<String> l = List.of(
                //"",
                //"+ Status: <white>{status}",
                "",
                "+ Quantity: <yellow>{quantity}</yellow>",
                "",
                "<dark_gray>[<gold>Left Click</gold>]</dark_gray> Withdraw one item.",
                "<dark_gray>[<gold>Right Click</gold>]</dark_gray> Withdraw a stack.",
                "<dark_gray>[<gold>Shift Left</gold>]</dark_gray> Withdraw all.",
                "<dark_gray>[<gold>Shift Right</gold>]</dark_gray> Add all to storage."
        );

        return l.stream().map(s -> s.replace("{quantity}", String.valueOf(quantity))).toList();
    }
}
