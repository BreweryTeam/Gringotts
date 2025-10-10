package dev.jsinco.gringotts.api.events.interfaces;

import dev.jsinco.gringotts.obj.Vault;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for all vault-related events
 */
public abstract class VaultEvent extends Event implements Cancellable {

    private final Vault vault;

    private boolean cancelled;

    public VaultEvent(@NotNull Vault vault) {
        this.vault = vault;
    }

    /**
     * The vault involved in the event
     * @return Vault involved in the event
     */
    @NotNull
    public Vault getVault() {
        return vault;
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
