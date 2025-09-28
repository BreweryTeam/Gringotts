package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.configuration.Config;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.GuiConfig;
import dev.jsinco.gringotts.configuration.IntPair;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.gui.item.UncontainedGuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
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

    private static final GuiConfig guiConfig = ConfigManager.instance().guiConfig();

    private PaginatedGui paginatedGui;

    private Warehouse warehouse;
    private GringottsPlayer gringottsPlayer;

    private ClickType state;


    private final GuiItem previousPage = GuiItem.builder()
            .index(() -> guiConfig.warehouseGui().previousPage().slot())
            .itemStack(b -> b
                    .displayName(guiConfig.warehouseGui().previousPage().title())
                    .material(guiConfig.warehouseGui().previousPage().material())
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
            .index(() -> guiConfig.warehouseGui().nextPage().slot())
            .itemStack(b -> b
                    .displayName(guiConfig.warehouseGui().nextPage().title())
                    .material(guiConfig.warehouseGui().nextPage().material())
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
            .index(() -> guiConfig.warehouseGui().managerButton().slot())
            .itemStack(b -> b
                    .displayName(guiConfig.warehouseGui().managerButton().title())
                    .material(guiConfig.warehouseGui().managerButton().material())
                    .lore(guiConfig.warehouseGui().managerButton().lore())
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

    public WarehouseGui(Warehouse warehouse, GringottsPlayer gringottsPlayer) {
        super(guiConfig.warehouseGui().title(), guiConfig.warehouseGui().size());
        this.gringottsPlayer = gringottsPlayer;
        this.warehouse = warehouse;

        addGuiItem(previousPage);
        addGuiItem(nextPage);
        addGuiItem(managerButton);

        List<ItemStack> itemStacks = new ArrayList<>();
        for (GuiItem guiItem : warehouse.stockAsGuiItems(-1)) {
            itemStacks.add(guiItem.guiItemStack());
            addGuiItem(guiItem);
        }

        IntPair slots = guiConfig.warehouseGui().warehouseItem().slots();
        List<Integer> ignoredSlots = guiConfig.warehouseGui().warehouseItem().ignoredSlots();

        int i = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null && (!slots.includes(i) || ignoredSlots.contains(i))) {
                inventory.setItem(i, ItemStacks.BORDER);
            }
            i++;
        }

        this.paginatedGui = PaginatedGui.builder()
                .name(guiConfig.warehouseGui().title())
                .items(itemStacks)
                .startEndSlots(slots.a(), slots.b())
                .ignoredSlots(ignoredSlots)
                .base(this.getInventory())
                .build();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        // FIXME: Duplicated code
        Player player = (Player) event.getWhoClicked();
        Config.QuickReturn quickReturn = ConfigManager.instance().config().quickReturn();
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
}
