package dev.jsinco.gringotts.enums;

import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.Couple;
import dev.jsinco.gringotts.utility.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum WarehouseMode {

    // Automatically places collected items into the warehouse if there is a compartment for the material.
    AUTO_STORE(PlayerAttemptPickupItemEvent.class, (event, gringottsPlayer, warehouse) -> {
        Item physicalItem = event.getItem();
        ItemStack itemStack = physicalItem.getItemStack();
        Material material = itemStack.getType();
        if (!warehouse.hasCompartment(itemStack)) {
            return;
        }

        Lang lang = ConfigManager.get(Lang.class);
        int deposited = warehouse.stockItem(material, itemStack.getAmount());
        int remainder = itemStack.getAmount() - deposited;
        if (deposited < 0) {
            return;
        }

        if (remainder == 0) {
            physicalItem.remove();
        } else {
            itemStack.setAmount(remainder);
            physicalItem.setItemStack(itemStack);
        }

        lang.actionBarEntry(l -> l.warehouse().autoStoredItem(),
                event.getPlayer(),
                Couple.of("{material}", Util.formatEnumerator(material)),
                Couple.of("{amount}", deposited),
                Couple.of("{remainder}", remainder),
                Couple.of("{stock}", warehouse.getQuantity(material))
        );
        event.setCancelled(true);
        event.setFlyAtPlayer(true);
    }),

    // Allows players to click on a container to deposit items from their warehouse into it depending on the material they are holding.
    CLICK_TO_DEPOSIT(PlayerInteractEvent.class, (event, gringottsPlayer, warehouse) -> {
        Block clickedBlock = event.getClickedBlock();
        ItemStack itemInHand = event.getItem();
        Player player = event.getPlayer();
        if (itemInHand == null || !event.getAction().isLeftClick() || !(clickedBlock instanceof Container container)) {
            return;
        }
        Material material = itemInHand.getType();
        Lang lang = ConfigManager.get(Lang.class);
        Inventory inv = container.getInventory();


        int invAmt = Util.getAmountInvCanHold(inv, material);
        if (invAmt == 0) {
            lang.entry(l -> l.warehouse().containerFull(), player);
            return;
        }

        ItemStack item = warehouse.destockItem(material, invAmt);
        if (item != null) {
            inv.addItem(item);
            lang.actionBarEntry(l -> l.warehouse().depositedItem(), player,
                    Couple.of("{material}", Util.formatEnumerator(material)),
                    Couple.of("{amount}", item.getAmount()),
                    Couple.of("{stock}", warehouse.getQuantity(material))
            );
        } else {
            lang.actionBarEntry(l -> l.warehouse().notEnoughMaterial(), player,
                    Couple.of("{material}", Util.formatEnumerator(material.toString()))
            );
        }
    }),

    // Automatically refills the player's inventory from warehouse stock when items run low. (Consuming or building.)
    AUTO_REPLENISH(List.of(BlockPlaceEvent.class, PlayerItemConsumeEvent.class), (event, gringottsPlayer, warehouse) -> {
        ItemStack itemInHand;
        Player player;

        if (event instanceof BlockPlaceEvent e) {
            itemInHand = e.getItemInHand();
            player = e.getPlayer();
        } else if (event instanceof PlayerItemConsumeEvent e) {
            itemInHand = e.getItem();
            player = e.getPlayer();
        } else {
            throw new IllegalStateException("Unexpected event type.");
        }

        Material material = itemInHand.getType();

        if (itemInHand.getAmount() < 2 && warehouse.hasCompartment(material)) {
            ItemStack item = warehouse.destockItem(material, itemInHand.getMaxStackSize() - itemInHand.getAmount());
            if (item == null) {
                return;
            }
            player.getInventory().addItem(item);
            ConfigManager.get(Lang.class).actionBarEntry(l -> l.warehouse().replenishedItem(), player,
                    Couple.of("{material}", Util.formatEnumerator(material)),
                    Couple.of("{amount}", item.getAmount()),
                    Couple.of("{stock}", warehouse.getQuantity(material))
            );
        }
    }),
    NONE(List.of(), ((event, gringottsPlayer, warehouse) -> {
        // Do nothing
    }));

    private final List<Class<? extends  Event>> eventClasses;
    private final Handler<? extends Event> handler;

    <T extends Event> WarehouseMode(Class<T> eventClass, Handler<T> handler) {
        this.eventClasses = List.of(eventClass);
        this.handler = handler;
    }
    <T extends Event> WarehouseMode(List<Class<? extends Event>> eventClasses, Handler<T> handler) {
        this.eventClasses = eventClasses;
        this.handler = handler;
    }


    @Nullable
    public String getPermission() {
        if (this == NONE) {
            return null;
        }
        return "gringotts.warehouse.mode." + this.name().toLowerCase();
    }

    @SuppressWarnings("unchecked") // We suppress here since we trust enum construction to match event types
    public <T extends Event> void handle(T event, GringottsPlayer gringottsPlayer, Player player) {
        if (this == NONE || !this.eventClasses.contains(event.getClass())) {
            return;
        } else if (!player.hasPermission(this.getPermission())) {
            WarehouseMode newMode = getNextMode(this, player);
            gringottsPlayer.setWarehouseMode(newMode);
            // TODO: Lang
            return;
        }

        DataSource dataSource = DataSource.getInstance();
        Warehouse warehouse = dataSource.cachedWarehouse(gringottsPlayer.getUuid());

        if (warehouse == null) {
            throw new IllegalStateException("Warehouse is not cached.");
        }
        ((Handler<T>) handler).handle(event, gringottsPlayer, warehouse);
    }

    @FunctionalInterface
    public interface Handler<T extends Event> {
        void handle(T event, GringottsPlayer gringottsPlayer, Warehouse warehouse);
    }


    public static List<WarehouseMode> getEnabledModes() {
        List<WarehouseMode> modes = new ArrayList<>(ConfigManager.get(Config.class).warehouse().enabledModes());
        if (!modes.contains(NONE)) {
            modes.add(NONE);
        }
        return modes;
    }

    public static WarehouseMode getNextMode(final WarehouseMode mode, Player player) {
        WarehouseMode newMode = cycleMode(mode);
        while (newMode.getPermission() != null && !player.hasPermission(newMode.getPermission())) {
            newMode = cycleMode(newMode);
        } // Will eventually break because 'NONE' has null permission.
        return newMode;
    }

    private static WarehouseMode cycleMode(WarehouseMode mode) {
        List<WarehouseMode> modes = getEnabledModes();
        int index = modes.indexOf(mode);
        if (index == -1) {
            index = 0; // Mode must have been disabled.
        }
        return modes.get((index + 1) % modes.size());
    }
}
