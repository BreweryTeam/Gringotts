package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.gui.GringottsGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getHolder(false) instanceof GringottsGui gui) {
            gui.onPreInventoryClick(event);
        }
    }
}
