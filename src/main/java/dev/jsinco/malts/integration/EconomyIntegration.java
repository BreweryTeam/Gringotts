package dev.jsinco.malts.integration;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface EconomyIntegration extends Integration {

    String BYPASS_ECONOMY_PERMISSION = "malts.bypass.economy";

    /**
     * Attempts to withdraw the specified amount of currency from
     * the economy for the given player.
     * @param offlinePlayer The player to withdraw from.
     * @param amount The amount to withdraw. May be casted to int depending on implementation.
     * @return true if successful, false otherwise.
     */
    boolean withdraw(OfflinePlayer offlinePlayer, double amount);

    /**
     * Attempts to deposit the specified amount of currency into the economy
     * for the given player.
     * @param offlinePlayer The player to deposit to.
     * @param amount The amount to deposit. May be casted to int depending on implementation.
     * @return true if successful, false otherwise.
     */
    boolean deposit(OfflinePlayer offlinePlayer, double amount);


    /**
     * Gets the balance of the specified player.
     * @param offlinePlayer The player to get the balance of.
     * @return The balance of the player.
     */
    double getBalance(OfflinePlayer offlinePlayer);


    /**
     * Same implementation as {@link #withdraw(OfflinePlayer, double)}, but
     * does a permission check before attempting to withdraw.
     * @param player The player to withdraw from.
     * @param amount The amount to withdraw.
     * @return true if successful, false otherwise.
     */
    default boolean withdrawOrBypass(Player player, double amount) {
        if (player.hasPermission(BYPASS_ECONOMY_PERMISSION)) {
            return true;
        }
        return withdraw(player, amount);
    }
}
