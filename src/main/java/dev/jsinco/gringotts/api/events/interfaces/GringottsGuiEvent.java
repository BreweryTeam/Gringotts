package dev.jsinco.gringotts.api.events.interfaces;

import dev.jsinco.gringotts.gui.GringottsGui;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for all Gringotts gui-related events
 */
public abstract class GringottsGuiEvent extends Event implements Cancellable {

    private final GringottsGui gui;

    private boolean cancelled;

    public GringottsGuiEvent(@NotNull GringottsGui gui) {
        this.gui = gui;
    }

    /**
     * The GUI involved in the event
     * @return GUI involved in the event
     */
    @NotNull
    public GringottsGui getGui() {
        return gui;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
