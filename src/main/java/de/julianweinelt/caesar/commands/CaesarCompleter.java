package de.julianweinelt.caesar.commands;

import de.codeblocksmc.codelib.chat.AdvancedTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CaesarCompleter extends AdvancedTabCompleter implements TabCompleter {
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length <= 1) {
            complete(completions, args[0], "setup", "extension", "test", "notify");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setup")) {
                complete(completions, args[1], "key", "db", "caesar", "language", "encryption");
            } else if (args[0].equalsIgnoreCase("extension")) {
                complete(completions, args[1], "list", "reload", "disable", "enable", "load", "unload");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[2], "mysql", "mariadb", "mssql", "postgresql", "oracle_sql");
            }
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("caesar")) {
                complete(completions, args[2], "<host>");
            }
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("key")) {
                complete(completions, args[2], "<secret>");
            }
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("language")) {
                complete(completions, args[2], "list", "download");
            }
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("encryption")) {
                complete(completions, args[2], "enable", "disable", "status");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[3], "<HOST>");
            }

            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("language")
            && args[2].equalsIgnoreCase("download")) {
                //TODO: Display downloadable language files
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[4], "<PORT>");
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[5], "<DATABASE>");
            }
        } else if (args.length == 7) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[6], "<USERNAME>");
            }
        } else if (args.length == 8) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[7], "<PASSWORD>");
            }
        }
        return completions;
    }

}
