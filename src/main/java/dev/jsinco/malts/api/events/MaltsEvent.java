package dev.jsinco.malts.api.events;

import org.bukkit.event.Event;


public abstract class MaltsEvent extends Event {

    public MaltsEvent(boolean async) {
        super(async);
    }
}
