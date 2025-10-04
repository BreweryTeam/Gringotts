package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GringottsBaseCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            // TODO: Help command
            sender.sendMessage("Available commands: " + String.join(", ", Registry.SUB_COMMANDS.keySet()));
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = Registry.SUB_COMMANDS.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage("Unknown command: " + subCommandName);
            return true;
        } else if (subCommand.playerOnly() && !(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        } else if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission())) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        List<String> commandArgs = new ArrayList<>(List.of(args));
        commandArgs.removeFirst(); // Remove the subcommand name

        return subCommand.execute(Gringotts.getInstance(), sender, label, commandArgs);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> subCommandNames = new ArrayList<>(Registry.SUB_COMMANDS.keySet());
            return subCommandNames.stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = Registry.SUB_COMMANDS.get(subCommandName);

        if (subCommand == null || (subCommand.permission() != null && !sender.hasPermission(subCommand.permission()))) {
            return null;
        }

        List<String> commandArgs = new ArrayList<>(List.of(args));
        commandArgs.removeFirst();

        return subCommand.tabComplete(Gringotts.getInstance(), sender, label, commandArgs);
    }
}
