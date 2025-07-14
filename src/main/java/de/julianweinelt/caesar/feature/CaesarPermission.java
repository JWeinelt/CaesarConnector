package de.julianweinelt.caesar.feature;

import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

public class CaesarPermission {
    public static final Permission BAN_COMMAND = new Permission("caesar.ban");
    public static final Permission UNBAN_COMMAND = new Permission("caesar.unban");
    public static final Permission MUTE_COMMAND = new Permission("caesar.mute");
    public static final Permission UNMUTE_COMMAND = new Permission("caesar.unmute");
    public static final Permission KICK_COMMAND = new Permission("caesar.kick");
    public static final Permission WARN_COMMAND = new Permission("caesar.warn");
    public static final Permission REPORT_COMMAND = new Permission("caesar.report");

    public static final Permission VIEW_REPORTS = new Permission("caesar.admin.reports.view");
    public static final Permission EDIT_REPORTS = new Permission("caesar.admin.reports.edit");
    public static final Permission NOTIFY_REPORTS = new Permission("caesar.admin.reports.notify");

    public static final Permission NOTIFY_PUNISHMENTS = new Permission("caesar.admin.punish.notify");

    public static List<Permission> values() {
        List<Permission> p = new ArrayList<>();
        p.add(BAN_COMMAND);
        p.add(UNBAN_COMMAND);
        p.add(MUTE_COMMAND);
        p.add(UNMUTE_COMMAND);
        p.add(KICK_COMMAND);
        p.add(WARN_COMMAND);
        p.add(REPORT_COMMAND);
        p.add(VIEW_REPORTS);
        p.add(EDIT_REPORTS);
        p.add(NOTIFY_REPORTS);
        p.add(NOTIFY_PUNISHMENTS);
        return p;
    }
}
