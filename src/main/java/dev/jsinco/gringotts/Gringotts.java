package dev.jsinco.gringotts;

import dev.jsinco.gringotts.commands.GringottsBaseCommand;
import dev.jsinco.gringotts.commands.PlayerVaultsBaseCommand;
import dev.jsinco.gringotts.commands.WarehouseBaseCommand;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.events.ChatPromptInputListener;
import dev.jsinco.gringotts.events.GuiListener;
import dev.jsinco.gringotts.events.PlayerListener;
import dev.jsinco.gringotts.events.VaultListener;
import dev.jsinco.gringotts.obj.GringottsInventory;
import dev.jsinco.gringotts.storage.DataSource;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gringotts extends JavaPlugin {

    @Getter
    private static Gringotts instance;
    @Getter
    private static boolean shutdown;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        //ConfigManager.instance();
        DataSource.createInstance();

        getServer().getPluginCommand("gringotts").setExecutor(new GringottsBaseCommand());
        getServer().getPluginCommand("playervaults").setExecutor(new PlayerVaultsBaseCommand());
        getServer().getPluginCommand("warehouse").setExecutor(new WarehouseBaseCommand());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new VaultListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new ChatPromptInputListener(), this);

        DataSource dataSource = DataSource.getInstance();
        for (Player player : getServer().getOnlinePlayers()) {
            dataSource.cacheObject(dataSource.getGringottsPlayer(player.getUniqueId()));
            dataSource.cacheObject(dataSource.getWarehouse(player.getUniqueId()));
        }
    }

    @Override
    public void onDisable() {
        shutdown = true;
        DataSource dataSource = DataSource.getInstance();
        dataSource.close();

        for (Player player : getServer().getOnlinePlayers()) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv.getHolder(false) instanceof GringottsInventory) {
                player.closeInventory();
            }
        }
    }
}