package dev.jsinco.malts.integration.external;

import dev.jsinco.malts.integration.Integration;
import org.bukkit.entity.Player;
import dev.jsinco.malts.utility.ClassUtil;
import io.papermc.paper.block.TileStateInventoryHolder;
import net.coreprotect.listener.player.InventoryChangeListener;

public class CoreProtectIntegration implements Integration {

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("net.coreprotect.listener.player.InventoryChangeListener");
    }

    @Override
    public void register() {
    }

    @Override
    public String name() {
        return "CoreProtect";
    }

    public void logContainer(Player player, TileStateInventoryHolder container) {
        InventoryChangeListener.inventoryTransaction(player.getName(), container.getLocation(), container.getInventory().getContents());
    }
}
