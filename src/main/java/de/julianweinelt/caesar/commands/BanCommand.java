package de.julianweinelt.caesar.commands;

import de.julianweinelt.caesar.connection.CaesarLink;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BanCommand extends BukkitCommand {
    public BanCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            Player toBan = Bukkit.getPlayer(args[0]);
            Player player = (Player) sender;
            if (toBan != null) {
                //CaesarLink.getInstance().banPlayer(toBan.getUniqueId(), player.getUniqueId(), "No reason given.");
            }
        }
        return false;
    }
}
