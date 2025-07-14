package de.julianweinelt.caesar.feature;

import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

public class CaesarPermission {
    public static final Permission BAN_COMMAND = new Permission("caesar.command.ban");
    public static final Permission UNBAN_COMMAND = new Permission("caesar.command.unban");
    public static final Permission MUTE_COMMAND = new Permission("caesar.command.mute");
    public static final Permission UNMUTE_COMMAND = new Permission("caesar.command.unmute");
    public static final Permission KICK_COMMAND = new Permission("caesar.command.kick");
    public static final Permission WARN_COMMAND = new Permission("caesar.command.warn");
    public static final Permission REPORT_COMMAND = new Permission("caesar.command.report");
    public static final Permission NOTIFY = new Permission("caesar.notify");

    public static List<Permission> values() {
        List<Permission> p = new ArrayList<>();
        p.add(BAN_COMMAND);
        p.add(UNBAN_COMMAND);
        p.add(MUTE_COMMAND);
        p.add(UNMUTE_COMMAND);
        p.add(KICK_COMMAND);
        p.add(WARN_COMMAND);
        p.add(REPORT_COMMAND);
        p.add(NOTIFY);
        return p;
    }
}
