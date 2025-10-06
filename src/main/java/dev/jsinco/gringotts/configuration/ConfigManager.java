package dev.jsinco.gringotts.configuration;

import dev.jsinco.gringotts.configuration.serdes.IntPairTransformer;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.registry.RegistryCrafter;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;

import static dev.jsinco.gringotts.storage.DataSource.DATA_FOLDER;

@Getter
@Accessors(fluent = true)
public class ConfigManager {

    public static <T extends OkaeriFile> T get(Class<T> clazz) {
        return Registry.CONFIGS.values().stream()
                .filter(it -> it.getClass().equals(clazz))
                .map(it -> (T) it)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No config found for class " + clazz.getName()));
    }

    public static class ConfigCrafter implements RegistryCrafter.Extension<OkaeriConfig> {
        @Override
        public <T extends OkaeriConfig> T craft(Class<?> clazz) {
            OkaeriFileName annotation = clazz.getAnnotation(OkaeriFileName.class);
            if (annotation == null) {
                throw new IllegalStateException("OkaeriFile must be annotated with @OkaeriFileName");
            }

            String fileName = annotation.dynamicFileName() ? dynamicFileName(annotation) : annotation.value();

            return eu.okaeri.configs.ConfigManager.create((Class<T>) clazz, (it) -> {
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

        private String dynamicFileName(OkaeriFileName annotation) {
            OkaeriFile config = ConfigManager.get(annotation.dynamicFileNameHolder());
            return config.get(annotation.dynamicFileNameKey(), String.class);
        }
    }

    public static class Translations {
        public void createTranslationConfigs() {

        }
    }
}
