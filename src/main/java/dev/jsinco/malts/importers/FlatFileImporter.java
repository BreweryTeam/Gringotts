package dev.jsinco.malts.importers;

import dev.jsinco.malts.utility.Couple;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FlatFileImporter extends Importer {

    Path path();

    CompletableFuture<Couple<UUID, Result>> importVaults(File file);

}
