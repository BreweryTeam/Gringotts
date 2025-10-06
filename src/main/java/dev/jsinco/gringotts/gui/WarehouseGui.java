package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.IntPair;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.gui.item.UncontainedGuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Util;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// FIXME: I hate this code.
public class WarehouseGui extends GringottsGui {

    private static final GuiConfig.WarehouseGui cfg = ConfigManager.get(GuiConfig.class).warehouseGui();

    private PaginatedGui paginatedGui;

    private Warehouse warehouse;
    private GringottsPlayer gringottsPlayer;

    private ClickType state;


    private final GuiItem previousPage = GuiItem.builder()
            .index(() -> cfg.previousPage().slot())
            .itemStack(b -> b
                    .displayName(cfg.previousPage().title())
                    .material(cfg.previousPage().material())
                    .lore(cfg.previousPage().lore())
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();

                Inventory inv = paginatedGui.getPrevious(e.getInventory());
                if (inv != null) {
                    player.openInventory(inv);
                } else {
                    player.sendMessage("You are on the first page.");
                }
            })
            .build();
    private final GuiItem nextPage = GuiItem.builder()
            .index(() -> cfg.nextPage().slot())
            .itemStack(b -> b
                    .displayName(cfg.nextPage().title())
                    .material(cfg.nextPage().material())
                    .lore(cfg.nextPage().lore())
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();

                Inventory inv = paginatedGui.getNext(e.getInventory());
                if (inv != null) {
                    player.openInventory(inv);
                } else {
                    player.sendMessage("You are on the last page.");
                }
            })
            .build();
    private final UncontainedGuiItem managerButton = UncontainedGuiItem.builder()
            .index(() -> cfg.managerButton().slot())
            .itemStack(b -> b
                    .displayName(cfg.managerButton().name())
                    .material(cfg.managerButton().material())
                    .lore(cfg.managerButton().lore())
            )
            .action((event, self, isClicked) -> {
                Player player = (Player) event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();
                Inventory clickedInventory = event.getClickedInventory();
                if (clickedItem == null) {
                    return;
                }
                ItemStack iconItem = Arrays.stream(event.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(item -> Util.hasPersistentKey(item, self.key()))
                        .findFirst().orElse(null);

                boolean currentValue = iconItem.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) != null ? iconItem.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) : false;

                if (!currentValue) {
                    if (isClicked) {
                        iconItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                        state = event.getClick();
                    }
                    return;
                }

                switch (state) {
                    case LEFT -> {
                        if (clickedInventory != event.getInventory() && !warehouse.contains(clickedItem.getType())) {
                            warehouse.stockItem(clickedItem.getType(), 0);
                            refresh(player);
                        }
                    }
                    case RIGHT -> {
                        if (clickedInventory == event.getInventory()) {
                            if (warehouse.removeItem(clickedItem.getType())) {
                                player.sendMessage("Removed " + clickedItem.getType() + " from the warehouse.");
                                refresh(player);
                            } else {
                                player.sendMessage("This item's stock is not at 0, so it cannot be removed.");
                            }
                        }
                    }
                }
                state = null;
                iconItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
                event.setCancelled(true);
            })
            .build();

    @SuppressWarnings("unchecked")
    private final GuiItem statusIcon = GuiItem.builder()
            .index(() -> cfg.statusIcon().slot())
            .itemStack(b -> b
                    .stringReplacements(
                            Couple.of("{name}", gringottsPlayer.name()),
                            Couple.of("{stock}", warehouse.currentStockQuantity()),
                            Couple.of("{maxStock}", gringottsPlayer.getCalculatedMaxWarehouseStock()),
                            Couple.of("{stockPercent}", String.format("%.0f", warehouseUsagePercent()))
                    )
                    .displayName(cfg.statusIcon().name())
                    .lore(cfg.statusIcon().lore())
                    .material(cfg.statusIcon().material())
                    .headOwner(cfg.statusIcon().headOwner())
            )
            .build();

    public WarehouseGui(Warehouse warehouse, GringottsPlayer gringottsPlayer) {
        super(cfg.title(), cfg.size());
        this.gringottsPlayer = gringottsPlayer;
        this.warehouse = warehouse;

        addGuiItem(previousPage);
        addGuiItem(nextPage);
        addGuiItem(managerButton);
        addGuiItem(statusIcon);

        List<ItemStack> itemStacks = new ArrayList<>();
        for (GuiItem guiItem : warehouse.stockAsGuiItems(-1)) {
            itemStacks.add(guiItem.guiItemStack());
            addGuiItem(guiItem);
        }

        IntPair slots = cfg.warehouseItem().slots();
        List<Integer> ignoredSlots = cfg.warehouseItem().ignoredSlots();

        int i = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null && (!slots.includes(i) || ignoredSlots.contains(i))) {
                inventory.setItem(i, ItemStacks.BORDER);
            }
            i++;
        }

        this.paginatedGui = PaginatedGui.builder()
                .name(cfg.title())
                .items(itemStacks)
                .startEndSlots(slots.a(), slots.b())
                .ignoredSlots(ignoredSlots)
                .base(this.getInventory())
                .build();
    }

    @Override
    public void onPreInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (warehouse.isExpired()) {
            player.sendMessage("This warehouse view has expired. (Object expired, try re-opening this GUI.)");
            player.closeInventory();
            return;
        }
        super.onPreInventoryClick(event);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Config.QuickReturn quickReturn = ConfigManager.get(Config.class).quickReturn();

        event.setCancelled(true);

        if (event.getClickedInventory() == null && quickReturn.enabled() && event.getClick() == quickReturn.clickType()) {
            GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(player.getUniqueId());
            YourVaultsGui gui = GringottsGui.factory(() -> new YourVaultsGui(gringottsPlayer));
            gui.open(player);
        }
    }


    @Override
    public void open(Player player) {
        Executors.sync(() -> player.openInventory(this.paginatedGui.getPage(0)));
    }

    private void refresh(Player player) {
        WarehouseGui newGui = GringottsGui.factory(() -> new WarehouseGui(warehouse, gringottsPlayer));
        newGui.open(player);
    }

    private double warehouseUsagePercent() {
        int dem = gringottsPlayer.getCalculatedMaxWarehouseStock();
        if (dem == 0) {
            return 0;
        }
        return ((double) warehouse.currentStockQuantity() / dem) * 100.0;
    }
}
