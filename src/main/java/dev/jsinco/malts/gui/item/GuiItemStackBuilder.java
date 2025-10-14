package dev.jsinco.malts.gui.item;

import dev.jsinco.malts.utility.ItemStacks;

@FunctionalInterface
public interface GuiItemStackBuilder {
    ItemStacks.ItemStackBuilder create(ItemStacks.ItemStackBuilder builder);
}
