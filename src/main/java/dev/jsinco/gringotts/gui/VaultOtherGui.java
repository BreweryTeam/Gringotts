package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.IntPair;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.OtherPlayerSnapshotVault;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.ItemStacks;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VaultOtherGui extends GringottsGui implements PromisedInventory {

    private static final GuiConfig.VaultOtherGui cfg = ConfigManager.get(GuiConfig.class).vaultOtherGui();
    private static final Lang lng = ConfigManager.get(Lang.class);

    private PaginatedGui paginatedGui;

    private final Player viewer;
    private final OfflinePlayer target;

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
                    lng.entry(l -> l.gui().firstPage(), player);
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
                    lng.entry(l -> l.gui().lastPage(), player);
                }
            })
            .build();



    public VaultOtherGui(Player viewer, OfflinePlayer target) {
        super("Placeholder", cfg.size());
        this.viewer = viewer;
        this.target = target;

        this.addGuiItem(previousPage);
        this.addGuiItem(nextPage);

        IntPair slots = cfg.vaultItem().slots();
        List<Integer> ignoredSlots = cfg.vaultItem().ignoredSlots();
        for (int i = 0; i < this.inventory.getSize(); i++) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (itemStack != null || (slots.includes(i) && !ignoredSlots.contains(i))) continue;
            this.inventory.setItem(i, ItemStacks.BORDER);
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public CompletableFuture<@Nullable Inventory> promiseInventory() {
        DataSource dataSource = DataSource.getInstance();
        return dataSource.getVaults(target.getUniqueId()).thenCompose(unfilteredSnapshotVaults -> {
            if (unfilteredSnapshotVaults.isEmpty()) {
                lng.entry(l -> l.vaults().noVaultsFound(), viewer, Couple.of("{name}", targetName()));
                return CompletableFuture.completedFuture(null);
            }

            List<OtherPlayerSnapshotVault> snapshotVaults = unfilteredSnapshotVaults.stream()
                    .filter(it -> it.canAccess(viewer))
                    .map(OtherPlayerSnapshotVault::new)
                    .sorted(Comparator.comparingInt(SnapshotVault::getId))
                    .toList();

            if (snapshotVaults.isEmpty()) {
                lng.entry(l -> l.vaults().noVaultsAccessible(), viewer, Couple.of("{name}", targetName()));
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<GringottsPlayer> playerFuture;

            GringottsPlayer cached = dataSource.cachedGringottsPlayer(target.getUniqueId());
            playerFuture = cached != null ? CompletableFuture.completedFuture(cached) : dataSource.getGringottsPlayer(target.getUniqueId());

            return playerFuture.thenApply(targetGringottsPlayer -> {
                List<ItemStack> itemStacks = new ArrayList<>();

                for (var snapshotVault : snapshotVaults) {
                    addGuiItem(snapshotVault);
                    itemStacks.add(snapshotVault.guiItemStack());
                }

                IntPair slots = cfg.vaultItem().slots();
                this.paginatedGui = PaginatedGui.builder()
                        .name(cfg.title().replace("{name}", targetName()))
                        .items(itemStacks)
                        .startEndSlots(slots.a(), slots.b())
                        .ignoredSlots(cfg.vaultItem().ignoredSlots())
                        .base(this.inventory)
                        .build();

                return this.paginatedGui.getPage(0);
            });
        });
    }

    @Override
    public void open(Player player) {
        promiseInventory().thenAccept(inventory -> {
            if (inventory != null) {
                Executors.sync(() -> player.openInventory(inventory));
            } else {
                player.sendMessage("You do not have access to any vaults for " + targetName() + ". (Have they trusted you to any of them?)");
            }
        });
    }

    private String targetName() {
        return target.getName() != null ? target.getName() : "Unknown";
    }


}
