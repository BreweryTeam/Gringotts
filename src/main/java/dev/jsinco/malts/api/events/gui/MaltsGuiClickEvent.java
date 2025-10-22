package dev.jsinco.malts.api.events.gui;

import dev.jsinco.malts.api.events.interfaces.MaltsGuiEvent;
import dev.jsinco.malts.gui.MaltsGui;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player clicks in a Malts GUI inventory.
 * Wrapper for InventoryClickEvent.
 */
public class MaltsGuiClickEvent extends MaltsGuiEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final InventoryClickEvent backing;

    public MaltsGuiClickEvent(@NotNull MaltsGui gui, @NotNull InventoryClickEvent backing, boolean async) {
        super(gui, async);
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
