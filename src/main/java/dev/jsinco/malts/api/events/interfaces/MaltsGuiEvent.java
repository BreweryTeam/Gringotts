package dev.jsinco.malts.api.events.interfaces;

import dev.jsinco.malts.api.events.MaltsEvent;
import dev.jsinco.malts.gui.MaltsGui;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for all Malts gui-related events
 */
public abstract class MaltsGuiEvent extends MaltsEvent implements Cancellable {

    private final MaltsGui gui;

    private boolean cancelled;

    public MaltsGuiEvent(@NotNull MaltsGui gui, boolean async) {
        super(async);
        this.gui = gui;
    }

    /**
     * The GUI involved in the event
     * @return GUI involved in the event
     */
    @NotNull
    public MaltsGui getGui() {
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
