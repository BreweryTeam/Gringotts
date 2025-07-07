package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.Gringotts;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager implements TabExecutor {

    private final Map<String, SubCommand> commands = new HashMap<>();

    public CommandManager() {
        commands.put("vault", new VaultOpenTestCommand());
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

        if (subCommand != null) {
            return subCommand.tabComplete(Gringotts.getInstance(), sender, label, List.of(args));
        }

        return null; // No tab completion for unknown commands
    }
}
