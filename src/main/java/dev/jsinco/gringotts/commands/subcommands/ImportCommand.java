package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.importers.Importer;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.utility.Couple;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.jsinco.gringotts.utility.Text.CONSOLE;

public class ImportCommand implements SubCommand {

    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.isEmpty()) {
            return false;
        }
        String importerName = args.getFirst();
        Importer importer = Registry.IMPORTERS.get(importerName);

        if (importer == null || !importer.canImport()) {
            lng.entry(l -> l.command()._import().cannotImport(),
                    sender,
                    Couple.of("{importer}", importerName)
            );
            return true;
        }


        lng.entry(l -> l.command()._import().startImport(),
                List.of(sender, CONSOLE),
                Couple.of("{importer}", importerName)
        );
        importer.importAll().thenAccept(results -> {
            Map<UUID, Importer.Result> failed = results.entrySet().stream()
                    .filter(e -> e.getValue() != Importer.Result.SUCCESS) // change method if different
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            lng.entry(l -> l.command()._import().importComplete(),
                    List.of(sender, CONSOLE),
                    Couple.of("{amount}", results.size()),
                    Couple.of("{failedAmount}", failed.size())
            );

            if (!failed.isEmpty()) {
                failed.forEach((uuid, r) -> {
                    lng.entry(l -> l.command()._import().failedImport(),
                            List.of(sender, CONSOLE),
                            Couple.of("{uuid}", uuid),
                            Couple.of("{result}", r)
                    );
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
