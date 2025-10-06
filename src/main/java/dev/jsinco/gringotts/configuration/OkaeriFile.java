package dev.jsinco.gringotts.configuration;

import dev.jsinco.gringotts.registry.RegistryItem;
import eu.okaeri.configs.OkaeriConfig;

public class OkaeriFile extends OkaeriConfig implements RegistryItem {

    public String getFileName() {
        OkaeriFileName fileName = getClass().getAnnotation(OkaeriFileName.class);
        if (fileName == null) {
            throw new IllegalStateException("OkaeriFile must be annotated with @OkaeriFileName");
        }
        return fileName.value();
    }

    @Override
    public String name() {
        return getFileName();
    }
}
