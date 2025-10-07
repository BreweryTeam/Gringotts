package dev.jsinco.gringotts.configuration.files;

import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.OkaeriFileName;
import dev.jsinco.gringotts.enums.Driver;
import dev.jsinco.gringotts.enums.WarehouseMode;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
@OkaeriFileName("config.yml")
public class Config extends OkaeriFile {

    @Comment({
            "The language file to use for Gringotts.",
            "The file must be located in the 'translations' directory.",
            "If you would like to use a language file that is not provided by Gringotts,",
            "you will need to create it yourself and place it in the 'translations' directory.",
    })
    private String language = "en_us";


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
                " ",
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

    private Vaults vaults = new Vaults();
    @Getter
    @Accessors(fluent = true)
    public static class Vaults extends OkaeriConfig {
        @Comment({
                "This is the default title of all vaults for Gringotts.",
                "This does not prevent players from changing their vault",
                "names to whatever they want."
        })
        private String defaultName = "Vault #{id}";
        @Comment({
                "The size for all Gringotts vaults. This setting applies globally.",
                "If this setting is lowered and players already have vaults that fill up to 54 slots,",
                "Gringotts will dynamically increase the size of the inventory until the inventory's",
                "items have been lowered to the desired configured amount."
        })
        private int size = 54;
        @Comment({
                "The maximum amount of characters that can be",
                "used to rename a vault."
        })
        private int maxNameCharacters = 25;

        @Comment({
                "The maximum amount of players that can be trusted to a single vault",
                "(not including the owner of the vault). This setting does not apply to",
                "warehouses as warehouses have no concept of 'trusting' and can only be",
                "accessed by the owner and/or moderators who have elevated permissions."
        })
        private int trustCap = 3;

        @Comment({
                "The default amount of vaults players have.",
                "Players may have their vaults increased by using",
                "'gringotts.maxvaults.<amount>' permissions.",
                "Or, by using '/gringotts max vaults add <player> <amount>'.",
        })
        private int defaultMaxVaults = 0;
    }


    private Warehouse warehouse = new Warehouse();
    @Getter
    @Accessors(fluent = true)
    public static class Warehouse extends Vaults {

        @Comment({
                "Automatically blacklist items that have a stack <= 1."
        })
        private boolean blacklistSingleStackMaterials = true; // TODO: Implement
        @Comment({
                "Manually prevent items from being stored in warehouses.",
                "Supports regex."
        })
        private List<String> blacklistedMaterials = List.of(); // TODO: Implement
        public List<Material> blacklistedMaterials() {
            Material[] materials = Material.values();
            List<Pattern> patterns = blacklistedMaterials.stream().map(Pattern::compile).toList();
            List<Material> blacklist = new ArrayList<>();
            for (Material material : materials) {
                if (patterns.stream().anyMatch(pattern -> pattern.matcher(material.toString()).matches())) {
                    blacklist.add(material);
                }
            }
            return blacklist;
        }

        @Comment({
                "All enabled warehouse modes for Gringotts.",
                "To use a specific mode, a player must have the",
                "permission 'gringotts.warehouse.mode.<mode> (e.g. gringotts.warehouse.mode.auto_store)",
                " ",
                "* Options: AUTO_STORE, CLICK_TO_DEPOSIT, AUTO_REPLENISH"
        })
        private List<WarehouseMode> enabledModes = List.of(
                WarehouseMode.AUTO_STORE,
                WarehouseMode.CLICK_TO_DEPOSIT,
                WarehouseMode.AUTO_REPLENISH
        );


        @Comment({
                "The default amount of warehouse stock players have.",
                "Players may have their stock increased by using",
                "'gringotts.maxstock.<amount>' permissions.",
                "Or, by using '/gringotts max stock add <player> <amount>'.",
        })
        private int defaultMaxStock = 0;
    }

}
