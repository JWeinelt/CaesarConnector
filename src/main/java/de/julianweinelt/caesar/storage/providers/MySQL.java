package de.julianweinelt.caesar.storage.providers;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.Storage;
import de.julianweinelt.caesar.storage.StorageFactory;
import org.bukkit.Bukkit;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class MySQL extends Storage {
    private final Logger log = CaesarConnector.getInstance().getLogger();

    public static Storage instance() {
        return StorageFactory.getInstance().getStorage();
    }

    @Override
    public void connect() {
        final String DRIVER = "com.mysql.cj.jdbc.Driver";
        final String PARAMETERS = "?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        final String URL = "jdbc:mysql://" + LocalStorage.getInstance().getData().getDatabaseHost()
                + ":" + LocalStorage.getInstance().getData().getDatabasePort() + "/" +
                LocalStorage.getInstance().getData().getDatabaseName() + PARAMETERS;
        final String USER = LocalStorage.getInstance().getData().getDatabaseUser();
        final String PASSWORD = LocalStorage.getInstance().getData().getDatabasePassword();

        try {
            Class.forName(DRIVER);

            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            log.info("Connected to MySQL database: " + URL);
            conn.createStatement().execute("USE " + LocalStorage.getInstance().getData().getDatabaseName());
        } catch (Exception e) {
            log.severe("Failed to connect to MySQL database: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendServerData() {
        UUID id = LocalStorage.getInstance().getData().getServerId();
        String name = LocalStorage.getInstance().getData().getServerName();
        int players = Bukkit.getOnlinePlayers().size();
        OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        double cpu = bean.getSystemLoadAverage();
        int memory = (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576);
        double tps = Bukkit.getServer().getTPS()[0];

        try {
            PreparedStatement pS = conn.prepareStatement("INSERT INTO server_data (UUID, Name, Players, cpu, memory, TPS) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");
            pS.setString(1, id.toString());
            pS.setString(2, name);
            pS.setInt(3, players);
            pS.setFloat(4, (float) cpu);
            pS.setInt(5, memory);
            pS.setInt(6, (int) tps);
            pS.execute();
        } catch (SQLException e) {
            printStackStrace(e);
        }
    }

    @Override
    public void loadPlayers() {
        playerNames.clear();
        try (ResultSet set = conn.createStatement().executeQuery("SELECT * FROM mc_players")) {
            while (set.next()) {
                playerNames.put(UUID.fromString(set.getString(1)), set.getString(2));
            }
        } catch (SQLException e) {
            printStackStrace(e);
        }
    }
}