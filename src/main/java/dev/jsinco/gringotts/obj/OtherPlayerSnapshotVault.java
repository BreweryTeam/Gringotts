package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class OtherPlayerSnapshotVault extends SnapshotVault {

    private static final GuiConfig.VaultOtherGui.VaultItem cfg = ConfigManager.get(GuiConfig.class).vaultOtherGui().vaultItem();

    public OtherPlayerSnapshotVault(SnapshotVault snapshotVault) {
        super(snapshotVault.getOwner(), snapshotVault.getId(), snapshotVault.getCustomName(), snapshotVault.getIcon(), snapshotVault.getTrustedPlayers());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemStack itemStack() {
        return ItemStacks.builder()
                .stringReplacements(
                        Couple.of("{vaultName}", getCustomName()),
                        Couple.of("{id}", getId())
                )
                .displayName(cfg.name())
                .material(getIcon())
                .lore(cfg.lore())
                .build();
    }

    @Override
    public void onClick(InventoryClickEvent event, ItemStack clickedItem) {
        if (!Util.hasPersistentKey(clickedItem, key())) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.isLeftClick()) {
            lazyVault().thenAccept(vault -> vault.open(player));
        }
    }
}
