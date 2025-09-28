package dev.jsinco.gringotts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

import java.util.List;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class GuiArrowItem extends OkaeriConfig {
    private String title;
    private List<String> lore;
    private int slot;
    private Material material;
}
