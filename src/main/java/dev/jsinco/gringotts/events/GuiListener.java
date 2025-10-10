package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.api.events.gui.GringottsGuiClickEvent;
import dev.jsinco.gringotts.gui.GringottsGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof GringottsGui gui)) {
            return;
        }

        GringottsGuiClickEvent gringottsGuiClickEvent = new GringottsGuiClickEvent(gui, event);
        if (!gringottsGuiClickEvent.callEvent()) {
            event.setCancelled(true);
            return;
        }

        gui.onPreInventoryClick(event);
    }
}
