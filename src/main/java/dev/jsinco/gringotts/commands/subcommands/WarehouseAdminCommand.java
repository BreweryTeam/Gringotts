package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.WarehouseGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WarehouseAdminCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.isEmpty()) return false;
        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args.getFirst());
        DataSource dataSource = DataSource.getInstance();

        CompletableFuture<GringottsPlayer> playerFuture = dataSource.cacheObject(dataSource.getGringottsPlayer(target.getUniqueId()), 180000L);
        CompletableFuture<Warehouse> warehouseFuture = dataSource.cacheObject(dataSource.getWarehouse(target.getUniqueId()), 180000L);

        CompletableFuture.allOf(playerFuture, warehouseFuture).thenRunAsync(() -> {
            GringottsPlayer gringottsPlayer = playerFuture.join();
            Warehouse warehouse = warehouseFuture.join();

            WarehouseGui warehouseGui = GringottsGui.factory(() -> new WarehouseGui(warehouse, gringottsPlayer));
            warehouseGui.open(player);
        });
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return null;
    }

    @Override
    public String permission() {
        return "gringotts.command.warehouseadmin";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String name() {
        return "warehouseadmin";
    }
}
