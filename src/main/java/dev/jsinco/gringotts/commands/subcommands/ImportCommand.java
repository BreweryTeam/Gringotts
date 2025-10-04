package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.importers.Importer;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImportCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.isEmpty()) {
            return false;
        }
        String importerName = args.getFirst();
        Importer importer = Registry.IMPORTERS.get(importerName);

        if (importer == null || !importer.canImport()) {
            sender.sendMessage("Cannot import vaults from " + importerName + ". (Is it installed and/or enabled?)");
            return true;
        }


        sender.sendMessage("Importing vaults from " + importerName + "...");
        importer.importAll().thenAccept(results -> {
            Map<UUID, Importer.Result> failed = results.entrySet().stream()
                    .filter(e -> e.getValue() != Importer.Result.SUCCESS) // change method if different
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Text.debug("Imported " + results.size() + " vaults with " + failed.size() + " failures.");
            sender.sendMessage("Imported " + results.size() + " vaults with " + failed.size() + " failures.");

            if (!failed.isEmpty()) {
                failed.forEach((uuid, r) -> {
                    Text.debug("Failed: " + uuid + " -> " + r);
                    sender.sendMessage("Failed: " + uuid + " -> " + r);
                });
            }
        });


        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return List.copyOf(Registry.IMPORTERS.keySet());
    }

    @Override
    public String name() {
        return "import";
    }

    @Override
    public String permission() {
        return "gringotts.command.import";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }
}
