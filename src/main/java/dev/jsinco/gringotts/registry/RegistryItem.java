package dev.jsinco.gringotts.registry;

public interface RegistryItem {
    String name();
    default String[] names() {
        return new String[]{name()};
    }
}
