package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.gringotts.commands.subcommands.VaultsCommand;

public class PlayerVaultsBaseCommand extends SubCommandWrapper {
    public PlayerVaultsBaseCommand() {
        super(new VaultsCommand());
    }
}
