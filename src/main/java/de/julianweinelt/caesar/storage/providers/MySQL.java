package de.julianweinelt.caesar.storage.providers;

import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.Storage;
import org.bukkit.Bukkit;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL extends Storage {
    @Override
    public void connect() {

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
}