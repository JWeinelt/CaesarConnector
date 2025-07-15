package de.julianweinelt.caesar.feature;

import de.julianweinelt.caesar.CaesarConnector;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotificationManager {
    private final List<UUID> notifications = new ArrayList<>();

    public static NotificationManager getInstance() {
        return CaesarConnector.getInstance().getNotificationManager();
    }

    public void addPlayer(UUID uuid) {
        notifications.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        notifications.remove(uuid);
    }

    public boolean hasNotification(UUID uuid) {
        return notifications.contains(uuid);
    }

    public void toggle(Player player) {
        if (hasNotification(player.getUniqueId())) {
            removePlayer(player.getUniqueId());
            player.sendMessage("§aYou won't receive notifications from Caesar now.");
        } else {
            addPlayer(player.getUniqueId());
            player.sendMessage("§aYou will receive important notifications from Caesar now.");
        }
    }
}
