package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.enums.Driver;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

public class ReloadCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        boolean success = true;
        try {
            ConfigManager.createTranslationConfigs();
            Registry.CONFIGS.values()
                    .stream()
                    .sorted(Comparator.comparing(OkaeriFile::isDynamicFileName))
                    .forEach(OkaeriFile::reload);

            Config.Storage storage = ConfigManager.get(Config.class).storage();
            Driver setDriver = storage.driver();
            DataSource dataSource = DataSource.getInstance();
            if (setDriver.getIdentifyingClass() != dataSource.getClass()) {
                dataSource.closeAsync().whenComplete((unused, throwable) -> {
                    DataSource.createInstance(storage);
                    lng.entry(l -> l.command().reload().newDatabaseDriverSet(), sender, Couple.of("{driver}", setDriver.toString()));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        final boolean finalSuccess = success;
        lng.entry(l -> finalSuccess ? l.command().reload().success() : l.command().reload().failed(), sender);
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "gringotts.command.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String name() {
        return "reload";
    }
}
