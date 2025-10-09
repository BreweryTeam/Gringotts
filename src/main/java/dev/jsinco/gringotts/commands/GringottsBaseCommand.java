package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.subcommands.HelpCommand;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.Lang;
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
        if (args.length == 0 && sender.hasPermission("gringotts.command.base")) {
            String baseCommandBehavior = ConfigManager.get(Config.class).baseCommandBehavior();
            SubCommand subCommand = Registry.SUB_COMMANDS.get(baseCommandBehavior);
            if (subCommand == null) {
                subCommand = Registry.SUB_COMMANDS.get(HelpCommand.class);
            }
            return subCommand.execute(Gringotts.getInstance(), sender, label, new ArrayList<>());
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = Registry.SUB_COMMANDS.get(subCommandName);
        Lang lang = ConfigManager.get(Lang.class);

        if (subCommand == null) {
            lang.entry(l -> l.command().base().unknownCommand(), sender);
            return true;
        } else if (subCommand.playerOnly() && !(sender instanceof Player)) {
            lang.entry(l -> l.command().base().playerOnly(), sender);
            return true;
        } else if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission())) {
            lang.entry(l -> l.command().base().noPermission(), sender);
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
