package de.julianweinelt.caesar.feature;

import de.julianweinelt.caesar.CaesarConnector;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PunishmentManager implements Listener {
    private final List<UUID> bannedPlayers = new ArrayList<>();
    private final List<UUID> mutedPlayers = new ArrayList<>();

    public static PunishmentManager instance() {
        if (CaesarConnector.getInstance().getPunishmentManager() == null) {
            throw new IllegalStateException("Punishments are not enabled on this server.");
        }
        return CaesarConnector.getInstance().getPunishmentManager();
    }

    public void banPlayer(OfflinePlayer p) {
        bannedPlayers.add(p.getUniqueId());
    }

    public void unbanPlayer(OfflinePlayer p) {
        bannedPlayers.remove(p.getUniqueId());
    }

    public void mutePlayer(OfflinePlayer p) {
        mutedPlayers.add(p.getUniqueId());
    }

    public void unmutePlayer(OfflinePlayer p) {
        mutedPlayers.remove(p.getUniqueId());
    }

    public void update(List<UUID> banned, List<UUID> muted) {
        bannedPlayers.addAll(banned);
        mutedPlayers.addAll(muted);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {
        if (bannedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.disallow(PlayerLoginEvent.Result.KICK_BANNED, Component.text("You are banned."));
            //TODO: Use custom message with %reason% placeholder
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent e) {
        if (mutedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou have been muted.");
        }
    }
}