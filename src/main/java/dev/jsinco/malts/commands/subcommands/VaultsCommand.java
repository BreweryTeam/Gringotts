package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.gui.YourVaultsGui;
import dev.jsinco.malts.integration.EconomyIntegration;
import dev.jsinco.malts.obj.MaltsPlayer;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VaultsCommand implements SubCommand {

    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        MaltsPlayer maltsPlayer = dataSource.cachedObject(player.getUniqueId(), MaltsPlayer.class);

        if (args.isEmpty()) {
            YourVaultsGui yourVaultsGui = new YourVaultsGui(maltsPlayer);
            yourVaultsGui.open(player);
            return true;
        }

        int vaultId = Util.getInteger(args.getFirst(), 1);

        if (maltsPlayer.getCalculatedMaxVaults() < vaultId) {
            lng.entry(l -> l.vaults().noAccess(), player, Couple.of("{id}", vaultId));
            return true;
        }

        Config.Economy cfg = ConfigManager.get(Config.class).economy();

        dataSource.getVaultWithEconomy(player, vaultId, vault -> {
            vault.open(player);
            lng.entry(l -> l.vaults().opening(), player,
                    Couple.of("{id}", vaultId),
                    Couple.of("{vaultName}", vault.getCustomName())
            );
        });
        return true;
    }

    @Override
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        MaltsPlayer maltsPlayer = dataSource.cachedObject(player.getUniqueId(), MaltsPlayer.class);
        // List of vault IDs the player has access to
        return IntStream.rangeClosed(1, maltsPlayer.getCalculatedMaxVaults())
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "vaults";
    }

    @Override
    public String permission() {
        return "malts.command.vaults";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

}
