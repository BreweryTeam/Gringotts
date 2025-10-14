package dev.jsinco.malts.integration.external;

import com.google.common.base.Preconditions;
import com.griefcraft.lwc.LWC;
import dev.jsinco.malts.integration.ProtectionIntegration;
import dev.jsinco.malts.utility.ClassUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LWCIntegration implements ProtectionIntegration {

    private LWC lwc;

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("com.griefcraft.lwc.LWC");
    }

    @Override
    public void register() {
        this.lwc = LWC.getInstance();
    }

    @Override
    public String name() {
        return "LWC";
    }


    @Override
    public boolean canAccess(@NotNull Block block, Player player) {
        Preconditions.checkNotNull(lwc, "LWC instance is not initialized");
        return lwc.canAccessProtection(player, block);
    }
}
