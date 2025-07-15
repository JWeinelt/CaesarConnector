package de.julianweinelt.caesar.commands;

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

public class CaesarCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e§lCAESAR");
            sender.sendMessage("§eSystem version: §a" + CaesarLink.getInstance().getServerVersion());
            String status;
            if (CaesarLink.getInstance().isOpen()) status = "§a✔ Connected";
            else status = "§c❌ Disconnected";
            sender.sendMessage("§eStatus: " + status);
            sender.sendMessage("§eReports: " + ((Registry.instance().featureActive(Feature.REPORT_SYSTEM)) ? "§a✔ Active" : "§c❌ Not active"));
            sender.sendMessage("§ePunishments: " + ((Registry.instance().featureActive(Feature.BAN_SYSTEM)) ? "§a✔ Active" : "§c❌ Not active"));
            sender.sendMessage(
                    Component.text("§eExtensions loaded: ").append(Component.text("ℹ")
                            .hoverEvent(HoverEvent.showText(Component.text("§aExtensions are Caesar plugins that are compatible with Minecraft."))))
                            .append(Component.newline()).append(Component.text("§a" + Registry.instance().getPlugins().size()))
            );
            return false;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setup")) {
                sender.sendMessage("§e§lCaesar - Command usage");
                sender.sendMessage("§c/caesar setup db <type> <host> <port> <database> <user> <password>");
                sender.sendMessage("§c/caesar setup caesar <host> <port>");
                sender.sendMessage("§c/caesar setup key <key>");
                return false;
            } else if (args[0].equalsIgnoreCase("test")) {
                sender.sendMessage("§aSending a ping to the Caesar backend server...");
                int ping = CaesarLink.getInstance().pingServer();
                if (ping == -2) {
                    sender.sendMessage("§cCaesar couldn't get any answer. That seems strange...");
                } else if (ping == -1) {
                    sender.sendMessage("§cSomething went wrong. Please report this issue. Code: -1");
                } else {
                    String color = "a";
                    String info = "This is absolutely normal. Your server is working great!";
                    if (ping > 80) {
                        color = "c";
                        info = "Your ping is quite high. If the Backend is running on the same server, there might be a performance leak.";
                    }
                    if (ping > 50 && ping <= 80) {
                        color = "6";
                        info = "Your ping could be better. Everything's working, but expect minimal waiting times at some places.";
                    }
                    sender.sendMessage("§eGot a pong from backend: §" + color + ping + "ms");
                    sender.sendMessage(Component.text("§eGot a pong from backend: ").append(Component.text("§" + color + ping + "ms ")
                            .append(Component.text("§bℹ").hoverEvent(HoverEvent.showText(Component.text("§" + color + info))))));
                }
            }
        } else if (args.length == 3) {
             if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("key")) {
                LocalStorage.getInstance().getData().setConnectionKey(args[2]);
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