package dev.jsinco.gringotts.api;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.importers.Importer;
import dev.jsinco.gringotts.integration.Integration;
import dev.jsinco.gringotts.obj.GringottsPlayer;
import dev.jsinco.gringotts.obj.SnapshotVault;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.obj.Warehouse;
import dev.jsinco.gringotts.registry.Registry;
import dev.jsinco.gringotts.storage.DataSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p></p>
 * The main entry point for the Gringotts API.
 * This class consists of various static methods to interact with the Gringotts plugin.
 * It is advised for you to read the Javadocs for each method to understand their usage.
 * For extra information, you may look into each class' source code
 * </p>
 * <br><br/>
 * <p>
 * Events can be found in the {@link dev.jsinco.gringotts.api.events} package.
 * Guis can be found in the {@link dev.jsinco.gringotts.gui} package, although they are not technically 'exposed' API.
 * <b>When opening a Gringotts gui, please use the {@link dev.jsinco.gringotts.gui.GringottsGui#open} method to ensure proper functionality.</b>
 * </p>
 */
public final class GringottsAPI {

    private GringottsAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get the main Gringotts instance.
     * This is useful for accessing the plugin's logger, data folder, and other Bukkit-related functionality.
     * @return The main Gringotts instance
     */
    @NotNull
    public static Gringotts getGringottsInstance() {
        return Gringotts.getInstance();
    }

    /**
     * Get the config registry.
     * This registry contains all configuration files used by Gringotts.
     * @return The config registry
     */
    @NotNull
    public static Registry<OkaeriFile> getConfigRegistry() {
        return Registry.CONFIGS;
    }

    /**
     * Get the importer registry.
     * This registry contains all importers available for importing data from other plugins.
     * @return The importer registry
     */
    @NotNull
    public static Registry<Importer> getImporterRegistry() {
        return Registry.IMPORTERS;
    }

    /**
     * Get the sub-command registry.
     * This registry contains all sub-commands available for the /gringotts command.
     * @return The sub-command registry
     */
    @NotNull
    public static Registry<SubCommand> getSubCommandRegistry() {
        return Registry.SUB_COMMANDS;
    }

    /**
     * Get the integration registry.
     * This registry contains all integrations available for Gringotts.
     * @return The integration registry
     */
    @NotNull
    public static Registry<Integration> getIntegrationRegistry() {
        return Registry.INTEGRATIONS;
    }

    /**
     * Get the main config.
     * @return The "config.yml" file
     */
    @NotNull
    public static Config getConfig() {
        return getConfigRegistry().get(Config.class);
    }

    /**
     * Get the GUI config.
     * @return The "gui.yml" file
     */
    @NotNull
    public static GuiConfig getGuiConfig() {
        return getConfigRegistry().get(GuiConfig.class);
    }

    /**
     * Get the language config.
     * @see Lang#getFileName()
     * @return The active language file.
     */
    @NotNull
    public static Lang getLang() {
        return getConfigRegistry().get(Lang.class);
    }

    /**
     * <b>
     * Important: Some internal gringotts interactions are exposed through this class.
     * Using the data source directly is not recommended, and getting cacheable objects
     * directly from the database is dangerous and may lead to data inconsistency!
     * </b>
     *
     * <p>
     * Get the data source.
     * The data source is responsible for all database interactions.
     * </p>
     *
     * @return The data source
     */
    @ApiStatus.Internal
    @NotNull
    public static DataSource getDataSource() {
        return DataSource.getInstance();
    }

    /**
     * Queries the database for a vault with the given owner and id.
     * If the vault does not exist it will be created and returned.
     * @param owner The owner of the vault
     * @param id The id of the vault
     * @return A future that will complete with the vault, or complete exceptionally if not found
     */
    @NotNull
    public static CompletableFuture<@NotNull Vault> getVault(UUID owner, int id) {
        return getDataSource().getVault(owner, id);
    }

    /**
     * Queries the database for all vaults owned by the given owner.
     * These snapshot vaults can be transformed using {@link SnapshotVault#toVault()}.
     * @param owner The owner of the vaults
     * @return A future that will complete with a list of snapshot vaults, or complete exceptionally if an error occurs
     */
    @NotNull
    public static CompletableFuture<@NotNull List<SnapshotVault>> getVaults(UUID owner) {
        return getDataSource().getVaults(owner);
    }

    /**
     * Saves the given vault to the database.
     * @param vault The vault to save
     * @return A future that will complete when the vault is saved, or complete exceptionally if an error occurs
     */
    @NotNull
    public static CompletableFuture<@NotNull Void> saveVault(Vault vault) {
        return getDataSource().saveVault(vault);
    }

    /**
     * Deletes the vault with the given owner and id from the database.
     * @param owner The owner of the vault
     * @param id The id of the vault
     * @return A future that will complete with true if the vault was deleted, false if it did not exist, or complete exceptionally if an error occurs
     */
    @NotNull
    public static CompletableFuture<@NotNull Boolean> deleteVault(UUID owner, int id) {
        return getDataSource().deleteVault(owner, id);
    }

    /**
     * Attempts to get a cached warehouse for the given owner.
     * If not cached, it will query the database and cache the result with the default expiration time set by the config.
     * @param owner The owner of the warehouse
     * @return A future that will complete with the warehouse
     */
    @NotNull
    public static CompletableFuture<@NotNull Warehouse> getOrCacheWarehouseWithDefaultExpire(UUID owner) {
        DataSource dataSource = getDataSource();
        return dataSource.cacheObjectWithDefaultExpire(dataSource.getWarehouse(owner));
    }

    /**
     * Attempts to get a cached warehouse for the given owner.
     * If not cached, it will query the database and cache the result for the number of milliseconds provided.
     * @param owner The owner of the warehouse
     * @param expireMillis The expiration time in milliseconds
     * @return A future that will complete with the warehouse
     */
    @NotNull
    public static CompletableFuture<@NotNull Warehouse> getOrCacheWarehouse(UUID owner, long expireMillis) {
        DataSource dataSource = getDataSource();
        return dataSource.cacheObject(dataSource.getWarehouse(owner), expireMillis);
    }

    /**
     * Attempts to get a cached warehouse for the given owner.
     * If not cached, it will query the database and cache the result indefinitely.
     * @see GringottsAPI#uncacheWarehouse(UUID)
     * @param owner The owner of the warehouse
     * @return A future that will complete with the warehouse
     */
    @NotNull
    public static CompletableFuture<@NotNull Warehouse> getOrCacheWarehouse(UUID owner) {
        return getDataSource().getWarehouse(owner);
    }

    /**
     * Gets a cached warehouse for the given owner.
     * If not cached, it will return null.
     * @param owner The owner of the warehouse
     * @return The cached warehouse, or null if not cached
     */
    @Nullable
    public static Warehouse getCachedWarehouse(UUID owner) {
        return getDataSource().cachedObject(owner, Warehouse.class);
    }

    /**
     * Uncaches the warehouse for the given owner and saves it to the database.
     * @param owner The owner of the warehouse
     */
    public static void uncacheWarehouse(UUID owner) { // TODO: Use CompletableFuture<Boolean>
        getDataSource().uncacheObject(owner, Warehouse.class);
    }

    /**
     * Attempts to get a cached GringottsPlayer for the given UUID.
     * If not cached, it will query the database and cache the result with the default expiration time set by the config.
     * @param uuid The UUID of the player
     * @return A future that will complete with the GringottsPlayer
     */
    @NotNull
    public static CompletableFuture<@NotNull GringottsPlayer> getOrCacheGringottsPlayerWithDefaultExpire(UUID uuid) {
        DataSource dataSource = getDataSource();
        return dataSource.cacheObjectWithDefaultExpire(dataSource.getGringottsPlayer(uuid));
    }

    /**
     * Attempts to get a cached GringottsPlayer for the given UUID.
     * If not cached, it will query the database and cache the result for the number of milliseconds provided.
     * @param uuid The UUID of the player
     * @param expireMillis The expiration time in milliseconds
     * @return A future that will complete with the GringottsPlayer
     */
    @NotNull
    public static CompletableFuture<@NotNull GringottsPlayer> getOrCacheGringottsPlayer(UUID uuid, long expireMillis) {
        DataSource dataSource = getDataSource();
        return dataSource.cacheObject(dataSource.getGringottsPlayer(uuid), expireMillis);
    }

    /**
     * Attempts to get a cached GringottsPlayer for the given UUID.
     * If not cached, it will query the database and cache the result indefinitely.
     * @see GringottsAPI#uncacheGringottsPlayer(UUID)
     * @param uuid The UUID of the player
     * @return A future that will complete with the GringottsPlayer
     */
    @NotNull
    public static CompletableFuture<@NotNull GringottsPlayer> getOrCacheGringottsPlayer(UUID uuid) {
        return getDataSource().getGringottsPlayer(uuid);
    }

    /**
     * Gets a cached GringottsPlayer for the given UUID.
     * If not cached, it will return null.
     * @param uuid The UUID of the player
     * @return The cached GringottsPlayer, or null if not cached
     */
    @Nullable
    public static GringottsPlayer getCachedGringottsPlayer(UUID uuid) {
        return getDataSource().cachedObject(uuid, GringottsPlayer.class);
    }

    /**
     * Uncaches the GringottsPlayer for the given UUID and saves it to the database.
     * @param uuid The UUID of the player
     */
    public static void uncacheGringottsPlayer(UUID uuid) { // TODO: Use CompletableFuture<Boolean>
        getDataSource().uncacheObject(uuid, GringottsPlayer.class);
    }

}
