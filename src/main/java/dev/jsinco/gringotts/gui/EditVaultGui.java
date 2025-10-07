package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.events.ChatPromptInputListener.ChatInputCallback;
import dev.jsinco.gringotts.gui.item.AutoRegisterGuiItems;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.gui.item.UncontainedGuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AutoRegisterGuiItems
public class EditVaultGui extends GringottsGui {

    private static final GuiConfig.EditVaultGui cfg = ConfigManager.get(GuiConfig.class).editVaultGui();
    private static final Lang lng = ConfigManager.get(Lang.class);

    private Vault vault;
    private GringottsPlayer gringottsPlayer;
    private Player p;

    private final GuiItem backButton = GuiItem.builder()
            .index(() -> cfg.backButton().slot())
            .itemStack(b -> b
                    .displayName(cfg.backButton().name())
                    .material(cfg.backButton().material())
                    .lore(cfg.backButton().lore())
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();
                YourVaultsGui yourVaultsGui = GringottsGui.factory(() -> new YourVaultsGui(gringottsPlayer));
                yourVaultsGui.open(player);
            })
            .build();
    @SuppressWarnings("unchecked")
    private final GuiItem editNameButton = GuiItem.builder()
            .index(() -> cfg.editNameButton().slot())
            .itemStack(b -> b
                    .stringReplacements(
                            Couple.of("{vaultName}", vault.getCustomName()),
                            Couple.of("{id}", String.valueOf(vault.getId()))
                    )
                    .displayName(cfg.editNameButton().name())
                    .material(cfg.editNameButton().material())
                    .lore(cfg.editNameButton().lore())
            )
            .action(event -> {
                ItemStack clickedItem = event.getCurrentItem();
                Player player = (Player) event.getWhoClicked();
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

                ChatInputCallback.of(
                        player,
                        Text.title("<red><b>Enter text", "Enter in chat"),
                        "Enter a new vault name in chat, type 'cancel' to cancel.",
                        input -> {
                            vault.setCustomName(input);
                            DataSource.getInstance().saveVault(vault);

                            player.sendMessage("Vault name changed to: \"" + input + "\"");
                            Util.editMeta(clickedItem, meta -> {
                                meta.lore(Text.mmlNoItalic(Util.replaceAll(cfg.editNameButton().lore(), "{vaultName}", vault.getCustomName()), NamedTextColor.WHITE));
                            });
                            open(player);
                        },
                        () -> open(player)
                );
            })
            .build();

    private final UncontainedGuiItem editIconButton = UncontainedGuiItem.builder()
            .index(() -> cfg.editIconButton().slot())
            .itemStack(b -> b
                    .displayName(cfg.editIconButton().name())
                    .material(cfg.editIconButton().material())
                    .lore(cfg.editIconButton().lore())
            )
            .action((event, self, isClicked) -> {
                ItemStack clickedItem = event.getCurrentItem();
                ItemStack iconItem = Arrays.stream(event.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(item -> Util.hasPersistentKey(item, self.key()))
                        .findFirst().orElse(null);


                boolean currentValue = iconItem.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) != null ? iconItem.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) : false;
                Inventory clickedInventory = event.getClickedInventory();


                if (isClicked) {
                    iconItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, !currentValue);
                } else if (currentValue && clickedInventory != event.getInventory()) {
                    vault.setIcon(clickedItem.getType());
                    DataSource.getInstance().saveVault(vault);

                    iconItem.setType(clickedItem.getType());
                    iconItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
                }
            })
            .build();

    @SuppressWarnings("unchecked")
    private final GuiItem editTrustedListButton = GuiItem.builder()
            .index(() -> cfg.editTrustListButton().slot())
            .itemStack(b -> b
                    .stringReplacements(
                            Couple.of("{vaultName}", vault.getCustomName()),
                            Couple.of("{id}", String.valueOf(vault.getId())),
                            Couple.of("{name}", p.getName()),
                            Couple.of("{trustedListSize}", trustListCap())
                            //Couple.of("{trustedList}", trustedListString()),
                    )
                    .displayName(cfg.editTrustListButton().name())
                    .material(cfg.editTrustListButton().material())
                    .headOwner(cfg.editTrustListButton().headOwner())
                    .lore(Util.replaceStringWithList(cfg.editTrustListButton().lore(), "{trustedList}", trustedListString()))
            )
            .action(event -> {
                ItemStack clickedItem = event.getCurrentItem();
                Player player = (Player) event.getWhoClicked();
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);

                ChatInputCallback.of(
                        player,
                        Text.title("<red><b>Enter text", "Enter in chat"),
                        "In chat, enter the username of a player. To remove an existing player from your vault, enter their name. Type 'cancel' to cancel.",
                        input -> {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(input);

                            if (offlinePlayer == null) {
                                lng.entry(l -> l.vaults().playerNeverOnServer(), player, Couple.of("{name}", input));
                                open(player);
                                return;
                            }

                            if (vault.isTrusted(offlinePlayer.getUniqueId())) {
                                vault.removeTrusted(offlinePlayer.getUniqueId());
                                lng.entry(l -> l.vaults().playerUntrusted(), player, Couple.of("{name}", input));
                            } else if (vault.addTrusted(offlinePlayer.getUniqueId())){
                                lng.entry(l -> l.vaults().playerTrusted(), player, Couple.of("{name}", input));
                            } else {
                                lng.entry(l -> l.vaults().trustListMaxed(), player, Couple.of("{trustedListSize}", trustListCap()));
                            }

                            DataSource.getInstance().saveVault(vault);

                            Util.editMeta(clickedItem, meta -> {
                                meta.displayName(Text.mmNoItalic(cfg.editTrustListButton().name().replace("{trustedListSize}", trustListCap()), NamedTextColor.AQUA));
                                meta.lore(Text.mmlNoItalic(
                                        Util.replaceAll(
                                                Util.replaceStringWithList(cfg.editTrustListButton().lore(), "{trustedList}", trustedListString()),
                                                "{trustedListSize}", trustListCap()
                                        ),
                                        NamedTextColor.WHITE
                                ));
                            });
                            //event.getInventory().setItem(cfg.editTrustListButton().slot(), item);
                            open(player);
                        },
                        () -> open(player)
                );
            })
            .build();


    public EditVaultGui(Vault vault, GringottsPlayer gringottsPlayer, Player player) {
        super(cfg.title().replace("{vaultName}", vault.getCustomName()).replace("{id}", String.valueOf(vault.getId())), cfg.size());
        this.vault = vault;
        this.gringottsPlayer = gringottsPlayer;
        this.p = player;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void open(Player player) {
        Executors.sync(() -> player.openInventory(this.getInventory()));
    }

    private String trustListCap() {
        int max = ConfigManager.get(Config.class).vaultTrustCap();
        return vault.getTrustedPlayers().size() + "/" + max;
    }

    private List<String> trustedListString() {
        return vault.getTrustedPlayers().stream().map(id -> {
            String name = Bukkit.getOfflinePlayer(id).getName();
            return "- " + (name != null ? name : id.toString());
        }).toList();
    }
}
