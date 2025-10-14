package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.OkaeriFile;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.enums.Driver;
import dev.jsinco.malts.registry.Registry;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

public class ReloadCommand implements SubCommand {
    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
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
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "malts.command.reload";
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
