package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

public class Config extends OkaeriConfig {

    @CustomKey("default-max-vaults")
    @Comment({
            "The default amount of vaults players have.",
            "Players may have their vaults increased by using",
            "'gringotts.maxvaults.<amount>' permissions.",
            "Or, by using '/gringotts maxvaults <amount>' command.",
    })
    private int defaultMaxVaults = 0;
}
