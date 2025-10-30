package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class EditVaultCommand implements SubCommand {
    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
        return false;
    }

    @Override
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String name() {
        return "";
    }
    // TODO: Implement
}
