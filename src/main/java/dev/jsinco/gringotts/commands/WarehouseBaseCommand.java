package dev.jsinco.gringotts.commands;

import dev.jsinco.gringotts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.gringotts.commands.subcommands.WarehouseCommand;

public class WarehouseBaseCommand extends SubCommandWrapper {
    public WarehouseBaseCommand() {
        super(new WarehouseCommand());
    }
}
