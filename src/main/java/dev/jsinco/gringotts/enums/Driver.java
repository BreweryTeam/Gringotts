package dev.jsinco.gringotts.enums;

import dev.jsinco.gringotts.storage.sources.MySQLDataSource;
import lombok.Getter;
import dev.jsinco.gringotts.storage.DataSource;
import dev.jsinco.gringotts.storage.sources.SQLiteDataSource;

import java.util.function.Supplier;

@Getter
public enum Driver {

    SQLITE(SQLiteDataSource::new, SQLiteDataSource.class, "SQLite"),
    MYSQL(MySQLDataSource::new, MySQLDataSource.class, "MySQL");

    private final Supplier<DataSource> supplier;
    private final Class<? extends DataSource> identifyingClass;
    private final String asString;

    Driver(Supplier<DataSource> supplier, Class<? extends DataSource> identifyingClass, String asString) {
        this.supplier = supplier;
        this.identifyingClass = identifyingClass;
        this.asString = asString;
    }

    @Override
    public String toString() {
        return asString;
    }
}
