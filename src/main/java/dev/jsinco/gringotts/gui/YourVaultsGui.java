package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.gui.item.AutoRegisterGuiItems;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.gui.item.IgnoreAutoRegister;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AutoRegisterGuiItems
public class YourVaultsGui extends GringottsGui implements PromisedInventory {

    private PaginatedGui paginatedGui;
    private GringottsPlayer gringottsPlayer;
    private Inventory secondInv;
    private int indexStop;

    @IgnoreAutoRegister
    private final GuiItem warehouseButton = GuiItem.builder()
            .itemStack(b -> b
                    .displayName("View all Warehouse")
                    .lore("Click to view your warehouse")
                    .material(Material.ENDER_CHEST)
            )
            .action(e -> {
                Warehouse warehouse = DataSource.getInstance().cachedWarehouse(gringottsPlayer.getUuid());
                WarehouseGui warehouseGui = GringottsGui.factory(() -> new WarehouseGui(warehouse, gringottsPlayer));
                warehouseGui.open((Player) e.getWhoClicked());
            })
            .build();
    private final GuiItem previousPage = GuiItem.builder()
            .index(() -> indexStop + 3)
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
            .index(() -> indexStop + 5)
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


    public YourVaultsGui(GringottsPlayer gringottsPlayer) {
        super("Your Vaults", 54);
        this.gringottsPlayer = gringottsPlayer;
        this.secondInv = Bukkit.createInventory(this, 54, Text.mm("Your Vaults"));

        Warehouse warehouse = DataSource.getInstance().cachedWarehouse(gringottsPlayer.getUuid());
        List<GuiItem> warehouseItems = warehouse.stockAsGuiItems(7);
        this.indexStop = warehouseItems.isEmpty() ? 45 : 36;

        if (!warehouseItems.isEmpty()) {
            addGuiItem(warehouseButton, 45);

            int i = 0;
            for (GuiItem item : warehouseItems) {

                addGuiItem(item, 46 + i);
                i++;
            }
        }



        for (int i = indexStop; i < indexStop + 9; i++) {
            this.inventory.setItem(i, ItemStacks.BORDER);
        }

        // secondInv

        for (int i = 45; i < 54; i++) {
            this.secondInv.setItem(i, ItemStacks.BORDER);
        }
        this.secondInv.setItem(48, previousPage.guiItemStack());
        this.secondInv.setItem(50, nextPage.guiItemStack());
    }

    @Override
    public CompletableFuture<Inventory> promiseInventory() {
        DataSource dataSource = DataSource.getInstance();
        CompletableFuture<Inventory> future = new CompletableFuture<>();


        dataSource.getVaults(gringottsPlayer.getUuid()).thenAccept(snapshotVaults -> {

            List<ItemStack> itemStacks = new ArrayList<>();

            // FIXME: I don't like this solution, I'm just stupid and lazy
            for (int i = 0; i <  gringottsPlayer.getCalculatedMaxVaults(); i++) {
                final int finalI = i;
                SnapshotVault snapshotVault = snapshotVaults.stream().filter(it -> it.getId() == finalI + 1).findFirst().orElse(null);
                if (snapshotVault == null) {
                    snapshotVault = new SnapshotVault(gringottsPlayer.getUuid(), i + 1, null, null);
                }

                addGuiItem(snapshotVault, -1);
                itemStacks.add(snapshotVault.guiItemStack());
            }

            // Initialize the paginated GUI with the current items
            this.paginatedGui = PaginatedGui.builder()
                    .name("Your Vaults")
                    .items(itemStacks)
                    .startEndSlots(0, indexStop)
                    .base(this.getInventory())
                    .secondBase(this.secondInv)
                    .build();


            future.complete(this.paginatedGui.getPage(0));
        });


        return future;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void open(Player player) {
        promiseInventory().thenAccept(inventory -> {
            Executors.sync(() -> player.openInventory(inventory));
        });
    }
}
