package dev.jsinco.gringotts.commands.subcommands;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class MaxCommand implements SubCommand {
    @Override
    public boolean execute(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 4) {
            return false;
        }
        String type = args.getFirst();
        Operation operation = Util.getEnum(args.get(1), Operation.class);
        OfflinePlayer target = Bukkit.getOfflinePlayer(args.get(2));
        int num = Util.getInteger(args.get(3), -1);
        if (num >= 0 && operation != null) {
            DataSource dataSource = DataSource.getInstance();
            dataSource.cacheObjectWithDefaultExpire(dataSource.getGringottsPlayer(target.getUniqueId())).thenAccept(gringottsPlayer -> {
                int oldMax;
                int newMax;
                int calculatedMax;

                if (type.equalsIgnoreCase("vaults")) {
                    oldMax = gringottsPlayer.getMaxVaults();
                    newMax = operation.apply(oldMax, num);
                    calculatedMax = gringottsPlayer.getCalculatedMaxVaults();
                    gringottsPlayer.setMaxVaults(newMax);
                } else if (type.equalsIgnoreCase("stock")) {
                    oldMax = gringottsPlayer.getMaxWarehouseStock();
                    newMax = operation.apply(oldMax, num);
                    calculatedMax = gringottsPlayer.getCalculatedMaxWarehouseStock();
                    gringottsPlayer.setMaxWarehouseStock(newMax);
                } else {
                    lng.entry(l -> l.command().max().invalidType(), sender);
                    return;
                }
                lng.entry(l -> l.command().max().success(),
                        sender,
                        Couple.of("{type}", type),
                        Couple.of("{name}", target.getName()),
                        Couple.of("{oldMax}", oldMax),
                        Couple.of("{newMax}", newMax),
                        Couple.of("{calculatedMax}", calculatedMax)
                );
            });
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(Gringotts plugin, CommandSender sender, String label, List<String> args) {
        return switch (args.size()) {
            case 1 -> List.of("vaults", "stock");
            case 2 -> Arrays.stream(Operation.values()).map(Enum::toString).toList();
            case 3 -> null;
            case 4 -> Util.tryGetNextNumberArg(args.get(2));
            default -> List.of();
        };
    }


    @Override
    public String permission() {
        return "gringotts.admin.max";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String name() {
        return "max";
    }

    enum Operation {
        ADD((x, y) -> x + y),
        SET((x, y) -> x),
        REMOVE((x, y) -> x - y);

        interface OperationFunction {
            int apply(int x, int y);
        }

        private final OperationFunction operation;

        Operation(OperationFunction operation) {
            this.operation = operation;
        }

        public int apply(int x, int y) {
            return operation.apply(x, y);
        }

        public String toString() {
            return name().toLowerCase();
        }
    }
}
