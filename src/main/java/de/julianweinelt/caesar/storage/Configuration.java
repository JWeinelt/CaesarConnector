package de.julianweinelt.caesar.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Configuration {
    private String caesarHost;
    private int caesarPort;


    private String databaseHost;
    private int databasePort;
    private String databaseName;
    private String databaseUser;
    private String databasePassword;
    private StorageType storageType;

    private String serverName;
    private UUID serverId = UUID.randomUUID();

    private String connectionKey;
}