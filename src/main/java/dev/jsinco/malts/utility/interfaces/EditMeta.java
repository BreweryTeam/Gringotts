package dev.jsinco.malts.utility.interfaces;

import org.bukkit.inventory.meta.ItemMeta;

@FunctionalInterface
public interface EditMeta {
    void edit(ItemMeta meta);
}
