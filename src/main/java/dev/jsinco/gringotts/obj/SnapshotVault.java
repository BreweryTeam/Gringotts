package dev.jsinco.gringotts.obj;

import com.google.common.collect.ImmutableList;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.gui.EditVaultGui;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.item.AbstractGuiItem;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// A snapshot of a vault with reduced information
public class SnapshotVault implements AbstractGuiItem {

    private static final GuiConfig.YourVaultsGui.VaultItem cfg = ConfigManager.get(GuiConfig.class).yourVaultsGui().vaultItem();

    @Getter
    private final UUID owner;
    @Getter
    private final int id;
    @Getter
    private final String customName;
    @Getter
    private final Material icon;
    @Getter
    private final ImmutableList<UUID> trustedPlayers;


    @Nullable
    private Vault lazyVault;

    public SnapshotVault(UUID owner, int id, String customName, Material icon) {
        this(owner, id, customName, icon, ImmutableList.of());
    }

    public SnapshotVault(UUID owner, int id, String customName, Material icon, ImmutableList<UUID> trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;
        this.trustedPlayers = trustedPlayers;
    }


    public SnapshotVault(UUID owner, int id, String customName, Material icon, String trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;

        List<UUID> json = Util.GSON.fromJson(trustedPlayers, Vault.TYPE_TOKEN);
        this.trustedPlayers = json != null ? ImmutableList.copyOf(json) : ImmutableList.of();
    }


    public CompletableFuture<Vault> toVault() {
        DataSource dataSource = DataSource.getInstance();
        return dataSource.getVault(owner, id);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public boolean canAccess(Player player) {
        return player.getUniqueId() == this.owner || this.trustedPlayers.contains(player.getUniqueId()) || player.hasPermission("gringotts.mod");
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemStack itemStack() {
        return ItemStacks.builder()
                .stringReplacements(
                        Couple.of("{vaultName}", customName),
                        Couple.of("{id}", id)
                )
                .displayName(cfg.name())
                .material(icon)
                .lore(cfg.lore())
                .build();
    }

    public CompletableFuture<Vault> lazyVault() {
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
                GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedObject(player.getUniqueId(), GringottsPlayer.class);
                EditVaultGui editVaultGui = new EditVaultGui(vault, gringottsPlayer, player);
                editVaultGui.open(player);
            });
        }
    }
}
