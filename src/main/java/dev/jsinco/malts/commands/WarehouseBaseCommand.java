package dev.jsinco.malts.commands;

import dev.jsinco.malts.commands.interfaces.SubCommandWrapper;
import dev.jsinco.malts.commands.subcommands.WarehouseCommand;

public class WarehouseBaseCommand extends SubCommandWrapper {
    public WarehouseBaseCommand() {
        super(new WarehouseCommand());
    }
}
