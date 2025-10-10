package dev.jsinco.gringotts.integration.external;

import com.google.common.base.Preconditions;
import dev.jsinco.gringotts.integration.ProtectionIntegration;
import dev.jsinco.gringotts.utility.ClassUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.popcraft.bolt.BoltAPI;

public class BoltIntegration implements ProtectionIntegration {

    private BoltAPI bolt;

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("org.popcraft.bolt.BoltAPI");
    }

    @Override
    public void register() {
        this.bolt = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
    }

    @Override
    public String name() {
        return "Bolt";
    }

    @Override
    public boolean canAccess(@NotNull Block block, Player player) {
        Preconditions.checkNotNull(bolt, "Bolt API is not initialized");
        return bolt.canAccess(block, player);
    }
}
