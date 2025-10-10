package dev.jsinco.gringotts.integration.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.jsinco.gringotts.integration.ProtectionIntegration;
import dev.jsinco.gringotts.utility.ClassUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class WorldGuardIntegration implements ProtectionIntegration {


    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
    }

    @Override
    public void register() {

    }

    @Override
    public String name() {
        return "WorldGuard";
    }


    @Override
    public boolean canAccess(@NotNull Block block, Player player) {
        ProtectedRegion region = getRegionAtLocation(block.getLocation());
        if (region == null) {
            // No region, default behavior (usually allows access)
            return true;
        }

        StateFlag.State chestAccessFlag = region.getFlag(Flags.CHEST_ACCESS);
        WorldGuardPlugin worldGuard = WorldGuardPlugin.inst();
        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        boolean member = region.isOwner(localPlayer) || region.isMember(localPlayer);

        if (chestAccessFlag == StateFlag.State.ALLOW) {
            return true;
        }
        return member;
    }

    @Nullable
    public ProtectedRegion getRegionAtLocation(Location location) {
        com.sk89q.worldedit.util.Location wgLocation = BukkitAdapter.adapt(location);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(wgLocation);

        //return region with highest priority
        return set.getRegions().stream()
                .max(Comparator.comparingInt(ProtectedRegion::getPriority))
                .orElse(null);
    }
}
