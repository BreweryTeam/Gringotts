package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.events.ChatPromptInputListener.ChatInputCallback;
import dev.jsinco.gringotts.gui.item.AutoRegisterGuiItems;
import dev.jsinco.gringotts.gui.item.GuiItem;
import dev.jsinco.gringotts.gui.item.UncontainedGuiItem;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Executors;
import dev.jsinco.gringotts.utility.Text;
import dev.jsinco.gringotts.utility.Util;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

// TODO: Add cap to trust list
@AutoRegisterGuiItems
public class EditVaultGui extends GringottsGui {

    private Vault vault;
    private GringottsPlayer gringottsPlayer;
    private Player p;

    private final GuiItem backButton = GuiItem.builder()
            .index(() -> 0)
            .itemStack(b -> b
                    .displayName("<red>Back")
                    .material(Material.BARRIER)
            )
            .action(e -> {
                Player player = (Player) e.getWhoClicked();
                YourVaultsGui yourVaultsGui = GringottsGui.factory(() -> new YourVaultsGui(gringottsPlayer));
                yourVaultsGui.open(player);
            })
            .build();
    private final GuiItem editNameButton = GuiItem.builder()
            .index(() -> 12)
            .itemStack(builder -> builder
                    .displayName("Change Name")
                    .material(Material.NAME_TAG)
                    .lore("Current Name:", "<i>\"" + vault.getCustomName() + "\"")
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
                                meta.lore(Text.mmlNoItalic("Current Name:", "<i>\"" + vault.getCustomName() + "\""));
                            });
                            open(player);
                        },
                        () -> open(player)
                );
            })
            .build();

    private final UncontainedGuiItem editIconButton = UncontainedGuiItem.builder()
            .index(() -> 13)
            .itemStack(b -> b
                    .displayName("Change Icon")
                    .material(vault.getIcon())
                    .lore("Click to change the icon", "of this vault.")
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
                } else if (currentValue) {
                    vault.setIcon(clickedItem.getType());
                    DataSource.getInstance().saveVault(vault);

                    iconItem.setType(clickedItem.getType());
                    iconItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
                }
            })
            .build();

    private final GuiItem editTrustedListButton = GuiItem.builder()
            .index(() -> 14)
            .itemStack(b -> b
                    .displayName("Edit Trust List")
                    .material(Material.PLAYER_HEAD)
                    .playerProfile(p.getPlayerProfile())
                    .lore(
                            Stream.concat(
                                    Stream.of("Trusted Players:"),
                                    vault.getTrustedPlayers().stream().map(id -> {
                                        String name = Bukkit.getOfflinePlayer(id).getName();
                                        return "- " + (name != null ? name : id.toString());
                                    })
                            ).toArray(String[]::new)
                    )
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
                                player.sendMessage("'" + input + "' has never been on this server.");
                                open(player);
                                return;
                            }

                            if (vault.isTrusted(offlinePlayer.getUniqueId())) {
                                vault.removeTrusted(offlinePlayer.getUniqueId());
                                player.sendMessage("'" + input + "' has been removed from your vault.");
                            } else {
                                vault.addTrusted(offlinePlayer.getUniqueId());
                                player.sendMessage("'" + input + "' has been added to your vault.");
                            }

                            DataSource.getInstance().saveVault(vault);

                            Util.editMeta(clickedItem, meta -> {
                                meta.lore(Text.mmlNoItalic(
                                        Stream.concat(
                                                Stream.of("Trusted Players:"),
                                                vault.getTrustedPlayers().stream().map(id -> {
                                                    String name = Bukkit.getOfflinePlayer(id).getName();
                                                    return "- " + (name != null ? name : id.toString());
                                                })
                                        ).toArray(String[]::new)
                                ));
                            });
                            open(player);
                        },
                        () -> open(player)
                );
            })
            .build();


    public EditVaultGui(Vault vault, GringottsPlayer gringottsPlayer, Player player) {
        super("Editing Vault #" + vault.getId(), 27);
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
}
