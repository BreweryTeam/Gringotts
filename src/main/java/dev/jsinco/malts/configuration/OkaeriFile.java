package dev.jsinco.malts.configuration;

import dev.jsinco.malts.registry.RegistryItem;
import eu.okaeri.configs.OkaeriConfig;

import java.nio.file.Path;

import static dev.jsinco.malts.storage.DataSource.DATA_FOLDER;

public class OkaeriFile extends OkaeriConfig implements RegistryItem {

    public String getFileName() {
        return getFileName(true);
    }

    public String getFileName(boolean dynamicFileName) {
        OkaeriFileName annotation = getClass().getAnnotation(OkaeriFileName.class);
        if (annotation == null) {
            throw new IllegalStateException("OkaeriFile must be annotated with @OkaeriFileName");
        }

        return annotation.dynamicFileName() && dynamicFileName ? dynamicFileName(annotation) : annotation.value();
    }

    private String dynamicFileName(OkaeriFileName annotation) {
        OkaeriFile config = ConfigManager.get(annotation.dynamicFileNameHolder());
        String value = config.get(annotation.dynamicFileNameKey(), String.class);
        if (value != null) {
            return String.format(annotation.dynamicFileNameFormat(), value);
        }
        return null;
    }

    public boolean isDynamicFileName() {
        OkaeriFileName annotation = getClass().getAnnotation(OkaeriFileName.class);
        return annotation != null && annotation.dynamicFileName();
    }

    public void reload() {
        Path bindFile = DATA_FOLDER.resolve(this.getFileName(true));
        if (!this.getBindFile().equals(bindFile)) {
            this.setBindFile(bindFile);
        }
        this.load(true);
    }

    @Override
    public String name() {
        return getFileName(false);
    }
}
