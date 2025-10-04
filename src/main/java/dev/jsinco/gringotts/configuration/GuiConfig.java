package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

import java.util.List;

@Getter
@Accessors(fluent = true)
@SuppressWarnings("FieldMayBeFinal")
public class GuiConfig extends OkaeriConfig {

    @Comment("'altSlot' is used when the warehouse quickbar is not present.")
    private YourVaultsGui yourVaultsGui = new YourVaultsGui();
    private WarehouseGui warehouseGui = new WarehouseGui();
    private VaultGui vaultGui = new VaultGui();

    @Getter
    @Accessors(fluent = true)
    public static class VaultGui extends OkaeriConfig {
        @Comment({
                "This is the default title of all vaults for Gringotts.",
                "This does not prevent players from changing their vault",
                "names to whatever they want."
        })
        private String title = "Vault #{id}";
        @Comment({
                "The size of all vaults for Gringotts."
        })
        private int size = 54;
    }

    @Getter
    @Accessors(fluent = true)
    public static class YourVaultsGui extends OkaeriConfig {
        private String title = "Your Vaults";
        private int size = 54;

        private VaultItem vaultItem = new VaultItem();
        private WarehouseQuickbar warehouseQuickbar = new WarehouseQuickbar();
        private YourVaultsGuiArrowItem previousPage = new YourVaultsGuiArrowItem("Previous Page", List.of(), 39, 48, Material.ARROW);
        private YourVaultsGuiArrowItem nextPage = new YourVaultsGuiArrowItem("Next Page", List.of(), 41, 50, Material.ARROW);

        @Getter
        @Accessors(fluent = true)
        @AllArgsConstructor
        public static class YourVaultsGuiArrowItem extends OkaeriConfig {
            private String title;
            private List<String> lore;
            private int slot;
            private int altSlot;
            private Material material;
        }

        @Getter
        @Accessors(fluent = true)
        public static class VaultItem extends OkaeriConfig {
            private String title = "<aqua>{name}";
            private List<String> lore = List.of(
                    "<white>Left-click to open",
                    "<white>this vault.",
                    "",
                    "<white>Right-click to open",
                    "<white>your vault settings."
            );
            private IntPair slots = IntPair.of(0, 35);
            private IntPair altSlots = IntPair.of(0, 45);
            private List<Integer> ignoredSlots = List.of();
            private List<Integer> altIgnoredSlots = List.of();
        }

        @Getter
        @Accessors(fluent = true)
        public static class WarehouseQuickbar extends OkaeriConfig {
            private String title = "<aqua>View all Warehouse";
            private List<String> lore = List.of("<white>Click to view your warehouse");

            private int slot = 45;
            private Material material = Material.ENDER_CHEST;
            private IntPair slots = IntPair.of(46, 53);
            private List<Integer> ignoredSlots = List.of();
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class WarehouseGui extends OkaeriConfig {
        private String title = "Your Warehouse";
        private int size = 45;

        private WarehouseItem warehouseItem = new WarehouseItem();
        private ManagerButton managerButton = new ManagerButton();
        private GuiArrowItem previousPage = new GuiArrowItem("Previous Page", List.of(), 39, Material.ARROW);
        private GuiArrowItem nextPage = new GuiArrowItem("Next Page", List.of(), 41, Material.ARROW);

        @Getter
        @Accessors(fluent = true)
        public static class WarehouseItem extends OkaeriConfig {
            private String title = "<aqua><b>{material}";
            private List<String> lore = List.of(
                    "",
                    "<gray>+ Quantity: <yellow>{quantity}</yellow>",
                    "",
                    "<gray><dark_gray>[<gold>Left Click</gold>]</dark_gray> Withdraw one item.",
                    "<gray><dark_gray>[<gold>Right Click</gold>]</dark_gray> Withdraw a stack.",
                    "<gray><dark_gray>[<gold>Shift Left</gold>]</dark_gray> Withdraw all.",
                    "<gray><dark_gray>[<gold>Shift Right</gold>]</dark_gray> Add all to storage."
            );
            private IntPair slots = IntPair.of(10, 34);
            private List<Integer> ignoredSlots = List.of(17, 18, 26, 27);
        }

        @Getter
        @Accessors(fluent = true)
        public static class ManagerButton extends OkaeriConfig {
            private String title = "<aqua><b>Manage Warehouse";
            private List<String> lore = List.of(
                    "",
                    "<gray><dark_gray>[<gold>Left Click</gold>]</dark_gray> Add a compartment.",
                    "<gray><dark_gray>[<gold>Right Click</gold>]</dark_gray> Remove a compartment."
            );
            private int slot = 40;
            private Material material = Material.CHEST_MINECART;
        }


    }
}
