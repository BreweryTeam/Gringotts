package dev.jsinco.gringotts.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

public class Text {

    public static Component mm(String m) {
        return MiniMessage.miniMessage().deserialize(m);
    }

    public static void debug(String msg) {
        Bukkit.getConsoleSender().sendMessage(mm("GRINGOTTS: " + msg));
    }
}
