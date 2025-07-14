package de.julianweinelt.caesar.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReportCommand extends BukkitCommand {
    public ReportCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("report")) {
            if (args.length == 0) {

            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args)
            throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();
        if (label.equalsIgnoreCase("report")) {
            List<String> players = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
            completions = StringUtil.copyPartialMatches(args[0], completions, players);
        }
        return completions;
    }
}