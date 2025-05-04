package de.julianweinelt.caesar.feature;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.commands.ReportCommand;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;


public class FeatureRegistry {
    public static FeatureRegistry instance() {
        return CaesarConnector.getInstance().getFeatureRegistry();
    }

    public void registerFeature(Feature f) {
        switch (f) {
            case REPORT_SYSTEM:
                CaesarConnector.getInstance().getServer().getCommandMap().register("report",
                        new ReportCommand("report"));
                break;
            case BAN_SYSTEM:
                break;
        }
    }

    public void registerPermissions() {
        PluginManager manager = Bukkit.getPluginManager();
        for (Permission p : CaesarPermission.values()) {
            manager.removePermission(p.getName());
            manager.addPermission(p);
        }
    }
}