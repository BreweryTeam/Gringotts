package dev.jsinco.gringotts.commands.interfaces;

import dev.jsinco.gringotts.Gringotts;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SubCommandWrapper implements TabExecutor {

    private final SubCommand subCommand;

    public SubCommandWrapper(SubCommand subCommand) {
        this.subCommand = subCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (subCommand.playerOnly() && !(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }
        return subCommand.execute(Gringotts.getInstance(), sender, label, List.of(args));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return subCommand.tabComplete(Gringotts.getInstance(), sender, label, List.of(args));
    }

    public String name() {
        return subCommand.name();
    }
}
