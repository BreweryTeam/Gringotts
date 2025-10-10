package dev.jsinco.gringotts.api.events.gui;


import dev.jsinco.gringotts.api.events.interfaces.GringottsGuiEvent;
import dev.jsinco.gringotts.gui.GringottsGui;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player opens a Gringotts gui
 */
public class GringottsGuiOpenEvent extends GringottsGuiEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;

    public GringottsGuiOpenEvent(@NotNull GringottsGui gui, @NotNull Player player) {
        super(gui);
        this.player = player;
    }

    /**
     * The player opening the GUI
     * @return Player opening the GUI
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Set the player opening the GUI
     * @param player Player opening the GUI
     */
    public void setPlayer(@NotNull Player player) {
        this.player = player;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
