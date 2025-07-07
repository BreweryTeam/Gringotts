package dev.jsinco.gringotts;

import dev.jsinco.gringotts.commands.CommandManager;
import dev.jsinco.gringotts.events.PlayerListener;
import dev.jsinco.gringotts.events.VaultListener;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gringotts extends JavaPlugin {

    @Getter
    private static Gringotts instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        getServer().getPluginCommand("gringotts").setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new VaultListener(), this);
    }
}