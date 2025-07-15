package de.julianweinelt.caesar.connection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.feature.Feature;
import de.julianweinelt.caesar.feature.NotificationManager;
import de.julianweinelt.caesar.feature.PunishmentManager;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.reports.ReportManager;
import de.julianweinelt.caesar.reports.ReportView;
import de.julianweinelt.caesar.storage.LocalStorage;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CaesarLink extends WebSocketClient {

    private final Logger log = CaesarConnector.getInstance().getLogger();
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    @Getter
    @Setter
    private boolean useEncryptedConnection = false;

    private ScheduledExecutorService schedulerRestart = Executors.newScheduledThreadPool(1);

    private CountDownLatch pingLatch;

    private final LiveConsoleHandler consoleHandler = new LiveConsoleHandler();

    @Getter
    private String serverVersion;

    public static CaesarLink getInstance() {
        return CaesarConnector.getInstance().getLink();
    }

    public CaesarLink(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    public CaesarLink(URI serverUri) {
        super(serverUri);
    }

    public void restart(String host, int port) {
        try {
            closeBlocking();
            super.uri = URI.create("ws://" + host + ":" + port);
            super.clearHeaders();
            super.addHeader("ConnectionKey", LocalStorage.getInstance().getData().getConnectionKey());
            connect();
        } catch (InterruptedException ignored) {}
    }

    public void restart() {
        try {
            closeBlocking();
            connect();
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        schedulerRestart.shutdown();
        log.info("Connection has been opened.");
        log.info("Sending handshake...");
        sendHandshake();
    }

    @Override
    public void onMessage(String s) {
        String data = s;
        if (useEncryptedConnection) {
            try {
                data = decrypt(s, getConnectionKey());
            } catch (Exception e) {
                log.severe("Could not receive data from server.");
                log.severe("Encryption is enabled, but the text sent by the server provided can't be decrypted.");
                log.severe(e.getMessage());
                return;
            }
        }
        JsonObject root = JsonParser.parseString(data).getAsJsonObject();
        if (root.has("action")) {
            switch (LinkAction.valueOf(root.get("action").getAsString())) {
                case HANDSHAKE:
                    log.info("Received handshake from Caesar.");
                    log.info("Caesar has been connected.");
                    serverVersion = root.get("serverVersion").getAsString();
                    useEncryptedConnection = root.get("useEncryptedConnection").getAsBoolean();
                    log.info("Server version: " + serverVersion);
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (NotificationManager.getInstance().hasNotification(player.getUniqueId())) {
                            Audience.audience(player).sendActionBar(Component.text("§aCaesar has been connected!"));
                        }
                    });
                    break;
                case DISCONNECT:
                    log.info("Received disconnect from Caesar.");
                    super.close(0);
                    break;
                case TRANSFER_CONFIG:
                    log.info("Received configuration from Caesar.");
                    if (root.get("useReports").getAsBoolean()){
                        Registry.instance().registerFeature(Feature.REPORT_SYSTEM);
                        ReportManager.instance().setView(new ReportView()
                                .prepare(root.get("reports").getAsJsonObject())
                        );
                        Bukkit.getPluginManager().registerEvents(ReportView.instance(), CaesarConnector.getInstance());
                        log.info("Report system feature has been registered.");
                    }
                    log.info("Configuration transfer complete.");
                    break;
                case PONG:
                    pingLatch.countDown();
                    break;
                case PING:
                    sendPong();
                    break;
                case PUNISHMENT_LIST:
                    List<UUID> banned = new ArrayList<>();
                    List<UUID> muted = new ArrayList<>();
                    for (JsonElement e : root.get("banned").getAsJsonArray()) {
                        banned.add(UUID.fromString(e.getAsString()));
                    }
                    for (JsonElement e : root.get("muted").getAsJsonArray()) {
                        muted.add(UUID.fromString(e.getAsString()));
                    }
                    PunishmentManager.instance().update(banned, muted);
                    log.info("Received a list of banned and muted players from backend.");
                    break;
                case SERVER_SHOW_CONSOLE:
                    boolean stream =  root.get("stream").getAsBoolean();
                    if (stream) {
                        consoleHandler.setFormatter(new java.util.logging.SimpleFormatter());
                        Bukkit.getLogger().addHandler(consoleHandler);
                        consoleHandler.setLive(true, line -> {
                            JsonObject response = new JsonObject();
                            response.addProperty("action", "CONSOLE_OUTPUT_LIVE");
                            response.addProperty("line", line);
                            response.addProperty("server", CaesarConnector.getInstance().getName());
                            sendE(response.toString());
                        });
                    } else {
                        consoleHandler.setLive(false, null);
                    }
                    break;
                case SERVER_STOP:
                    Bukkit.getScheduler().runTask(CaesarConnector.getInstance(), Bukkit::shutdown);
                    break;
                case SERVER_RESTART:
                    Bukkit.getScheduler().runTask(CaesarConnector.getInstance(), () -> Bukkit.spigot().restart());
                    break;
                case SERVER_EXECUTE_COMMAND:
                    String command = root.get("command").getAsString();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    break;
                case SERVER_INFO:
                    JsonObject o = new JsonObject();
                    o.addProperty("action", LinkAction.SERVER_INFO.toString());
                    o.addProperty("server", CaesarConnector.getInstance().getName());
                    o.addProperty("serverId", LocalStorage.getInstance().getData().getServerId().toString());
                    o.addProperty("serverVersion", serverVersion);
                    o.addProperty("maxPlayers", Bukkit.getMaxPlayers());
                    o.addProperty("onlinePlayers",  Bukkit.getOnlinePlayers().size());
                    JsonObject b = new JsonObject();
                    b.addProperty("bukkitVersion", Bukkit.getBukkitVersion());
                    b.addProperty("allowEnd", Bukkit.getAllowEnd());
                    b.addProperty("allowNether", Bukkit.getAllowNether());
                    b.addProperty("minecraftVersion", Bukkit.getMinecraftVersion());
                    b.addProperty("whitelist", Bukkit.hasWhitelist());
                    o.add("bukkit", b);
                    JsonArray plugins = new JsonArray();
                    for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
                        plugins.add(p.getName());
                    }
                    o.add("plugins", plugins);
                    o.addProperty("hasPlugman", Bukkit.getPluginManager().isPluginEnabled("PlugmanX"));

                    sendE(o.toString());
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.warning("Connection has been closed. Reason: " + s + ", Code: " + i);
        if (b) {
            log.info("Remote host closed connection.");
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (NotificationManager.getInstance().hasNotification(player.getUniqueId())) {
                    Audience.audience(player).sendActionBar(Component.text("§cCaesar has been disconnected."));
                }
            });
            if (schedulerRestart.isShutdown()) {
                schedulerRestart = Executors.newScheduledThreadPool(1);
                schedulerRestart.scheduleAtFixedRate(this::reconnect, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        log.severe("An error occurred: " + e.getMessage());
    }


    public void sendE(String text) {
        if (useEncryptedConnection) {
            try {
                send(encrypt(text, getConnectionKey()));
            } catch (Exception ex) {
                log.severe("Could not encrypt text for sending.");
                log.severe(ex.getMessage());
            }
        } else
            send(text);
    }

    public int pingServer() {
        long pingStart = System.currentTimeMillis();
        sendPingData();

        pingLatch = new CountDownLatch(1);
        try {
            if (pingLatch.await(3, TimeUnit.SECONDS)) {
                long nowMillis = System.currentTimeMillis();
                return Math.toIntExact(nowMillis - pingStart);
            } else return -2;
        } catch (InterruptedException ignored) {}
        return -1;
    }

    public void banPlayer(OfflinePlayer player, UUID bannedBy, String reason, long until) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_BAN.name());
        o.addProperty("toBan", player.getUniqueId().toString());
        o.addProperty("bannedBy", bannedBy.toString());
        o.addProperty("reason", reason);
        o.addProperty("until", until);
        sendE(o.toString());
    }

    public void mutePlayer(OfflinePlayer player, UUID mutedBy, String reason, long until) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_MUTE.name());
        o.addProperty("toMute", player.getUniqueId().toString());
        o.addProperty("mutedBy", mutedBy.toString());
        o.addProperty("reason", reason);
        o.addProperty("until", until);
        sendE(o.toString());
    }

    public void kickPlayer(Player player, UUID kickBy, String reason) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_KICK.name());
        o.addProperty("kicked", player.getUniqueId().toString());
        o.addProperty("kickedBy", kickBy.toString());
        o.addProperty("reason", reason);
        sendE(o.toString());
    }

    public void warnPlayer(OfflinePlayer player, UUID warnBy, String reason) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_WARN.name());
        o.addProperty("warned", player.getUniqueId().toString());
        o.addProperty("warnBy", warnBy.toString());
        o.addProperty("reason", reason);
        sendE(o.toString());
    }


    public void unbanPlayer(OfflinePlayer player, UUID warnBy) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_UNBAN.name());
        o.addProperty("banned", player.getUniqueId().toString());
        o.addProperty("unbanBy", warnBy.toString());
        sendE(o.toString());
    }
    public void unmutePlayer(OfflinePlayer player, UUID warnBy) {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PLAYER_UNMUTE.name());
        o.addProperty("muted", player.getUniqueId().toString());
        o.addProperty("unmuteBy", warnBy.toString());
        sendE(o.toString());
    }

    private void sendPong() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PONG.name());
        sendE(o.toString());
    }

    private void sendHandshake() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.HANDSHAKE.name());
        o.addProperty("serverName", LocalStorage.getInstance().getData().getServerName());
        o.addProperty("serverId", LocalStorage.getInstance().getData().getServerId().toString());
        send(o.toString());
    }

    private void sendPingData() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PING.name());
        sendE(o.toString());
    }



    private String decrypt(String encryptedBase64, byte[] key) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);

        byte[] ciphertext = new byte[encrypted.length - iv.length];
        System.arraycopy(encrypted, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, StandardCharsets.UTF_8);
    }

    private String encrypt(String plaintext, byte[] key) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] encrypted = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertext, 0, encrypted, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    private byte[] getConnectionKey() {
        String connKeyBase64 = LocalStorage.getInstance().getData().getConnectionKey();
        return Base64.getDecoder().decode(connKeyBase64);
    }

    public enum LinkAction {
        HANDSHAKE,
        DISCONNECT,
        TRANSFER_CONFIG,
        PING,
        PONG,
        SERVER_RESTART,
        SERVER_STOP,
        SERVER_EXECUTE_COMMAND,
        SERVER_SHOW_CONSOLE,
        SERVER_INFO,
        PLAYER_KICK,
        PLAYER_WARN,
        PLAYER_BAN,
        PLAYER_MUTE,
        PLAYER_UNBAN,
        PLAYER_UNMUTE,
        PLAYER_INFO,
        PLAYER_LIST,
        PLAYER_REPORT,
        PUNISHMENT_LIST,
        REPORT_CREATE,
        REPORT_UPDATE,
        REPORT_DELETE,
    }
}