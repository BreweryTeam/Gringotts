package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.gui.item.AutoRegisterGuiItems;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.ItemStacks;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarehouseGui extends GringottsGui {

    private PaginatedGui paginatedGui;

    private Warehouse warehouse;
    private GringottsPlayer gringottsPlayer;


    private final GuiItem previousPage = GuiItem.builder()
            .index(() -> 48)
            .itemStack(b -> b
                    .displayName("Previous Page")
                    .material(Material.ARROW)
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
            .index(() -> 50)
            .itemStack(b -> b
                    .displayName("Next Page")
                    .material(Material.ARROW)
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

    public WarehouseGui(Warehouse warehouse, GringottsPlayer gringottsPlayer) {
        super("Warehouse", 54);

        this.gringottsPlayer = gringottsPlayer;
        this.warehouse = warehouse;

        List<ItemStack> itemStacks = new ArrayList<>();
        for (GuiItem guiItem : warehouse.stockAsGuiItems(-1)) {
            itemStacks.add(guiItem.guiItemStack());
            addGuiItem(guiItem);
        }

        for (int i = 45; i < 54; i++) {
            this.inventory.setItem(i, ItemStacks.BORDER);
        }

        addGuiItem(previousPage);
        addGuiItem(nextPage);

        this.paginatedGui = PaginatedGui.builder()
                .name("Warehouse")
                .items(itemStacks)
                .startEndSlots(10, 34)
                .base(this.getInventory())
                .build();
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }


    @Override
    public void open(Player player) {
        Executors.sync(() -> player.openInventory(this.paginatedGui.getPage(0)));
    }
}
