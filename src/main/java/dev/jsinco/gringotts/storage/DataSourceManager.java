package dev.jsinco.gringotts.storage;

import dev.jsinco.gringotts.enums.Driver;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
public class DataSourceManager {

    @Getter
    private static final DataSourceManager instance;

    private final DataSource dataSource;
    private final Set<GringottsPlayer> cachedPlayers;


    private DataSourceManager() throws SQLException {
        Driver driver = Driver.SQLITE;
        this.dataSource = driver.getSupplier().get();
        this.cachedPlayers = new HashSet<>();

        // This shouldn't happen, but if it does, we want to ensure the cache is populated
        Bukkit.getOnlinePlayers().forEach(player -> {
            dataSource.getGringottsPlayer(player.getUniqueId()).thenAccept(cachedPlayers::add);
        });
    }


    @Nullable
    public GringottsPlayer cachedGringottsPlayer(UUID uuid) {
        return cachedPlayers.stream()
                .filter(player -> player.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public void cacheGringottsPlayer(UUID uuid) {
        dataSource.getGringottsPlayer(uuid).thenAccept(gringottsPlayer -> {
            GringottsPlayer newPlayer = Objects.requireNonNullElseGet(gringottsPlayer, () -> new GringottsPlayer(uuid, 0, 0, 0, new HashMap<>()));
            cachedPlayers.add(newPlayer);
        });
    }

    static {
        try {
            instance = new DataSourceManager();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
