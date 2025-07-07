package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.Gringotts;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args);

    List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args);

    String getPermission();

    String getUsage();

    boolean playerOnly();
}
