package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;

import static dev.jsinco.gringotts.storage.DataSource.DATA_FOLDER;

@Getter
@Accessors(fluent = true)
public class ConfigManager {

    @Getter
    private static final ConfigManager instance = new ConfigManager();

    private Config config;
    private GuiConfig guiConfig;



    private ConfigManager() {
        this.config = loadConfig(Config.class, "config.yml");
        this.guiConfig = loadConfig(GuiConfig.class, "gui.yml");
    }

    private <T extends OkaeriConfig> T loadConfig(Class<T> configClass, String fileName) {
        return eu.okaeri.configs.ConfigManager.create(configClass, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new StandardSerdes());
            it.withRemoveOrphans(false);
            it.withBindFile(DATA_FOLDER.resolve(fileName));
            it.withSerdesPack(serdes -> {
                serdes.register(new IntPairTransformer());
            });

            it.saveDefaults();
            it.load(true);
        });
    }
}
