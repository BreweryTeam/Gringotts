package dev.jsinco.gringotts.obj;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    private String customName;
    @NotNull
    private Material icon;
    private List<UUID> trustedPlayers;

    public Vault(UUID owner, int id) {
        Preconditions.checkArgument(id > 0, "Vault ID must be greater than 0");
        this.owner = owner;
        this.id = id;
        this.customName = "Vault #" + id;
        this.icon = Material.CHEST;
        this.trustedPlayers = new ArrayList<>();

        this.inventory = Bukkit.createInventory(this, 54, Text.mm(customName));
    }

    public Vault(UUID owner, int id, String encodedInventory, String customName, Material icon, String trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;

        List<UUID> json = GSON.fromJson(trustedPlayers, TYPE_TOKEN);
        this.trustedPlayers = json != null ? json : new ArrayList<>();

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

    public void open(Player player) {
        if (!this.isVaultOpen()){
            Executors.runSync(() -> player.openInventory(inventory));
        } else {
            player.sendMessage("This vault is currently open by another player.");
        }
    }

    // TODO: I can't figure out a good solution to having player inventory open at the same time as another player.
    // TODO: make it so that when an admin or mod opens a vault it closes for the player that has it open.
    public boolean isVaultOpen() {
        return !inventory.getViewers()
                .stream()
                .filter(viewer ->
                        // Check if the viewer is a mod or admin viewing another player's vault
                    !viewer.hasPermission("gringotts.viewothers")
                            && viewer.getUniqueId() != this.owner
                            && !this.trustedPlayers.contains(viewer.getUniqueId())
                )
                .toList()
                .isEmpty();
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public void addTrusted(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void removeTrusted(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    private static ItemStack[] decodeInventory(String encodedInventory) {
        byte[] itemByteArray = Base64.getDecoder().decode(encodedInventory);
        return ItemStack.deserializeItemsFromBytes(itemByteArray);
    }
}
