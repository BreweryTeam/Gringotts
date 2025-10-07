package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.IntPair;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class YourVaultsGui extends GringottsGui implements PromisedInventory {

    private static final GuiConfig.YourVaultsGui cfg = ConfigManager.get(GuiConfig.class).yourVaultsGui();
    private static final Lang lng = ConfigManager.get(Lang.class);

    private PaginatedGui paginatedGui;
    private GringottsPlayer gringottsPlayer;
    private final Inventory secondInv;
    private final boolean withQuickbar;

    private final GuiItem warehouseButton = GuiItem.builder()
            .itemStack(b -> b
                    .displayName(cfg.warehouseQuickbar().name())
                    .material(cfg.warehouseQuickbar().material())
                    .lore(cfg.warehouseQuickbar().lore())
            )
            .action(e -> {
                Warehouse warehouse = DataSource.getInstance().cachedWarehouse(gringottsPlayer.getUuid());
                WarehouseGui warehouseGui = GringottsGui.factory(() -> new WarehouseGui(warehouse, gringottsPlayer));
                warehouseGui.open((Player) e.getWhoClicked());
            })
            .build();
    private final GuiItem previousPage = GuiItem.builder()
            .itemStack(b -> b
                    .displayName(cfg.previousPage().name())
                    .material(cfg.previousPage().material())
                    .lore(cfg.previousPage().lore())
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();

                Inventory inv = paginatedGui.getPrevious(e.getInventory());
                if (inv != null) {
                    player.openInventory(inv);
                } else {
                    lng.entry(l -> l.gui().firstPage(), player);
                }
            })
            .build();
    private final GuiItem nextPage = GuiItem.builder()
            .itemStack(b -> b
                    .displayName(cfg.nextPage().name())
                    .material(cfg.nextPage().material())
                    .lore(cfg.nextPage().lore())
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();

                Inventory inv = paginatedGui.getNext(e.getInventory());
                if (inv != null) {
                    player.openInventory(inv);
                } else {
                    lng.entry(l -> l.gui().lastPage(), player);
                }
            })
            .build();


    public YourVaultsGui(GringottsPlayer gringottsPlayer) {
        super(cfg.title(), cfg.size());
        this.gringottsPlayer = gringottsPlayer;
        this.secondInv = Bukkit.createInventory(this, 54, Text.mm(cfg.title()));
        this.autoRegister(false);

        Warehouse warehouse = DataSource.getInstance().cachedWarehouse(gringottsPlayer.getUuid());

        this.withQuickbar = this.assemble(this.inventory, warehouse);
        this.assemble(this.secondInv, null);
    }

    @Override
    public CompletableFuture<Inventory> promiseInventory() {
        DataSource dataSource = DataSource.getInstance();
        CompletableFuture<Inventory> future = new CompletableFuture<>();


        dataSource.getVaults(gringottsPlayer.getUuid()).thenAccept(snapshotVaults -> {

            List<ItemStack> itemStacks = new ArrayList<>();

            for (int i = 0; i <  gringottsPlayer.getCalculatedMaxVaults(); i++) {
                final int finalI = i;
                SnapshotVault snapshotVault = snapshotVaults.stream().filter(it -> it.getId() == finalI + 1).findFirst().orElse(null);
                if (snapshotVault == null) {
                    snapshotVault = new SnapshotVault(gringottsPlayer.getUuid(), i + 1, null, null);
                }

                addGuiItem(snapshotVault);
                itemStacks.add(snapshotVault.guiItemStack());
            }
            IntPair slots = withQuickbar ? cfg.vaultItem().slots() : cfg.vaultItem().altSlots();
            List<Integer> ignoredSlots = withQuickbar ? cfg.vaultItem().ignoredSlots() : cfg.vaultItem().altIgnoredSlots();

            for (int i = 0; i < inventory.getSize() && !itemStacks.isEmpty(); i++) {
                if (slots.includes(i) && !ignoredSlots.contains(i) && !itemStacks.isEmpty()) {
                    ItemStack itemStack = itemStacks.removeFirst();
                    inventory.setItem(i, itemStack);
                }
            }


            IntPair paginatedSlots = cfg.vaultItem().altSlots();
            this.paginatedGui = PaginatedGui.builder()
                    .name(cfg.title())
                    .items(itemStacks)
                    .startEndSlots(paginatedSlots.a(), paginatedSlots.b())
                    .ignoredSlots(cfg.vaultItem().altIgnoredSlots())
                    .base(this.secondInv)
                    .build();
            this.paginatedGui.insert(this.inventory, 0);


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


    private boolean assemble(Inventory inv, @Nullable Warehouse warehouse) {
        boolean quickBar = assembleQuickbar(inv, warehouse);

        int previousPageSlot = quickBar ? cfg.previousPage().slot() : cfg.previousPage().altSlot();
        int nextPageSlot = quickBar ? cfg.nextPage().slot() : cfg.nextPage().altSlot();
        inv.setItem(previousPageSlot, previousPage.guiItemStack());
        inv.setItem(nextPageSlot, nextPage.guiItemStack());

        IntPair slots = quickBar ? cfg.vaultItem().slots() : cfg.vaultItem().altSlots();
        IntPair warehouseSlots = quickBar ? cfg.warehouseQuickbar().slots() : null;
        List<Integer> ignoredSlots = quickBar ? cfg.vaultItem().ignoredSlots() : cfg.vaultItem().altIgnoredSlots();
        List<Integer> ignoredWarehouseSlots = quickBar ? cfg.warehouseQuickbar().ignoredSlots() : null;

        if (cfg.borders()) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack itemStack = inv.getItem(i);
                if (itemStack != null || (slots.includes(i) && !ignoredSlots.contains(i))) continue;
                else if (quickBar && (warehouseSlots.includes(i) || ignoredWarehouseSlots.contains(i))) continue;

                inv.setItem(i, ItemStacks.BORDER);
            }
        }
        return quickBar;
    }

    private boolean assembleQuickbar(Inventory inv, @Nullable Warehouse warehouse) {
        IntPair slots = cfg.warehouseQuickbar().slots();
        int warehouseButtonSlot = cfg.warehouseQuickbar().slot();

        if (warehouse == null || slots.negative() || warehouseButtonSlot < 0) {
            return false;
        }

        int amount = slots.difference(false) + 1;
        List<GuiItem> warehouseItems = warehouse.stockAsGuiItems(amount);
        if (warehouseItems.isEmpty()) {
            return false;
        }

        inv.setItem(warehouseButtonSlot, warehouseButton.guiItemStack());

        List<Integer> ignoredSlots = cfg.warehouseQuickbar().ignoredSlots();
        for (int i = 0; i < Math.min(amount, warehouseItems.size()); i++) {
            if (ignoredSlots.contains(i)) {
                continue;
            }
            GuiItem item = warehouseItems.get(i);
            inv.setItem(slots.a() + i, item.guiItemStack());
            this.addGuiItem(item);
        }
        return true;
    }
}
