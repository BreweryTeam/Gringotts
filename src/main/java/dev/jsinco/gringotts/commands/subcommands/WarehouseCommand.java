package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.WarehouseGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarehouseCommand implements SubCommand {

    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        Warehouse warehouse = dataSource.cachedWarehouse(player.getUniqueId());
        GringottsPlayer gringottsPlayer = dataSource.cachedGringottsPlayer(player.getUniqueId());
        WarehouseGui warehouseGui = GringottsGui.factory(() -> new WarehouseGui(warehouse, gringottsPlayer));

        warehouseGui.open(player);
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String name() {
        return "warehouse";
    }

    @Override
    public String permission() {
        return "gringotts.command.warehouse";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

}
