package dev.jsinco.malts.gui;

import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;

/**
 * A promised inventory is an inventory that must query from the database before it is ready to be displayed to players.
 * Guis or inventories that implement this should not have their Bukkit {org.bukkit.inventory.InventoryHandler#getInventory} method called.
 */
public interface PromisedInventory {

    CompletableFuture<Inventory> promiseInventory();
}
