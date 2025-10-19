package dev.jsinco.malts.obj;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.jsinco.malts.api.events.interfaces.EventAction;
import dev.jsinco.malts.api.events.vault.VaultIconChangeEvent;
import dev.jsinco.malts.api.events.vault.VaultNameChangeEvent;
import dev.jsinco.malts.api.events.vault.VaultOpenEvent;
import dev.jsinco.malts.api.events.vault.VaultTrustPlayerEvent;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.files.Lang;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Executors;
import dev.jsinco.malts.utility.Text;
import dev.jsinco.malts.utility.Util;
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
public class Vault implements MaltsInventory {

    public static final Type LIST_UUID_TYPE_TOKEN = new TypeToken<List<UUID>>(){}.getType();
    public static String BYPASS_OPEN_VAULT_PERM = "malts.bypass.openvault";

    private static final Gson GSON = Util.GSON;
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
        this.icon = cfg.vaults().defaultIcon();
        this.trustedPlayers = new ArrayList<>();

        int size = cfg.vaults().size();

        this.inventory = Bukkit.createInventory(this, size, Text.mm(customName));
    }

    public Vault(UUID owner, int id, ItemStack[] items) {
        Preconditions.checkArgument(id > 0, "Vault ID must be greater than 0");
        this.owner = owner;
        this.id = id;
        this.customName = cfg.vaults().defaultName().replace("{id}", String.valueOf(id));
        this.icon = cfg.vaults().defaultIcon();
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
        this.icon = icon != null && icon.isItem() ? icon : cfg.vaults().defaultIcon();

        List<UUID> json = GSON.fromJson(trustedPlayers, LIST_UUID_TYPE_TOKEN);
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
        return GSON.toJson(trustedPlayers, LIST_UUID_TYPE_TOKEN);
    }

    public void open(Player player) {
        Couple<VaultOpenState, Player> couple = this.getOpenState();
        Executors.runSync(() -> {
            //VaultOpenEvent event = new VaultOpenEvent(this, player, couple);
            //event.setCancelled();
            //event.callEvent();

//            Couple<VaultOpenState, Player> updatedCouple = event.getOpenState();
//            VaultOpenState updatedState = updatedCouple.a();
//            Player updatedOtherPlayer = updatedCouple.b();
            VaultOpenState state = couple.a();
            Player otherPlayer = couple.b();

            if (couple.a() == VaultOpenState.OPEN) {
                if (state == VaultOpenState.OPEN) {
                    ConfigManager.get(Lang.class).entry(l -> l.vaults().alreadyOpen(), player);
                }
                return;
            }


            player.openInventory(this.inventory);

            if (state == VaultOpenState.BYPASSED && otherPlayer.getOpenInventory().getTopInventory().getHolder(false) instanceof Vault otherVault) {
                otherVault.update(otherPlayer);
            }
        });
    }

    public Couple<@NotNull VaultOpenState, @Nullable Player> getOpenState() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.equals(player.getOpenInventory().getTopInventory().getHolder(false))) {
                if (player.hasPermission(BYPASS_OPEN_VAULT_PERM)) {
                    return Couple.of(VaultOpenState.BYPASSED, player);
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
        return player.getUniqueId() == this.owner || this.trustedPlayers.contains(player.getUniqueId()) || player.hasPermission(BYPASS_OPEN_VAULT_PERM);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid) || uuid == owner;
    }

    public boolean addTrusted(UUID uuid) {
        int cap = cfg.vaults().trustCap();
        //VaultTrustPlayerEvent event = new VaultTrustPlayerEvent(this, EventAction.ADD, uuid);
        //event.setCancelled();
        //event.callEvent();

        if (trustedPlayers.size() >= cap) {
            return false;
        }
        trustedPlayers.add(uuid);
        return true;
    }

    public boolean removeTrusted(UUID uuid) {
        if (!trustedPlayers.contains(uuid)) return false;
        //VaultTrustPlayerEvent event = new VaultTrustPlayerEvent(this, EventAction.REMOVE, uuid);
        //if (event.callEvent()) return false;

        trustedPlayers.remove(uuid);
        return true;
    }

    public boolean setCustomName(@NotNull String customName) {
        int maxLength = cfg.vaults().maxNameCharacters();
//        VaultNameChangeEvent event = new VaultNameChangeEvent(this, customName);
//        event.setCancelled(customName.length() > maxLength);
//        if (event.callEvent()) return false;
        if (customName.length() > maxLength) {
            return false;
        }

        this.customName = customName;
        return true;
    }

    public boolean setIcon(@NotNull Material icon) {
//        VaultIconChangeEvent event = new VaultIconChangeEvent(this, icon);
//        if (!event.callEvent()) return false;

        this.icon = icon;
        return true;
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
        BYPASSED,
        CLOSED;
    }
}
