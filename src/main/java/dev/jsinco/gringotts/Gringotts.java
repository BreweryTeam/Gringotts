package dev.jsinco.gringotts;

import dev.jsinco.gringotts.commands.CommandManager;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.events.ChatPromptInputListener;
import dev.jsinco.gringotts.events.GuiListener;
import dev.jsinco.gringotts.events.PlayerListener;
import dev.jsinco.gringotts.events.VaultListener;
import dev.jsinco.gringotts.storage.DataSource;
import lombok.Getter;
import org.bukkit.entity.Player;
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
        DataSource.createInstance();

        getServer().getPluginCommand("gringotts").setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new VaultListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new ChatPromptInputListener(), this);

        DataSource dataSource = DataSource.getInstance();
        for (Player player : getServer().getOnlinePlayers()) {
            dataSource.cacheObject(dataSource.getGringottsPlayer(player.getUniqueId()));
            dataSource.cacheObject(dataSource.getWarehouse(player.getUniqueId()));
        }

        ConfigManager.instance();
    }

    @Override
    public void onDisable() {
        shutdown = true;
        DataSource dataSource = DataSource.getInstance();
        dataSource.close();
    }
}