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
            complete(completions, args[0], "setup");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("setup")) {
                complete(completions, args[1], "key", "db");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("key")) {
                complete(completions, args[2], "<KEY>");
            }

            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[2], "mysql", "mariadb", "mssql", "postgresql", "oracle_sql");
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setup") && args[1].equalsIgnoreCase("db")) {
                complete(completions, args[3], "<HOST>");
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
