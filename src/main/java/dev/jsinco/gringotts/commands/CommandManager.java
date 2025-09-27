package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.subcommands.VaultCommand;
import dev.jsinco.gringotts.commands.subcommands.VaultOpenTestCommand;
import dev.jsinco.gringotts.commands.subcommands.WarehouseCommand;
import dev.jsinco.gringotts.commands.subcommands.YourVaultsCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {

    private final Map<String, SubCommand> commands = new HashMap<>();

    public CommandManager() {
        commands.put("vault", new VaultCommand());
        commands.put("vaulttest", new VaultOpenTestCommand());
        commands.put("yourvaults", new YourVaultsCommand());
        commands.put("warehouse", new WarehouseCommand());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("Available commands: " + String.join(", ", commands.keySet()));
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = commands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage("Unknown command: " + subCommandName);
            return true;
        }

        if (subCommand.playerOnly() && !(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        List<String> commandArgs = new ArrayList<>(List.of(args));
        commandArgs.removeFirst(); // Remove the subcommand name

        return subCommand.execute(Gringotts.getInstance(), sender, label, commandArgs);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> subCommandNames = new ArrayList<>(commands.keySet());
            return subCommandNames.stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = commands.get(subCommandName);

        if (subCommand == null || subCommand.playerOnly() && !(sender instanceof Player)) {
            return null; // No tab completion for player-only commands if sender is not a player
        }

        return subCommand.tabComplete(Gringotts.getInstance(), sender, label, List.of(args));
    }
}
