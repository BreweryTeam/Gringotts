package dev.jsinco.gringotts.utility.interfaces;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@FunctionalInterface
public interface EditMeta {
    void edit(ItemMeta meta);
}
