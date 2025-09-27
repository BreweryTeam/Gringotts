package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {




    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Text.debug("Caching player data for " + event.getPlayer().getName());
        DataSource dataSource = DataSource.getInstance();
        dataSource.cacheObject(dataSource.getGringottsPlayer(player.getUniqueId()));
        dataSource.cacheObject(dataSource.getWarehouse(player.getUniqueId()));
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Text.debug("Uncaching player data for " + player.getName());
        DataSource dataSource = DataSource.getInstance();
        dataSource.uncacheObject(player.getUniqueId(), GringottsPlayer.class);
        dataSource.uncacheObject(player.getUniqueId(), Warehouse.class);
    }


}
