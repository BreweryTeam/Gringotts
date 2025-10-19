package dev.jsinco.malts.api.events;

import org.bukkit.event.Event;

/**
 * <b>Deprecated:</b> The entire Malts event system has been
 * deprecated and will be replaced by a custom event bus in a future update.
 */
@Deprecated
public abstract class MaltsEvent extends Event {

    public MaltsEvent(boolean async) {
        super(async);
    }
}
