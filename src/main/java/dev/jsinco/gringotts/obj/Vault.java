package dev.jsinco.gringotts.obj;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Vault implements InventoryHolder {

    private static Gson GSON = Util.GSON;
    private static final Type TYPE_TOKEN = new TypeToken<List<UUID>>(){}.getType();

    private final UUID owner;
    private final int id;
    private final Inventory inventory;
    private String customName;
    private List<UUID> trustedPlayers;

    public Vault(UUID owner, int id) {
        this.owner = owner;
        this.id = id;
        this.customName = "Vault #" + id;
        this.trustedPlayers = new ArrayList<>();

        this.inventory = Bukkit.createInventory(this, 54, Text.mm(customName));
    }

    public Vault(UUID owner, int id, String encodedInventory, String customName, String trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.trustedPlayers = GSON.fromJson(trustedPlayers, TYPE_TOKEN);

        this.inventory = Bukkit.createInventory(this, 54, Text.mm(customName));
        if (encodedInventory != null && !encodedInventory.isEmpty()) {
            ItemStack[] items = decodeInventory(encodedInventory);
            inventory.setContents(items);
        }
    }

    public String encodeInventory() {
        byte[] itemByteArray = ItemStack.serializeItemsAsBytes(inventory.getContents());
        return Base64.getEncoder().encodeToString(itemByteArray);
    }

    public String encodeTrusted() {
        return GSON.toJson(trustedPlayers, TYPE_TOKEN);
    }



    private static ItemStack[] decodeInventory(String encodedInventory) {
        byte[] itemByteArray = Base64.getDecoder().decode(encodedInventory);
        return ItemStack.deserializeItemsFromBytes(itemByteArray);
    }
}
