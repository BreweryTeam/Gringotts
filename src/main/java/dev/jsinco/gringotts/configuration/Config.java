package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.ClickType;

@Getter
@Accessors(fluent = true)
public class Config extends OkaeriConfig {

    @Comment({
            "The default amount of vaults players have.",
            "Players may have their vaults increased by using",
            "'gringotts.maxvaults.<amount>' permissions.",
            "Or, by using '/gringotts maxvaults <amount>'.",
    })
    private int defaultMaxVaults = 0;

    @Comment({
            "The default amount of warehouse stock players have.",
            "Players may have their stock increased by using",
            "'gringotts.maxstock.<amount>' permissions.",
            "Or, by using '/gringotts maxstock <amount>'.",
    })
    private int defaultMaxStock = 0;


    @Comment({
            "Quick return allows players to click outside of their",
            "vaults or warehouse to return to the main 'Your Vaults'",
            "gui."
    })
    private QuickReturn quickReturn = new QuickReturn();
    @Getter
    @Accessors(fluent = true)
    public static class QuickReturn extends OkaeriConfig {
        @Comment("Whether or not to enable the quick return feature.")
        private boolean enabled = true;
        @Comment("https://jd.papermc.io/paper/1.21.9/org/bukkit/event/inventory/ClickType.html")
        private ClickType clickType = ClickType.RIGHT;
    }

}
