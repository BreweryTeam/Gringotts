package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.VaultOtherGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VaultOtherCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.isEmpty()) return false;
        Player player = (Player) sender;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args.getFirst());
        DataSource dataSource = DataSource.getInstance();

        if (args.size() < 2) {
            VaultOtherGui vaultOtherGui = GringottsGui.factory(() -> new VaultOtherGui(player, target));
            vaultOtherGui.open(player);
            return true;
        }

        int vaultId = Util.getInteger(args.get(1), 1);

        dataSource.getVault(target.getUniqueId(), vaultId).thenAccept(vault -> {
            if (vault == null) {
                lng.entry(l -> l.command().vaultOther().noVaultFound(),
                        player,
                        Couple.of("{id}", vaultId),
                        Couple.of("{name}", target.getName())
                );
                return;
            } else if (!vault.canAccess(player)) {
                lng.entry(l -> l.vaults().noAccess(), player, Couple.of("{id}", vaultId));
                return;
            }

            Executors.sync(() -> vault.open(player));
            lng.entry(l -> l.vaults().opening(),
                    player,
                    Couple.of("{id}", vaultId),
                    Couple.of("{vaultName}", vault.getCustomName())
            );
        });
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() <= 1) {
            return null;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args.getFirst());
        DataSource dataSource = DataSource.getInstance();

        /* Normally this method will not return null, but this player may not be online,
        so we'll just return nothing if that's the case. */
        GringottsPlayer gringottsPlayer = dataSource.cachedGringottsPlayer(offlinePlayer.getUniqueId());
        if (gringottsPlayer == null) {
            return Util.tryGetNextNumberArg(args.get(1));
        }
        // List of vault IDs the player has access to
        return IntStream.rangeClosed(1, gringottsPlayer.getCalculatedMaxVaults())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public String permission() {
        return "gringotts.command.vaultother";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String name() {
        return "vaultother";
    }

    @Override
    public String[] names() {
        return new String[] {"vaultother", "vaultadmin"};
    }
}
