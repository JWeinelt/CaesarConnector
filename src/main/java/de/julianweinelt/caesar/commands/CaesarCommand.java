package de.julianweinelt.caesar.commands;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.connection.CaesarLink;
import de.julianweinelt.caesar.storage.LocalStorage;
import de.julianweinelt.caesar.storage.StorageType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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
            return false;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setup")) {
                sender.sendMessage("§e§lCaesar - Command usage");
                sender.sendMessage("§c/caesar setup db <type> <host> <port> <database> <user> <password>");
                return false;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("key")) {
                String key = args[2];
                LocalStorage.getInstance().getData().setConnectionKey(key);
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
            }
        }
        return false;
    }
}