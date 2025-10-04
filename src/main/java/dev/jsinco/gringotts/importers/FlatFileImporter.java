package dev.jsinco.gringotts.importers;

import dev.jsinco.gringotts.utility.Couple;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FlatFileImporter extends Importer {

    Path path();

    CompletableFuture<Couple<UUID, Result>> importVaults(File file);

}
