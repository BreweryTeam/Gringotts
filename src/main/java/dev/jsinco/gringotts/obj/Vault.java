package dev.jsinco.gringotts.obj;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class Vault implements GringottsInventory {

    private static final Gson GSON = Util.GSON;
    public static final Type TYPE_TOKEN = new TypeToken<List<UUID>>(){}.getType();
    private static final Config cfg = ConfigManager.get(Config.class);

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
        this.customName = cfg.vaults().defaultName().replace("{id}", String.valueOf(id));
        this.icon = Material.CHEST;
        this.trustedPlayers = new ArrayList<>();

        int size = cfg.vaults().size();

        this.inventory = Bukkit.createInventory(this, size, Text.mm(customName));
    }

    public Vault(UUID owner, int id, ItemStack[] items) {
        Preconditions.checkArgument(id > 0, "Vault ID must be greater than 0");
        this.owner = owner;
        this.id = id;
        this.customName = cfg.vaults().defaultName().replace("{id}", String.valueOf(id));
        this.icon = Material.CHEST;
        this.trustedPlayers = new ArrayList<>();


        int count = items != null ? items.length : 9;
        int size = Math.max(((count + 8) / 9) * 9, cfg.vaults().size());
        this.inventory = Bukkit.createInventory(this, size, Text.mm(customName));
        if (items != null) {
            inventory.setContents(items);
        }
    }

    public Vault(UUID owner, int id, String encodedInventory, String customName, Material icon, String trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;

        List<UUID> json = GSON.fromJson(trustedPlayers, TYPE_TOKEN);
        this.trustedPlayers = json != null ? json : new ArrayList<>();

        ItemStack[] items = null;
        if (encodedInventory != null && !encodedInventory.isEmpty()) {
            items = decodeInventory(encodedInventory);
        }

        int count = items != null ? items.length : 9;
        int size = Math.max(((count + 8) / 9) * 9, cfg.vaults().size());
        this.inventory = Bukkit.createInventory(this, size, Text.mm(customName));
        if (items != null) {
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
        var couple = this.getOpenState();
        var state = couple.a();
        var otherPlayer = couple.b();
        if (state == VaultOpenState.OPEN) {
            ConfigManager.get(Lang.class).entry(l -> l.vaults().alreadyOpen(), player);
            return;
        }

        Executors.runSync(() -> {
            player.openInventory(this.inventory);
            if (state == VaultOpenState.OPEN_BY_MOD && otherPlayer.getOpenInventory().getTopInventory().getHolder(false) instanceof Vault otherVault) {
                otherVault.update(otherPlayer);
            }
        });
    }

    public Couple<@NotNull VaultOpenState, @Nullable Player> getOpenState() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.equals(player.getOpenInventory().getTopInventory().getHolder(false))) {
                if (player.hasPermission("gringotts.mod")) {
                    return Couple.of(VaultOpenState.OPEN_BY_MOD, player);
                }
                return Couple.of(VaultOpenState.OPEN, player);
            }
        }
        return Couple.of(VaultOpenState.CLOSED, null);
    }

    public void update(Player updater) {
        Executors.delayedSync(1, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId() == updater.getUniqueId()) continue;
                Inventory inv = player.getOpenInventory().getTopInventory();
                if (this.equals(inv.getHolder(false))) {
                    inv.setContents(this.inventory.getContents());
                    Text.debug("Updated inventory for player " + player.getName() + " with vault " + this.id);
                }
            }
        });
    }

    public boolean canAccess(Player player) {
        return player.getUniqueId() == this.owner || this.trustedPlayers.contains(player.getUniqueId()) || player.hasPermission("gringotts.mod");
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid) || uuid == owner;
    }

    public boolean addTrusted(UUID uuid) {
        int cap = cfg.vaults().trustCap();
        if (trustedPlayers.size() >= cap) {
            return false;
        }
        trustedPlayers.add(uuid);
        return true;
    }

    public void removeTrusted(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    private static ItemStack[] decodeInventory(String encodedInventory) {
        byte[] itemByteArray = Base64.getDecoder().decode(encodedInventory);
        return ItemStack.deserializeItemsFromBytes(itemByteArray);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vault vault = (Vault) o;
        return id == vault.id && Objects.equals(owner, vault.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, id);
    }

    public enum VaultOpenState {
        OPEN,
        OPEN_BY_MOD,
        CLOSED;
    }
}
