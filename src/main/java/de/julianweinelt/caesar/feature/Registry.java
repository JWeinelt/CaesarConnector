package de.julianweinelt.caesar.feature;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.commands.ReportCommand;
import de.julianweinelt.caesar.plugin.CPlugin;
import de.julianweinelt.caesar.reports.ReportManager;
import de.julianweinelt.caesar.reports.ReportView;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class Registry {
    @Getter
    private final List<CPlugin> plugins = new ArrayList<>();
    @Getter
    private final List<Feature> features = new ArrayList<>();

    private final Logger log = CaesarConnector.getInstance().getLogger();

    public static Registry instance() {
        return CaesarConnector.getInstance().getFeatureRegistry();
    }

    public void registerFeature(Feature f) {
        switch (f) {
            case REPORT_SYSTEM:
                CaesarConnector.getInstance().getServer().getCommandMap().register("report",
                        new ReportCommand("report").setAliases(List.of("reports")));
                CaesarConnector.getInstance().setReportManager(new ReportManager());
                features.add(Feature.REPORT_SYSTEM);
                log.info("Registered command: /report");
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

    public void registerExtension(CPlugin cPlugin) {
        log.info("Registering extension: " + cPlugin.getName());
        plugins.add(cPlugin);
    }

    public boolean featureActive(Feature f) {
        return features.contains(f);
    }
}