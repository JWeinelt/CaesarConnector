package de.julianweinelt.caesar.storage;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.storage.providers.*;
import lombok.Getter;

public class StorageFactory {
    @Getter
    private Storage storage;

    public static StorageFactory getInstance() {
        return CaesarConnector.getInstance().getStorageFactory();
    }

    public void createStorage() {
        switch (LocalStorage.getInstance().getData().getStorageType()) {
            case MYSQL:
                storage = new MySQL();
                break;
            case MARIADB:
                storage = new MariaDB();
                break;
            case MSSQL:
                storage = new MSSQL();
                break;
            case ORACLE_SQL:
                storage = new OracleSQL();
                break;
            case POSTGRESQL:
                storage = new PostgreSQL();
                break;
        }
    }
}