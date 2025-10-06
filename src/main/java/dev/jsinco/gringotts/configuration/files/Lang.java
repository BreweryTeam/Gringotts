package dev.jsinco.gringotts.configuration.files;

import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.OkaeriFileName;
import eu.okaeri.configs.OkaeriConfig;

@OkaeriFileName(dynamicFileName = true, dynamicFileNameKey = "lang")
public class Lang extends OkaeriFile {

    public static class Warehouse extends OkaeriConfig {

    }
}
