package dev.jsinco.gringotts.api.events.warehouse;

import dev.jsinco.gringotts.api.events.interfaces.EventAction;
import dev.jsinco.gringotts.api.events.interfaces.WarehouseEvent;
import dev.jsinco.gringotts.obj.Warehouse;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a compartment is added or removed from a warehouse
 */
public class WarehouseCompartmentEvent extends WarehouseEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final EventAction action;


    public WarehouseCompartmentEvent(Warehouse warehouse, EventAction action, Material material) {
        super(warehouse, material);
        this.action = action;
    }

    /**
     * The action being performed, either ADD or REMOVE
     * @return Action being performed
     */
    @NotNull
    public EventAction getAction() {
        return action;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
