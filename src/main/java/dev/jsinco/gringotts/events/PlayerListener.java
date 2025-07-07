package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.storage.DataSourceManager;
import dev.jsinco.gringotts.utility.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Text.debug("Caching player data for " + event.getPlayer().getName());
        DataSourceManager dataSourceManager = DataSourceManager.getInstance();
        dataSourceManager.cacheGringottsPlayer(event.getPlayer().getUniqueId());
    }


}
