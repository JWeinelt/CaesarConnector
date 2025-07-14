package de.julianweinelt.caesar.connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.feature.Feature;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.reports.ReportManager;
import de.julianweinelt.caesar.reports.ReportView;
import de.julianweinelt.caesar.storage.LocalStorage;
import lombok.Getter;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CaesarLink extends WebSocketClient {

    private final Logger log = CaesarConnector.getInstance().getLogger();
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private boolean useEncryptedConnection = false;

    private ScheduledExecutorService schedulerRestart = Executors.newScheduledThreadPool(1);

    private long pingStart;
    private CountDownLatch pingLatch;

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
        String data = s;
        if (useEncryptedConnection) {
            try {
                data = decrypt(s, getConnectionKey());
            } catch (Exception e) {
                log.severe("Could not receive data from server.");
                log.severe("Encryption is enabled, but the text the server provided can't be decrypted.");
                log.severe(e.getMessage());
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
                    break;
                case DISCONNECT:
                    log.info("Received disconnect from Caesar.");
                    break;
                case TRANSFER_CONFIG:
                    log.info("Received configuration from Caesar.");
                    if (root.get("useReports").getAsBoolean()){
                        Registry.instance().registerFeature(Feature.REPORT_SYSTEM);
                        ReportManager.instance().setView(new ReportView()
                                .prepare(root.get("reports").getAsJsonObject())
                        );
                        log.info("Report system feature has been registered.");
                    }
                    log.info("Configuration transfer complete.");
                    break;
                case PONG:
                    pingLatch.countDown();
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

    public int pingServer() {
        pingStart = System.currentTimeMillis();
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

    public void sendHandshake() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.HANDSHAKE.name());
        o.addProperty("serverName", LocalStorage.getInstance().getData().getServerName());
        o.addProperty("serverId", LocalStorage.getInstance().getData().getServerId().toString());
        send(o.toString());
    }

    private void sendPingData() {
        JsonObject o = new JsonObject();
        o.addProperty("action", LinkAction.PING.name());
        send(o.toString());
    }



    public static String decrypt(String encryptedBase64, byte[] key) throws Exception {
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
    }
}