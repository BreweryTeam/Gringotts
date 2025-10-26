package dev.jsinco.malts.enums;

import lombok.Getter;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.Nullable;

@Getter
public enum QuickReturnClickType {

    LEFT(ClickType.LEFT),
    RIGHT(ClickType.RIGHT),
    MIDDLE(ClickType.MIDDLE),
    NONE(null);

    @Nullable
    private final ClickType backing;

    QuickReturnClickType(@Nullable ClickType backing) {
        this.backing = backing;
    }
}
