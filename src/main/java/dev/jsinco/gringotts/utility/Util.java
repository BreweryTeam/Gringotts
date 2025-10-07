package dev.jsinco.gringotts.utility;

import com.google.gson.Gson;
import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.utility.interfaces.EditMeta;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Util {

    public static final Gson GSON = new Gson();


    public static int getInteger(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Integer getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static <E extends Enum<E>> E getEnum(String value, Class<E> enumClass) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static NamespacedKey namespacedKey(String key) {
        return new NamespacedKey(Gringotts.getInstance(), key);
    }

    public static ItemStack editMeta(ItemStack itemStack, EditMeta editMeta) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }
        editMeta.edit(itemMeta);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static <P, C> void setPersistentKey(ItemStack item, String key, PersistentDataType<P, C> type, C value) {
        editMeta(item, meta -> {
            meta.getPersistentDataContainer().set(namespacedKey(key), type, value);
        });
    }

    public static <P, C> ItemStack setPersistentKey(ItemStack item, NamespacedKey key, PersistentDataType<P, C> type, C value) {
        editMeta(item, meta -> {
            meta.getPersistentDataContainer().set(key, type, value);
        });
        return item;
    }

    public static boolean hasPersistentKey(ItemStack item, NamespacedKey key) {
        return item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(key);
    }


    public static String formatEnumerator(Enum<?> e) {
        return formatEnumerator(e.name());
    }
    public static String formatEnumerator(String s) {
        String name = s.toLowerCase().replace("_", " ");
        name = name.substring(0, 1).toUpperCase() + name.substring(1);

        for (int i = 0; i < name.length(); i++) {
            if (name.charAt(i) == ' ' && i + 1 < name.length()) {
                name = name.substring(0, i + 1)
                        + Character.toUpperCase(name.charAt(i + 1))
                        + name.substring(i + 2);
            }
        }
        return name;
    }

    public static int getMaterialAmount(Inventory inv, Material material) {
        int amount = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null) {
                continue;
            }
            if (item.getType() == material && !item.hasItemMeta()) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public static int getAmountInvCanHold(Inventory inv, Material material) {
        int amount = 0;
        for (ItemStack item : inv.getStorageContents()) {
            if (item == null) {
                amount += material.getMaxStackSize();
            } else if (item.getType() == material && !item.hasItemMeta()) {
                amount += item.getMaxStackSize() - item.getAmount();
            }
        }
        return amount;
    }

    public static List<String> tryGetNextNumberArg(String arg) {
        int num = Util.getInteger(arg, -1);
        return IntStream.range(0, 10)
                .mapToObj(i -> num < 0 ? String.valueOf(i) : num + "" + i)
                .toList();
    }

    @SafeVarargs
    public static String replace(String string, Couple<String, Object>... pairs) {
        if (string == null) {
            return null;
        }
        String newString = string;
        for (Couple<String, Object> pair : pairs) {
            newString = newString.replace(pair.a(), String.valueOf(pair.b()));
        }
        return newString;
    }

    public static List<String> replaceAll(List<String> list, String charArray, String charArrayReplacement) {
        return list.stream().map(s -> s.replace(charArray, charArrayReplacement)).toList();
    }

    public static List<String> replaceStringWithList(List<String> list, String charArray, List<String> charArrayReplacement) {
        List<String> newList = new ArrayList<>(list);
        for (int i = 0; i < newList.size(); i++) {
            String s = newList.get(i);
            if (s.contains(charArray)) {
                newList.remove(i);
                newList.addAll(i, charArrayReplacement);
            }
        }
        return newList;
    }

    public static <T> List<String> replaceAll(List<String> list, Couple<String, T>... pairs) {
        List<String> newList = new ArrayList<>();
        for (String string : list) {
            String newString = string;
            for (Couple<String, T> pair : pairs) {
                newString = newString.replace(pair.a(), String.valueOf(pair.b()));
            }
            newList.add(newString);
        }
        return newList;
    }
}
