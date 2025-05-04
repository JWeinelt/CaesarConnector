package de.julianweinelt.caesar;

import de.julianweinelt.caesar.commands.CaesarCommand;
import de.julianweinelt.caesar.commands.CaesarCompleter;
import de.julianweinelt.caesar.connection.CaesarLink;
import de.julianweinelt.caesar.feature.FeatureRegistry;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.StorageFactory;
import eu.cloudnetservice.driver.CloudNetVersion;
import eu.cloudnetservice.driver.DriverEnvironment;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.CloudServiceFactory;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceEnvironment;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CaesarConnector extends JavaPlugin {
    private Logger log;

    @Getter
    private static CaesarConnector instance;

    @Getter
    private boolean standalone = false;

    @Getter
    private CaesarLink link;

    @Getter
    private LocalStorage storage;
    @Getter
    private StorageFactory storageFactory;

    @Getter
    private FeatureRegistry featureRegistry;

    @Override
    public void onLoad() {
        log = getLogger();
        instance = this;
    }

    @Override
    public void onEnable() {
        storage = new LocalStorage();
        storage.loadData();
        storageFactory = new StorageFactory();
        log.info("Starting CaesarConnector");
        if (storage.getData().getCaesarHost() == null) {
            log.warning("No settings provided.");
            log.warning("Please configure the settings in the config.json file.");
            log.warning("Shutting down...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        log.info("Connecting to database...");
        storageFactory.createStorage();
        storageFactory.getStorage().connect();
        log.info("Database connected.");
        log.info("Checking for CloudNET");
        boolean cloudnet = Bukkit.getPluginManager().getPlugin("CloudNet-Bridge") != null;
        if (cloudnet) {
            ServiceInfoSnapshot snapshot = InjectionLayer.ext().instance(ServiceInfoSnapshot.class);
            if (snapshot != null) {
                storage.getData().setServerName(snapshot.name());
            }
            link = new CaesarLink(URI.create(storage.getData().getCaesarHost() + ":" + storage.getData().getCaesarPort()));
        } else {
            log.info("CloudNet not found. Running in standalone mode.");
            log.info("Using " + storage.getData().getServerName() + " as server name.");
            standalone = true;

            Map<String, String> headers = new HashMap<>();
            headers.put("ConnectionKey", storage.getData().getConnectionKey());
            link = new CaesarLink(URI.create(storage.getData().getCaesarHost() + ":" + storage.getData().getCaesarPort()),
                    headers);
            link.connect();
        }

        getCommand("caesar").setExecutor(new CaesarCommand());
        getCommand("caesar").setTabCompleter(new CaesarCompleter());

        log.info("Trying to link to Caesar...");
        log.info("Starting system usage analyzer...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
                storageFactory.getStorage().sendServerData(), 0, 20 * 60 * 5);
        featureRegistry = new FeatureRegistry();
        featureRegistry.registerPermissions();
    }

    @Override
    public void onDisable() {
        log.info("Stopping system usage analyzer...");
        storageFactory.getStorage().sendServerData();
        storageFactory.getStorage().disconnect();
        log.info("Shutting down CaesarConnector");
        link.close();
        log.info("CaesarConnector has been shut down.");
        log.info("Goodbye!");
    }
}