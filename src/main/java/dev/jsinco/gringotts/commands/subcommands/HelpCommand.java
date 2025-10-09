package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.utility.Couple;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand implements SubCommand {

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        PluginMeta meta = Gringotts.getInstance().getPluginMeta();

        lng.entry(l -> l.command().help(), sender,
                Couple.of("{version}", meta.getVersion()),
                Couple.of("{description}", meta.getDescription()),
                Couple.of("{authors}", meta.getAuthors())
        );
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "gringotts.command.help";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String name() {
        return "help";
    }
}
