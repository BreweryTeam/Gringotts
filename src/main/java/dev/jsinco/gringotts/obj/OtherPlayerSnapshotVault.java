package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.utility.ItemStacks;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class OtherPlayerSnapshotVault extends SnapshotVault {

    public OtherPlayerSnapshotVault(SnapshotVault snapshotVault) {
        super(snapshotVault.getOwner(), snapshotVault.getId(), snapshotVault.getCustomName(), snapshotVault.getIcon(), snapshotVault.getTrustedPlayers());
    }

    @Override
    public ItemStack itemStack() {
        return ItemStacks.builder()
                .displayName(getCustomName())
                .material(getIcon())
                .lore(
                        "Left-click to open",
                        "this vault."
                )
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
