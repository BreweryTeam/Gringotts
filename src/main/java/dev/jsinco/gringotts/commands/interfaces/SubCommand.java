package dev.jsinco.gringotts.commands.interfaces;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.registry.RegistryItem;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand extends RegistryItem {

    Lang lng = ConfigManager.get(Lang.class);

    boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args);

    List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args);

    String permission();

    boolean playerOnly();

}
