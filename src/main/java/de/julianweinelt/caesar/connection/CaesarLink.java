package de.julianweinelt.caesar.connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.feature.Feature;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.storage.LocalStorage;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class CaesarLink extends WebSocketClient {

    private final Logger log = CaesarConnector.getInstance().getLogger();

    private ScheduledExecutorService schedulerRestart = Executors.newScheduledThreadPool(1);

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

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        schedulerRestart.shutdown();
        log.info("Connection has been opened.");
        log.info("Sending handshake...");
        sendHandshake();
    }

    @Override
    public void onMessage(String s) {
        JsonObject root = JsonParser.parseString(s).getAsJsonObject();
        if (root.has("action")) {
            switch (LinkAction.valueOf(root.get("action").getAsString())) {
                case HANDSHAKE:
                    log.info("Received handshake from Caesar.");
                    log.info("Caesar has been connected.");
                    serverVersion = root.get("serverVersion").getAsString();
                    log.info("Server version: " + serverVersion);
                    break;
                case DISCONNECT:
                    log.info("Received disconnect from Caesar.");
                    break;
                case TRANSFER_CONFIG:
                    log.info("Received configuration from Caesar.");
                    if (root.get("useReports").getAsBoolean()) Registry.instance()
                            .registerFeature(Feature.REPORT_SYSTEM);
                    log.info("Configuration transfer complete.");
                    break;
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.info("Connection has been closed. Reason: " + s + ", Code: " + i);
        if (b) {
            log.info("Remote host closed connection.");
            schedulerRestart = Executors.newScheduledThreadPool(1);
            schedulerRestart.scheduleAtFixedRate(this::reconnect, 10, 30, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    @Override
    public void onError(Exception e) {
        log.severe("An error occurred: " + e.getMessage());
    }

    public void sendHandshake() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.HANDSHAKE.name());
        o.addProperty("serverName", LocalStorage.getInstance().getData().getServerName());
        o.addProperty("serverId", LocalStorage.getInstance().getData().getServerId().toString());
        send(o.toString());
    }

    public enum LinkAction {
        HANDSHAKE,
        DISCONNECT,
        TRANSFER_CONFIG
    }
}