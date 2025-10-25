package dev.jsinco.malts.integration.external;

import dev.jsinco.malts.integration.EconomyIntegration;
import dev.jsinco.malts.utility.ClassUtil;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;

public class PlayerPointsIntegration implements EconomyIntegration {

    private PlayerPointsAPI playerPointsAPI;

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("org.black_ixx.playerpoints.PlayerPoints");
    }

    @Override
    public void register() {
        this.playerPointsAPI = PlayerPoints.getInstance().getAPI();
    }

    @Override
    public boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        return playerPointsAPI.take(offlinePlayer.getUniqueId(), (int) amount);
    }

    @Override
    public boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        return playerPointsAPI.give(offlinePlayer.getUniqueId(), (int) amount);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return playerPointsAPI.look(offlinePlayer.getUniqueId());
    }

    @Override
    public String name() {
        return "PlayerPoints";
    }
}
