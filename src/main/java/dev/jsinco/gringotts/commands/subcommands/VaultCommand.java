package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.SubCommand;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VaultCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        GringottsPlayer gringottsPlayer = dataSource.cachedGringottsPlayer(player.getUniqueId());

        int vaultId = Util.getInteger(args.getFirst(), 1);

        if (gringottsPlayer.getCalculatedMaxVaults() < vaultId) {
            player.sendMessage("You do not have access to vault " + vaultId + ".");
            return true;
        }

        dataSource.getVault(player.getUniqueId(), vaultId).thenAccept(qVault -> {
            Vault vault = Objects.requireNonNullElseGet(qVault, () -> new Vault(player.getUniqueId(), vaultId));
            Executors.sync(() -> {
                player.openInventory(vault.getInventory());
            });

            player.sendMessage("Opening vault " + vaultId + "...");
        });


        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        GringottsPlayer gringottsPlayer = dataSource.cachedGringottsPlayer(player.getUniqueId());
        // List of vault IDs the player has access to
        return IntStream.rangeClosed(1, gringottsPlayer.getCalculatedMaxVaults())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage() {
        return "/<command> vault <vault-id>";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }
}
