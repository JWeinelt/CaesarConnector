package de.julianweinelt.caesar.commands;

import de.julianweinelt.caesar.connection.CaesarLink;
import de.julianweinelt.caesar.feature.PunishmentManager;
import de.julianweinelt.caesar.storage.virtual.VirtualConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PunishCommand extends BukkitCommand {
    public PunishCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        UUID senderUUID;
        if (!(sender instanceof Player player)) {
            senderUUID = UUID.fromString("b1814b18-664e-4d4c-9a0d-2151fbc5e8ef");
        } else senderUUID = player.getUniqueId();

        if (label.equalsIgnoreCase("ban")) {
            if (args.length == 0) {
                sender.sendMessage("§cPlease provide a player to ban!");
            }
            if (args.length == 1) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().banPlayer(toBan);
                CaesarLink.getInstance().banPlayer(toBan, senderUUID, "Not provided", -1);
                sender.sendMessage("§eThe player§c " +  args[0] + "§e has been banned.");
            } else if (args.length == 2) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().banPlayer(toBan);
                long time = parseTimeOffset(args[1]);
                CaesarLink.getInstance().banPlayer(toBan, senderUUID, "", time);
                sender.sendMessage("§eThe player§c " +  args[0] + "§e has been banned until §c" + formatUnixTime(time) + "!");
            } else if (args.length >= 3) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().banPlayer(toBan);
                long time = parseTimeOffset(args[1]);
                StringBuilder reason = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
                CaesarLink.getInstance().banPlayer(toBan, senderUUID, reason.toString(), time);
                sender.sendMessage(Component.text("§eThe player§c " + args[0] + "§e has been banned until §c"
                + formatUnixTime(time) + "§e!").append(Component.text(" §b[View reason]")
                        .hoverEvent(HoverEvent.showText(Component.text(reason.toString())))));
            }
        } else if (label.equalsIgnoreCase("kick")) {
            if (args.length == 1) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage("§cPlease name a valid player. The player must be online!");
                    return false;
                }
                CaesarLink.getInstance().kickPlayer(player, senderUUID, "Not provided");
                player.kick(Component.empty());
                sender.sendMessage("§c" + player.getName() + " has been kicked.");
            } else if (args.length == 2) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage("§cPlease name a valid player. The player must be online!");
                    return false;
                }
                CaesarLink.getInstance().kickPlayer(player, senderUUID, args[1]);
                player.kick(Component.text(args[1]));
                sender.sendMessage("§c" + player.getName() + " has been kicked.");
            }
        } else if (label.equalsIgnoreCase("warn")) {
            if (args.length == 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                CaesarLink.getInstance().warnPlayer(player, senderUUID, "Not provided");
                sender.sendMessage("§c" + player.getName() + " has been warned.");
            } else if (args.length == 2) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                CaesarLink.getInstance().warnPlayer(player, senderUUID, args[1]);
                sender.sendMessage("§c" + player.getName() + " has been warned.");
            }
        } else if (label.equalsIgnoreCase("mute")) {
            if (args.length == 0) {
                sender.sendMessage("§cPlease provide a player to mute!");
            }
            if (args.length == 1) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().mutePlayer(toBan);
                CaesarLink.getInstance().mutePlayer(toBan, senderUUID, "Not provided", -1);
                sender.sendMessage("§eThe player§c " +  args[0] + "§e has been muted.");
            } else if (args.length == 2) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().mutePlayer(toBan);
                long time = parseTimeOffset(args[1]);
                CaesarLink.getInstance().mutePlayer(toBan, senderUUID, "", time);
                sender.sendMessage("§eThe player§c " +  args[0] + "§e has been muted until §c" + formatUnixTime(time) + "!");
            } else if (args.length >= 3) {
                OfflinePlayer toBan = Bukkit.getOfflinePlayer(args[0]);

                PunishmentManager.instance().mutePlayer(toBan);
                long time = parseTimeOffset(args[1]);
                StringBuilder reason = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    reason.append(args[i]).append(" ");
                }
                CaesarLink.getInstance().mutePlayer(toBan, senderUUID, reason.toString(), time);
                sender.sendMessage(Component.text("§eThe player§c " + args[0] + "§e has been muted until §c"
                        + formatUnixTime(time) + "§e!").append(Component.text(" §b[View reason]")
                        .hoverEvent(HoverEvent.showText(Component.text(reason.toString())))));
            }
        } else if (label.equalsIgnoreCase("unban")) {
            if (args.length == 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                PunishmentManager.instance().unbanPlayer(player);
                CaesarLink.getInstance().unbanPlayer(player, senderUUID);
            }
        } else if (label.equalsIgnoreCase("unmute")) {
            if (args.length == 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
                PunishmentManager.instance().unmutePlayer(player);
                CaesarLink.getInstance().unmutePlayer(player, senderUUID);
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();

        if (label.equalsIgnoreCase("ban") || label.equalsIgnoreCase("mute")) {
            if (args.length <= 1) {
                List<String> players = new ArrayList<>();
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) players.add(player.getName());
                complete(completions, args[0], players);
            } else if (args.length == 2) {
                List<String> comp = new ArrayList<>(List.of("permanent"));
                comp.addAll(timeCompletion(args[1]));
                complete(completions, args[1], comp);
            } else if (args.length == 3) {
                complete(completions, args[2], "<reason>");
            }
        } else if (label.equalsIgnoreCase("warn") || label.equalsIgnoreCase("kick")) {
            if (args.length <= 1) {
                List<String> players = new ArrayList<>();
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) players.add(player.getName());
                complete(completions, args[0], players);
            } else if (args.length == 2) {
                complete(completions, args[1], "<reason>");
            }
        }
        return completions;
    }

    private void complete(List<String> list, String arg, String... completions) {
        list.clear();
        StringUtil.copyPartialMatches(arg, Arrays.asList(completions), list);
    }

    private void complete(List<String> list, String arg, List<String> completions) {
        list.clear();
        StringUtil.copyPartialMatches(arg, completions, list);
    }

    private void completeEmpty(List<String> list) {
        list.clear();
    }


    private List<String> timeCompletion(String input) {
        List<String> units = List.of("y", "j", "mm", "d", "t", "h", "st", "m", "s");

        String[] parts = input.split(";");
        String lastPart = parts[parts.length - 1].trim();

        if (lastPart.matches("\\d*")) {
            return units.stream()
                    .map(unit -> lastPart + unit)
                    .collect(Collectors.toList());
        }

        if (lastPart.matches("\\d+(y|j|mm|d|t|h|st|m|s)")) {
            return List.of(input + ";1");
        }

        if (lastPart.endsWith(";")) {
            return List.of(input + "1");
        }

        return Collections.emptyList();
    }

    private long parseTimeOffset(String input) {
        long now = System.currentTimeMillis();
        long offsetMillis = 0;

        String[] parts = input.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            String numberStr = part.replaceAll("[^0-9]", "");
            String unit = part.replaceAll("[0-9]", "");

            if (numberStr.isEmpty() || unit.isEmpty()) continue;

            long number = Long.parseLong(numberStr);

            switch (unit.toLowerCase()) {
                case "y": case "j":
                    offsetMillis += number * 365L * 24 * 60 * 60 * 1000;
                    break;
                case "mm":
                    offsetMillis += number * 30L * 24 * 60 * 60 * 1000;
                    break;
                case "d": case "t":
                    offsetMillis += number * 24L * 60 * 60 * 1000;
                    break;
                case "h": case "st":
                    offsetMillis += number * 60L * 60 * 1000;
                    break;
                case "m":
                    offsetMillis += number * 60L * 1000;
                    break;
                case "s":
                    offsetMillis += number * 1000;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown unit: " + unit);
            }
        }

        return now + offsetMillis;
    }

    private String formatUnixTime(long unixMillis) {
        ZonedDateTime dateTime = Instant.ofEpochMilli(unixMillis)
                .atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(VirtualConfiguration.instance().getDatePattern());
        return formatter.format(dateTime);
    }


}
