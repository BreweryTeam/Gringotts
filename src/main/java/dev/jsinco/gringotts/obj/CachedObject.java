package dev.jsinco.gringotts.obj;

import dev.jsinco.gringotts.storage.DataSource;

import java.util.UUID;

public interface CachedObject {
    UUID getUuid();
    long getCacheTime();
    Long getExpire();
    void setExpire(Long expire);
    void save(DataSource dataSource);
}
