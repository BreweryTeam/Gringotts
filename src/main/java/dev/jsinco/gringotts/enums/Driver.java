package dev.jsinco.gringotts.enums;

import lombok.Getter;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.storage.sources.SQLiteDataSource;

import java.util.function.Supplier;

@Getter
public enum Driver {

    SQLITE(SQLiteDataSource::new),;

    private final Supplier<DataSource> supplier;

    Driver(Supplier<DataSource> supplier) {
        this.supplier = supplier;
    }
}
