package dev.jsinco.gringotts.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.stream.Stream;

public class Text {

    public static Component mm(String m) {
        return MiniMessage.miniMessage().deserialize(m);
    }

    public static Component mmNoItalic(String m) {
        return mm(m).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> mmlNoItalic(String... ms) {
        return Stream.of(ms).map(
                m -> mm(m).decoration(TextDecoration.ITALIC, false)
        ).toList();
    }

    public static List<Component> mmlNoItalic(List<String> ms) {
        return ms.stream().map(
                m -> mm(m).decoration(TextDecoration.ITALIC, false)
        ).toList();
    }

    public static List<Component> mmlNoItalic(List<String> ms, TextColor colorIfAbsent) {
        return ms.stream().map(
                m -> mm(m).decoration(TextDecoration.ITALIC, false).color(colorIfAbsent)
        ).toList();
    }

    public static Title title(String title, String subtitle) {
        return Title.title(mm(title), mm(subtitle));
    }

    public static void debug(String msg) {
        Bukkit.getConsoleSender().sendMessage(mm("GRINGOTTS: " + msg));
    }
}
