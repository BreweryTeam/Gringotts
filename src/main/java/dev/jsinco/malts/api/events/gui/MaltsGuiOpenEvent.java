package dev.jsinco.malts.api.events.gui;


import dev.jsinco.malts.api.events.interfaces.MaltsGuiEvent;
import dev.jsinco.malts.gui.MaltsGui;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player opens a Malts gui
 */
public class MaltsGuiOpenEvent extends MaltsGuiEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Player player;

    public MaltsGuiOpenEvent(@NotNull MaltsGui gui, @NotNull Player player) {
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
