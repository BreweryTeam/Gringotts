package dev.jsinco.malts.obj;

import com.google.common.base.Preconditions;
import dev.jsinco.malts.api.events.interfaces.EventAction;
import dev.jsinco.malts.api.events.warehouse.WarehouseCompartmentEvent;
import dev.jsinco.malts.api.events.warehouse.WarehouseDestockEvent;
import dev.jsinco.malts.api.events.warehouse.WarehouseStockEvent;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.files.GuiConfig;
import dev.jsinco.malts.configuration.files.Lang;
import dev.jsinco.malts.enums.TriState;
import dev.jsinco.malts.gui.item.GuiItem;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Text;
import dev.jsinco.malts.utility.Util;
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
import java.util.EnumMap;
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
    private final EnumMap<Material, Stock> warehouseMap; // Mapped to Material for faster lookup

    public Warehouse(UUID owner, EnumMap<Material, Stock> warehouseMap) {
        this.owner = owner;
        this.warehouseMap = warehouseMap;
    }
    public Warehouse(UUID owner) {
        this.owner = owner;
        this.warehouseMap = new EnumMap<>(Material.class);
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
        MaltsPlayer maltsPlayer = DataSource.getInstance().cachedObject(owner, MaltsPlayer.class);
        Preconditions.checkNotNull(maltsPlayer, "MaltsPlayer is null for owner: " + owner);
        int currentStockQuantity = currentStockQuantity();
        int maxWarehouseStock = maltsPlayer.getCalculatedMaxWarehouseStock();

        if (!material.isItem()) {
            throw new IllegalArgumentException("Material must be an item");
        } else if (currentStockQuantity + amt > maxWarehouseStock) {
            amt = maxWarehouseStock - currentStockQuantity;
        }

        WarehouseStockEvent stockEvent = new WarehouseStockEvent(this, material, amt);
        if (!stockEvent.callEvent()) return 0;

        material = stockEvent.getMaterial();
        amt = stockEvent.getAmount();


        Stock stock = warehouseMap.get(material);
        if (stock != null) {
            stock.increase(amt);
        } else {
            WarehouseCompartmentEvent compartmentEvent = new WarehouseCompartmentEvent(this, EventAction.ADD, material);
            if (!compartmentEvent.callEvent()) return 0;

            material = compartmentEvent.getMaterial();
            warehouseMap.put(material, new Stock(material, amt));
        }

        return amt;
    }

    @Nullable
    public ItemStack destockItem(Material material, int amt) {
        Stock stock = warehouseMap.get(material);
        if (stock == null) return null;

        WarehouseDestockEvent event = new WarehouseDestockEvent(this, material, stock.getAmount());
        event.setCancelled(stock.getAmount() < 1);

        if (!event.callEvent()) {
            return null;
        } else if (event.getAmount() < amt) {
            amt = event.getAmount();
        }

        if (amt < 1) {
            return null;
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

    public TriState removeCompartment(Material material) {
        Stock stock = warehouseMap.get(material);
        if (stock == null) return TriState.ALTERNATIVE_STATE;

        WarehouseCompartmentEvent event = new WarehouseCompartmentEvent(this, EventAction.REMOVE, material);
        event.setCancelled(stock.getAmount() > 0);

        if (event.callEvent()) {
            warehouseMap.remove(material);
            return TriState.TRUE;
        }
        return TriState.FALSE;
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

    @Override
    public String toString() {
        return "Warehouse{" +
                "expire=" + expire +
                ", owner=" + owner +
                ", warehouseMap=" + warehouseMap +
                '}';
    }
}
