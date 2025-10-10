package dev.jsinco.gringotts.integration.external;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import dev.jsinco.gringotts.integration.ProtectionIntegration;
import dev.jsinco.gringotts.utility.ClassUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TownyIntegration implements ProtectionIntegration {

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("com.palmergames.bukkit.towny.utils.PlayerCacheUtil");
    }

    @Override
    public void register() {

    }

    @Override
    public String name() {
        return "Towny";
    }

    @Override
    public boolean canAccess(@NotNull Block block, Player player) {
        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH);
    }
}
