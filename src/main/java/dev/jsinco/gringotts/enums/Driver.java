package dev.jsinco.gringotts.enums;

import dev.jsinco.gringotts.storage.sources.MySQLDataSource;
import lombok.Getter;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.storage.sources.SQLiteDataSource;

import java.util.function.Supplier;

@Getter
public enum Driver {

    SQLITE(SQLiteDataSource::new),
    MYSQL(MySQLDataSource::new);

    private final Supplier<DataSource> supplier;

    Driver(Supplier<DataSource> supplier) {
        this.supplier = supplier;
    }
}
