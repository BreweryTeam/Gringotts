package dev.jsinco.malts.events;

import dev.jsinco.malts.api.events.vault.VaultClickEvent;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.gui.MaltsGui;
import dev.jsinco.malts.gui.VaultOtherGui;
import dev.jsinco.malts.gui.YourVaultsGui;
import dev.jsinco.malts.obj.MaltsPlayer;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.obj.Vault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class VaultListener implements Listener {

    // Save vault data when the inventory is closed
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (!(holder instanceof Vault vault)) {
            return;
        }
        vault.update((Player) event.getPlayer());
        DataSource dataSource = DataSource.getInstance();
        dataSource.saveVault(vault);
    }

    @EventHandler
    public void onInventoryInteract(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof Vault vault)) {
            return;
        }
        vault.update((Player) event.getWhoClicked());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof Vault vault)) {
            return;
        }
        VaultClickEvent vaultClickEvent = new VaultClickEvent(vault, event);
        if (!vaultClickEvent.callEvent()) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Config.QuickReturn quickReturn = ConfigManager.get(Config.class).quickReturn();

        if (event.getClickedInventory() != null) {
            vault.update(player);
            return;
        } else if (!quickReturn.enabled() || event.getClick() != quickReturn.clickType()) {
            return;
        }

        MaltsPlayer maltsPlayer = DataSource.getInstance().cachedObject(player.getUniqueId(), MaltsPlayer.class);
        MaltsGui gui;
        if (!vault.getOwner().equals(player.getUniqueId())) {
            gui = new VaultOtherGui(player, Bukkit.getOfflinePlayer(vault.getOwner()));
        } else {
            gui = new YourVaultsGui(maltsPlayer);
        }
        gui.open(player);
    }
}
