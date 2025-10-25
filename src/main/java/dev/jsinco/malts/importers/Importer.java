package dev.jsinco.malts.importers;

import dev.jsinco.malts.registry.RegistryItem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Importer extends RegistryItem {


    CompletableFuture<Map<UUID, Result>> importAll();

    boolean canImport();

    enum Result {
        SUCCESS,
        NO_VAULTS_IN_OTHER_PLUGIN,
        VAULTS_NOT_EMPTY,
        FAILED_TO_IMPORT_SOME_VAULTS,
        FAILED;
    }
}
