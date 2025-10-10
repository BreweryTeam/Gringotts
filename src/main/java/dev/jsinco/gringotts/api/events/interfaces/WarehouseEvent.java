package dev.jsinco.gringotts.api.events.interfaces;

import dev.jsinco.gringotts.obj.Warehouse;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Base event for all warehouse-related events
 */
public abstract class WarehouseEvent extends Event implements Cancellable {

    private final Warehouse warehouse;
    private Material material;

    private boolean cancelled;

    public WarehouseEvent(@NotNull Warehouse warehouse, @NotNull Material material) {
        this.warehouse = warehouse;
        this.material = material;
    }

    /**
     * The warehouse involved in the event
     * @return Warehouse involved in the event
     */
    @NotNull
    public Warehouse getWarehouse() {
        return warehouse;
    }

    /**
     * The material involved in the event
     * @return Material involved in the event
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the material involved in the event
     * @param material New material involved in the event
     */
    public void setMaterial(@NotNull Material material) {
        this.material = material;
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
