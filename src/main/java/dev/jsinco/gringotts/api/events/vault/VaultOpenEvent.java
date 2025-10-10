package dev.jsinco.gringotts.api.events.vault;

import dev.jsinco.gringotts.api.events.interfaces.VaultEvent;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.utility.Couple;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a player attempts to open a vault.
 * If the player cannot open the vault (because another player that does not have the permission 'gringotts.mod' is viewing the vault already),
 * the event will be called in a cancelled state.
 */
public class VaultOpenEvent extends VaultEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player viewer;
    private final Couple<Vault.VaultOpenState, @Nullable Player> openState;

    private boolean cancelled;

    public VaultOpenEvent(@NotNull Vault vault, @NotNull Player viewer, @NotNull Couple<Vault.@NotNull VaultOpenState, @Nullable Player> state) {
        super(vault);
        this.viewer = viewer;
        this.openState = state;
    }

    /**
     * The player attempting to open the vault
     * @return Player attempting to open the vault
     */
    @NotNull
    public Player getViewer() {
        return viewer;
    }

    /**
     * The state of the vault being opened, and the <b>first</b> viewer Gringotts found, if applicable.
     * @return Couple of VaultOpenState and he <b>first</b> viewer Gringotts found (nullable)
     */
    @NotNull
    public Couple<Vault.@NotNull VaultOpenState, @Nullable Player> getOpenState() {
        return openState;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
