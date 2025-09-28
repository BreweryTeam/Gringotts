package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class GuiConfig extends OkaeriConfig {

    private WarehouseGui warehouseGui = new WarehouseGui();

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
