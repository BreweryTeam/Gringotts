package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import dev.jsinco.malts.gui.WarehouseGui;
import dev.jsinco.malts.obj.MaltsPlayer;
import dev.jsinco.malts.obj.Warehouse;
import dev.jsinco.malts.storage.DataSource;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarehouseCommand implements SubCommand {

    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        Warehouse warehouse = dataSource.cachedObject(player.getUniqueId(), Warehouse.class);
        MaltsPlayer maltsPlayer = dataSource.cachedObject(player.getUniqueId(), MaltsPlayer.class);
        WarehouseGui warehouseGui = new WarehouseGui(warehouse, maltsPlayer);


        warehouseGui.open(player);
        return true;
    }

    @Override
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String name() {
        return "warehouse";
    }

    @Override
    public String permission() {
        return "malts.command.warehouse";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

}
