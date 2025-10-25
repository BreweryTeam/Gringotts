package dev.jsinco.malts;

import dev.jsinco.malts.commands.MaltsBaseCommand;
import dev.jsinco.malts.commands.VaultsBaseCommand;
import dev.jsinco.malts.commands.WarehouseBaseCommand;
import dev.jsinco.malts.events.ChatPromptInputListener;
import dev.jsinco.malts.events.GuiListener;
import dev.jsinco.malts.events.PlayerListener;
import dev.jsinco.malts.events.VaultListener;
import dev.jsinco.malts.events.WarehouseListener;
import dev.jsinco.malts.obj.MaltsInventory;
import dev.jsinco.malts.storage.DataSource;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;


public final class Malts extends JavaPlugin {

    @Getter
    private static Malts instance;
    @Getter
    private static boolean shutdown;



    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        DataSource.createInstance();

        getServer().getPluginCommand("malts").setExecutor(new MaltsBaseCommand());
        getServer().getPluginCommand("vaults").setExecutor(new VaultsBaseCommand());
        getServer().getPluginCommand("warehouse").setExecutor(new WarehouseBaseCommand());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new VaultListener(), this);
        getServer().getPluginManager().registerEvents(new WarehouseListener(), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new ChatPromptInputListener(), this);
    }

    @Override
    public void onDisable() {
        shutdown = true;
        DataSource dataSource = DataSource.getInstance();


        for (Player player : getServer().getOnlinePlayers()) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv.getHolder(false) instanceof MaltsInventory) {
                player.closeInventory();
            }
        }

        dataSource.close();
    }
}