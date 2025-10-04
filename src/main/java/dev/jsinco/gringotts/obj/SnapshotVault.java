package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.gui.EditVaultGui;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.item.AbstractGuiItem;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// A snapshot of a vault with reduced information, used for listing or quick access
public class SnapshotVault implements AbstractGuiItem {

    @Getter
    private final UUID owner;
    @Getter
    private final int id;
    @Getter
    private final String customName;
    @Getter
    private final Material icon;


    @Nullable
    private Vault lazyVault;

    public SnapshotVault(UUID owner, int id, String customName, Material icon) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;
    }


    public CompletableFuture<Vault> toVault() {
        DataSource dataSource = DataSource.getInstance();
        return dataSource.getVault(owner, id);
    }

    @Override
    public ItemStack itemStack() {
        return ItemStacks.builder()
                .displayName(customName)
                .material(icon)
                .lore(
                        "Left-click to open",
                        "this vault.",
                        "",
                        "Right-click to open",
                        "your vault settings."
                )
                .build();
    }

    private CompletableFuture<Vault> lazyVault() {
        if (this.lazyVault == null) {
            return toVault().thenApply(vault -> {
                this.lazyVault = vault;
                return vault;
            });
        }
        return CompletableFuture.completedFuture(lazyVault);
    }

    @Override
    public void onClick(InventoryClickEvent event, ItemStack clickedItem) {
        if (!Util.hasPersistentKey(clickedItem, key())) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.isLeftClick()) {
            // Open vault
            lazyVault().thenAccept(vault -> vault.open(player));
        } else if (event.isRightClick()) {
            lazyVault().thenAccept(vault -> {
                GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(player.getUniqueId());
                EditVaultGui editVaultGui = GringottsGui.factory(() -> new EditVaultGui(vault, gringottsPlayer, player));
                editVaultGui.open(player);
            });
        }
    }
}
