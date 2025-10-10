package dev.jsinco.gringotts.integration.bstats;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.integration.Integration;
import dev.jsinco.gringotts.storage.DataSource;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class BStatsIntegration implements Integration.Compiled {

    private static final int BSTATS_ID = 27527;

    private Metrics metrics;

    @Override
    public String name() {
        return "bStats";
    }

    @Override
    public void register() {
        this.metrics = new Metrics(Gringotts.getInstance(), BSTATS_ID);

        DataSource dataSource = DataSource.getInstance();
        Config config = ConfigManager.get(Config.class);

        dataSource.getTotalVaultCount().thenAccept(count ->
                metrics.addCustomChart(new SingleLineChart("vault_count", () -> count))
        );
        dataSource.getTotalWarehouseStockCount().thenAccept(count ->
                metrics.addCustomChart(new SingleLineChart("warehouse_stock", () -> count))
        );

        metrics.addCustomChart(new SimplePie("storage_driver", () -> config.storage().driver().toString()));
    }
}
