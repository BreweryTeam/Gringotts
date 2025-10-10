package dev.jsinco.gringotts.api.events.gui;

import dev.jsinco.gringotts.api.events.interfaces.GringottsGuiEvent;
import dev.jsinco.gringotts.gui.GringottsGui;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player clicks in a Gringotts GUI inventory.
 * Wrapper for InventoryClickEvent.
 */
public class GringottsGuiClickEvent extends GringottsGuiEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final InventoryClickEvent backing;

    public GringottsGuiClickEvent(@NotNull GringottsGui gui, @NotNull InventoryClickEvent backing) {
        super(gui);
        this.backing = backing;
    }

    /**
     * The underlying Bukkit InventoryClickEvent
     * @return Underlying Bukkit InventoryClickEvent
     */
    @NotNull
    public InventoryClickEvent getBacking() {
        return backing;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
