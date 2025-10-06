package dev.jsinco.gringotts.configuration.files;

import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.OkaeriFileName;
import dev.jsinco.gringotts.enums.Driver;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.event.inventory.ClickType;

@Getter
@Accessors(fluent = true)
@OkaeriFileName("config.yml")
public class Config extends OkaeriFile {

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
            "The maximum amount of players that can be trusted to a single vault",
            "(not including the owner of the vault). This setting does not apply to",
            "warehouses as warehouses have no concept of 'trusting' and can only be",
            "accessed by the owner and/or moderators who have elevated permissions."
    })
    private int vaultTrustCap = 3;


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

    @Comment({
            "These are the Gringotts storage settings. All tables are prefixed",
            "with 'gringotts_' on both SQLite and MySQL to ensure uniqueness."
    })
    private Storage storage = new Storage();
    @Getter
    @Accessors(fluent = true)
    public static class Storage extends OkaeriConfig {
        @Comment({
                "How long Gringotts should keep cacheable objects in",
                "memory before saving them back to disk. The default value",
                "is fine for most people."
        })
        private long defaultObjectCacheTime = 1200000;
        @Comment({
                "The driver to use for storing data. Gringotts provides no",
                "methods for swapping storage drivers. If you would like to",
                "change storage methods, you will need to do so manually using",
                "SQL editors or phpMyAdmin for importing & exporting.",
                "",
                "* Your available options are: SQLITE, MYSQL",
                "If you choose to use MYSQL as your storage method",
                "you must configure your connection details."
        })
        private Driver driver = Driver.SQLITE;

        @Comment("The name of the database. When using SQLite, this becomes the file name.")
        private String database = "gringotts";

        private String host = "localhost";

        private int port = 3306;

        private String username = "root";

        private String password = "password";
    }
}
