package dev.jsinco.malts.obj;

import dev.jsinco.malts.storage.DataSource;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CachedObject {

    UUID getUuid();

    @Nullable
    Long getExpire();

    void setExpire(@Nullable Long expire);

    CompletableFuture<Void> save(DataSource dataSource);

    default boolean isExpired() {
        Long expiration = this.getExpire();
        return expiration != null && expiration < System.currentTimeMillis();
    }

}
