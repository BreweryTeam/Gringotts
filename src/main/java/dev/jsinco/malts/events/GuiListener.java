package dev.jsinco.malts.events;

import dev.jsinco.malts.api.events.gui.MaltsGuiClickEvent;
import dev.jsinco.malts.gui.MaltsGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof MaltsGui gui)) {
            return;
        }

        MaltsGuiClickEvent maltsGuiClickEvent = new MaltsGuiClickEvent(gui, event);
        if (!maltsGuiClickEvent.callEvent()) {
            event.setCancelled(true);
            return;
        }

        gui.onPreInventoryClick(event);
    }
}
