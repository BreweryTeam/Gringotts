package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.gringotts.commands.subcommands.VaultsCommand;

public class VaultsBaseCommand extends SubCommandWrapper {
    public VaultsBaseCommand() {
        super(new VaultsCommand());
    }
}
