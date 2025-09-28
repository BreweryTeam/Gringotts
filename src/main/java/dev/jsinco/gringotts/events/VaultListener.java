package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.configuration.Config;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.YourVaultsGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.obj.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class VaultListener implements Listener {

    // Save vault data when the inventory is closed
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (!(holder instanceof Vault vault)) {
            return;
        }
        DataSource dataSource = DataSource.getInstance();
        dataSource.saveVault(vault);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder(false);
        if (!(holder instanceof Vault vault)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Config.QuickReturn quickReturn = ConfigManager.instance().config().quickReturn();
        if (event.getClickedInventory() == null && quickReturn.enabled() && event.getClick() == quickReturn.clickType()) {
            GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(player.getUniqueId());
            YourVaultsGui gui = GringottsGui.factory(() -> new YourVaultsGui(gringottsPlayer));
            gui.open(player);
        }
    }
}
