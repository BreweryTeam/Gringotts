package dev.jsinco.malts.commands;

import dev.jsinco.malts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.malts.commands.subcommands.VaultsCommand;

public class VaultsBaseCommand extends SubCommandWrapper {
    public VaultsBaseCommand() {
        super(new VaultsCommand());
    }
}
