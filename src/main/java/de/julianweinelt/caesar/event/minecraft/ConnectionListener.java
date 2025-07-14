package de.julianweinelt.caesar.event.minecraft;

import de.julianweinelt.caesar.feature.Registry;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ConnectionListener implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        //if (Registry.instance().isBanned(event.getPlayer().getUniqueId())) {
          //  event.disallow(PlayerLoginEvent.Result.KICK_BANNED, Component.text("You are banned from this server!"));
        //}
    }
}