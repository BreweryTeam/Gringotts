package dev.jsinco.gringotts.integration;

import dev.jsinco.gringotts.registry.RegistryItem;
import org.bukkit.plugin.java.JavaPlugin;

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

    /**
     * An external integration
     * @param <T>
     */
    interface External<T extends JavaPlugin> extends Integration {

    }
}
