package dev.jsinco.malts.gui;

import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.configuration.files.GuiConfig;
import dev.jsinco.malts.configuration.files.Lang;
import dev.jsinco.malts.events.ChatPromptInputListener.ChatInputCallback;
import dev.jsinco.malts.gui.item.GuiItem;
import dev.jsinco.malts.gui.item.ItemConfirmation;
import dev.jsinco.malts.gui.item.UncontainedGuiItem;
import dev.jsinco.malts.obj.MaltsPlayer;
import dev.jsinco.malts.obj.Vault;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Executors;
import dev.jsinco.malts.utility.ItemStacks;
import dev.jsinco.malts.utility.Text;
import dev.jsinco.malts.utility.Util;
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

public class EditVaultGui extends MaltsGui {

    private static final GuiConfig.EditVaultGui cfg = ConfigManager.get(GuiConfig.class).editVaultGui();
    private static final Lang lng = ConfigManager.get(Lang.class);

    private Vault vault;
    private MaltsPlayer maltsPlayer;
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
                YourVaultsGui yourVaultsGui = new YourVaultsGui(maltsPlayer);
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
                            if (!vault.setCustomName(input)) return;
                            DataSource.getInstance().saveVault(vault);

                            lng.entry(l -> l.vaults().nameChanged(), player, Couple.of("{vaultName}", vault.getCustomName()));
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


                ItemConfirmation itemConfirmation = new ItemConfirmation(iconItem);
                boolean currentValue = itemConfirmation.isConfirmed();
                Inventory clickedInventory = event.getClickedInventory();


                if (isClicked) {
                    itemConfirmation.setConfirmation(!currentValue);
                } else if (currentValue && clickedInventory != event.getInventory()) {
                    if (!vault.setIcon(clickedItem.getType())) return;

                    DataSource.getInstance().saveVault(vault);

                    iconItem.setType(clickedItem.getType());
                    itemConfirmation.setConfirmation(false);
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
                                if (vault.removeTrusted(offlinePlayer.getUniqueId())) {
                                    lng.entry(l -> l.vaults().playerUntrusted(), player, Couple.of("{name}", input));
                                } else {
                                    lng.entry(l -> l.vaults().playerNotTrusted(), player, Couple.of("{name}", input));
                                }
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


    public EditVaultGui(Vault vault, MaltsPlayer maltsPlayer, Player player) {
        super(cfg.title().replace("{vaultName}", vault.getCustomName()).replace("{id}", String.valueOf(vault.getId())), cfg.size());
        this.vault = vault;
        this.maltsPlayer = maltsPlayer;
        this.p = player;

        this.autoRegister(false);

        if (cfg.borders()) {
            for (int i = 0; i < this.inventory.getSize(); i++) {
                if (this.inventory.getItem(i) == null) {
                    this.inventory.setItem(i, ItemStacks.BORDER);
                }
            }
        }
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @Override
    public void openImpl(Player player) {
        player.openInventory(this.getInventory());
    }

    private String trustListCap() {
        int max = ConfigManager.get(Config.class).vaults().trustCap();
        return vault.getTrustedPlayers().size() + "/" + max;
    }

    private List<String> trustedListString() {
        return vault.getTrustedPlayers().stream().map(id -> {
            String name = Bukkit.getOfflinePlayer(id).getName();
            return "- " + (name != null ? name : id.toString());
        }).toList();
    }
}
