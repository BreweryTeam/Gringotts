package dev.jsinco.gringotts.gui.item;

import dev.jsinco.gringotts.utility.ItemStacks;

@FunctionalInterface
public interface GuiItemStackBuilder {
    ItemStacks.ItemStackBuilder create(ItemStacks.ItemStackBuilder builder);
}
