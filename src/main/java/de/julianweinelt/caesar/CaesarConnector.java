package de.julianweinelt.caesar;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.julianweinelt.caesar.extensions.ExtensionLoader;
import de.julianweinelt.caesar.commands.CaesarCommand;
import de.julianweinelt.caesar.commands.CaesarCompleter;
import de.julianweinelt.caesar.connection.CaesarLink;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.StorageFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CaesarConnector extends JavaPlugin implements PluginMessageListener {
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
    private Registry featureRegistry;

    @Getter
    private ExtensionLoader extensionLoader;

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
            log.info("Trying to link to Caesar...");
            link = new CaesarLink(URI.create(storage.getData().getCaesarHost() + ":" + storage.getData().getCaesarPort()));
        } else {
            log.info("CloudNet not found. Running in standalone mode.");
            log.info("Using " + storage.getData().getServerName() + " as server name.");
            standalone = true;

            log.info("Trying to link to Caesar...");
            Map<String, String> headers = new HashMap<>();
            headers.put("ConnectionKey", storage.getData().getConnectionKey());
            link = new CaesarLink(URI.create("ws://" + storage.getData().getCaesarHost() + ":" + storage.getData().getCaesarPort()),
                    headers);
            link.connect();
        }

        getCommand("caesar").setExecutor(new CaesarCommand());
        getCommand("caesar").setTabCompleter(new CaesarCompleter());

        log.info("Starting system usage analyzer...");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->
                storageFactory.getStorage().sendServerData(), 0, 20 * 60 * 5);
        featureRegistry = new Registry();
        featureRegistry.registerPermissions();

        log.info("Registering to Proxy communication...");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        log.info("Starting Caesar Extension Loader...");
        extensionLoader = new ExtensionLoader();
        log.info("Loading Caesar Extensions...");
        extensionLoader.prepareLoading();
        extensionLoader.loadExtensions();
        log.info("Caesar Extensions loaded.");
        log.info("CaesarConnector has been enabled.");
        log.info("Thanks for using!");
    }

    @Override
    public void onDisable() {
        log.info("Unregistering from Proxy communication...");
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        log.info("Stopping system usage analyzer...");
        storageFactory.getStorage().sendServerData();
        storageFactory.getStorage().disconnect();
        log.info("Shutting down CaesarConnector");
        link.close();
        log.info("Disabling Caesar Extensions...");
        if (extensionLoader != null) extensionLoader.disableExtensions();
        log.info("CaesarConnector has been shut down.");
        log.info("Goodbye!");
    }


    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServer")) {
            String servername = in.readUTF();
            log.info("CaesarConnector received server name from BungeeCord: " + servername);
            storage.getData().setServerName(servername);
            storage.saveData();
            log.info("Server name saved.");
        }
    }

    public void getServername() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) {
            log.warning("No players online. Can't get server name.");
            return;
        }
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }
}