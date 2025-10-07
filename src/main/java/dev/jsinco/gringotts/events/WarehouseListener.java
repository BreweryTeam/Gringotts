package dev.jsinco.gringotts.events;

import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.storage.DataSource;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class WarehouseListener implements Listener {

    private void handle(Event event, Player player) {
        GringottsPlayer gringottsPlayer = DataSource.getInstance().cachedGringottsPlayer(player.getUniqueId());
        if (gringottsPlayer == null) {
            throw new IllegalStateException("GringottsPlayer is not cached.");
        }
        gringottsPlayer.getWarehouseMode().handle(event, gringottsPlayer, player);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttemptPickupItem(PlayerAttemptPickupItemEvent event) {
        handle(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        handle(event, event.getPlayer());
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        handle(event, event.getPlayer());
    }

}
