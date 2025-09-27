package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.SubCommand;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.YourVaultsGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class YourVaultsCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        try {
            GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(player.getUniqueId());

            YourVaultsGui yourVaultsGui = GringottsGui.factory(() -> new YourVaultsGui(gringottsPlayer));
            yourVaultsGui.promiseInventory().thenAcceptAsync(inv -> {
                try {
                    Executors.sync(() -> {
                        player.openInventory(inv);
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage() {
        return "/<command> yourvaults";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
