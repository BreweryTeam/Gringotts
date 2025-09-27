package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.obj.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
}
