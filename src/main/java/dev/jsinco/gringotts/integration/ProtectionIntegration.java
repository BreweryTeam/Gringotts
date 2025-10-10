package dev.jsinco.gringotts.integration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface ProtectionIntegration extends Integration {
    boolean canAccess(@NotNull Block block, Player player);
}
