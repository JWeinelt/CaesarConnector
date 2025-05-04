package de.julianweinelt.caesar.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Configuration {
    private String caesarHost = "localhost";
    private int caesarPort = 49850;


    private String databaseHost = "localhost";
    private int databasePort = 3306;
    private String databaseName = "caesar";
    private String databaseUser = "root";
    private String databasePassword = "secret";
    private StorageType storageType = StorageType.MYSQL;

    private String serverName = "SomeServer-1";
    private UUID serverId = UUID.randomUUID();

    private String connectionKey = "secret";
}