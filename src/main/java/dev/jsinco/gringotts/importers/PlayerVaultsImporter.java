package dev.jsinco.gringotts.importers;

import dev.jsinco.gringotts.Gringotts;
import dev.jsinco.gringotts.obj.Vault;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.utility.ClassUtil;
import dev.jsinco.gringotts.utility.Couple;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PlayerVaultsImporter implements FlatFileImporter {


    @Override
    public String name() {
        return "PlayerVaults";
    }

    @Override
    public Path path() {
        Path path = Gringotts.getInstance().getDataPath().getParent();
        return path.resolve("PlayerVaults")
                .resolve("newvaults");
    }

    @Override
    public boolean canImport() {
        return ClassUtil.classExists("com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization");
    }

    @Override
    public CompletableFuture<Map<UUID, Result>> importAll() {
        File[] filesList = path().toFile().listFiles();

        if (filesList == null) {
            return CompletableFuture.completedFuture(Map.of());
        }

        List<CompletableFuture<Couple<UUID, Result>>> futures = Arrays.stream(filesList)
                .filter(File::isFile)
                .map(this::importVaults)
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toMap(Couple::a, Couple::b)));
    }

    @Override
    public CompletableFuture<Couple<UUID, Result>> importVaults(File file) {
        DataSource dataSource = DataSource.getInstance();
        String uuidAsString = file.getName().replace(".yml", "");
        UUID owner = UUID.fromString(uuidAsString);
        YamlConfiguration loadedFile = YamlConfiguration.loadConfiguration(file);
        Result expectedResult;
        List<ItemStack[]> rawInventories = loadedFile.getKeys(false).stream()
                .map(loadedFile::getString)
                .map(base64 -> {
                    try {
                        return com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization.fromStorage(base64, uuidAsString);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .toList();
        List<ItemStack[]> inventories = rawInventories.stream().filter(inv -> inv != null && inv.length > 0).toList();

        if (rawInventories.stream().anyMatch(Objects::isNull)) {
            expectedResult = Result.FAILED_TO_IMPORT_SOME_VAULTS;
        } else {
            expectedResult = Result.SUCCESS;
        }

        if (inventories.isEmpty()) {
            return CompletableFuture.completedFuture(Couple.of(owner, Result.NO_VAULTS_IN_OTHER_PLUGIN));
        }

        return dataSource.getVaults(owner).thenCompose(existing -> {
            if (!existing.isEmpty()) {
                return CompletableFuture.completedFuture(Couple.of(owner, Result.VAULTS_NOT_EMPTY));
            }

            // Chain all saveVault() calls
            CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
            int i = 1;
            for (ItemStack[] inv : inventories) {
                final int index = i++;
                chain = chain.thenCompose(v -> dataSource.saveVault(new Vault(owner, index, inv)).thenApply(x -> null));
            }

            return chain.thenApply(v -> Couple.of(owner, expectedResult));
        });
    }

}
