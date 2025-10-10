package dev.jsinco.gringotts.integration;

import dev.jsinco.gringotts.registry.RegistryItem;

public interface Integration extends RegistryItem {

    boolean canRegister();

    void register();


    /**
     * An integration compiled into Gringotts
     */
    interface Compiled extends Integration {
        @Override
        default boolean canRegister() {
            return true;
        }
    }
}
