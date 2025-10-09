package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.gui.GringottsGui;
import dev.jsinco.gringotts.gui.YourVaultsGui;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VaultsCommand implements SubCommand {

    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        GringottsPlayer gringottsPlayer = dataSource.cachedObject(player.getUniqueId(), GringottsPlayer.class);

        if (args.isEmpty()) {
            YourVaultsGui yourVaultsGui = new YourVaultsGui(gringottsPlayer);
            yourVaultsGui.open(player);
            return true;
        }

        int vaultId = Util.getInteger(args.getFirst(), 1);

        if (gringottsPlayer.getCalculatedMaxVaults() < vaultId) {
            lng.entry(l -> l.vaults().noAccess(), player, Couple.of("{id}", vaultId));
            return true;
        }

        dataSource.getVault(player.getUniqueId(), vaultId).thenAccept(qVault -> {
            Vault vault = Objects.requireNonNullElseGet(qVault, () -> new Vault(player.getUniqueId(), vaultId));
            Executors.sync(() -> vault.open(player));
            lng.entry(l -> l.vaults().opening(), player,
                    Couple.of("{id}", vaultId),
                    Couple.of("{vaultName}", vault.getCustomName())
            );
        });
        return true;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        GringottsPlayer gringottsPlayer = dataSource.cachedObject(player.getUniqueId(), GringottsPlayer.class);
        // List of vault IDs the player has access to
        return IntStream.rangeClosed(1, gringottsPlayer.getCalculatedMaxVaults())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "vaults";
    }

    @Override
    public String permission() {
        return "gringotts.command.vaults";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

}
