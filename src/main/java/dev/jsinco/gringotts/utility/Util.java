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

    public static String formatMaterialName(String s) {
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
}
