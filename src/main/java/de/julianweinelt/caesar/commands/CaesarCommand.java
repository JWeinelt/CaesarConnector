package de.julianweinelt.caesar.commands;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.connection.CaesarLink;
import de.julianweinelt.caesar.feature.Feature;
import de.julianweinelt.caesar.feature.Registry;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.StorageFactory;
import de.julianweinelt.caesar.storage.StorageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Base64;

public class CaesarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e§lCAESAR");
            sender.sendMessage("§eSystem version: §a" + CaesarConnector.getInstance().getLink().getServerVersion());
            String status;
            if (CaesarLink.getInstance().isOpen()) status = "§a✔ Connected";
            else status = "§c❌ Disconnected";
            sender.sendMessage("§eStatus: " + status);
            sender.sendMessage("§eReports: " + ((Registry.instance().featureActive(Feature.REPORT_SYSTEM)) ? "§a✔ Active" : "§c❌ Not active"));
            sender.sendMessage("§ePunishments: " + ((Registry.instance().featureActive(Feature.BAN_SYSTEM)) ? "§a✔ Active" : "§c❌ Not active"));
            sender.sendMessage(
                    Component.text("§eExtensions loaded: ").append(Component.text("⍰")
                            .hoverEvent(HoverEvent.showText(Component.text("§aExtensions are Caesar plugins that are compatible with Minecraft."))))
                            .append(Component.text("§a" + Registry.instance().getPlugins().size()))
            );
            return false;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setup")) {
                sender.sendMessage("§e§lCaesar - Command usage");
                sender.sendMessage("§c/caesar setup db <type> <host> <port> <database> <user> <password>");
                return false;
            }
        } else if (args.length == 2) {
             if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("generate-key")) {
                 byte[] key = new byte[32]; // 256 bits
                 SecureRandom random = new SecureRandom();
                 random.nextBytes(key);

                 String base64Key = Base64.getEncoder().encodeToString(key);

                LocalStorage.getInstance().getData().setConnectionKey(base64Key);
                sender.sendMessage("§aConnection key set.");
                sender.sendMessage("§aRestarting CaesarLink...");
                CaesarLink.getInstance().close();
                CaesarLink.getInstance().connect();
            }
        } else if (args.length == 8) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                StorageType type = StorageType.valueOf(args[2].toUpperCase());
                String host = args[3];
                int port = Integer.parseInt(args[4]);
                String database = args[5];
                String user = args[6];
                String password = args[7];
                LocalStorage s = LocalStorage.getInstance();
                s.getData().setDatabaseHost(host);
                s.getData().setDatabasePort(port);
                s.getData().setDatabaseName(database);
                s.getData().setDatabaseUser(user);
                s.getData().setDatabasePassword(password);
                s.getData().setStorageType(type);
                s.saveData();
                sender.sendMessage("§aDatabase settings saved.");
                sender.sendMessage("§aRestarting database now...");
                StorageFactory.getInstance().getStorage().disconnect();
                StorageFactory.getInstance().createStorage();
                StorageFactory.getInstance().getStorage().connect();
                sender.sendMessage("§aDatabase restarted.");
            }
        }
        return false;
    }
}