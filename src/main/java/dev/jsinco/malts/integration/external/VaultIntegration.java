package dev.jsinco.malts.integration.external;

import com.google.common.base.Preconditions;
import dev.jsinco.malts.integration.EconomyIntegration;
import dev.jsinco.malts.utility.ClassUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration implements EconomyIntegration {

    private Economy economy;

    @Override
    public boolean canRegister() {
        return ClassUtil.classExists("net.milkbowl.vault.economy.Economy");
    }

    @Override
    public void register() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        Preconditions.checkNotNull(rsp, "Vault economy service provider not found but tried to register anyways");
        this.economy = rsp.getProvider();
    }

    @Override
    public String name() {
        return "Vault";
    }

    @Override
    public boolean withdraw(OfflinePlayer offlinePlayer, double amount) {
        EconomyResponse response = economy.withdrawPlayer(offlinePlayer, amount);
        return response.transactionSuccess();
    }

    @Override
    public boolean deposit(OfflinePlayer offlinePlayer, double amount) {
        EconomyResponse response = economy.depositPlayer(offlinePlayer, amount);
        return response.transactionSuccess();
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return economy.getBalance(offlinePlayer);
    }
}
