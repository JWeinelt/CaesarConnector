package de.julianweinelt.caesar.feature;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.commands.PunishCommand;
import de.julianweinelt.caesar.commands.ReportCommand;
import de.julianweinelt.caesar.plugin.CPlugin;
import de.julianweinelt.caesar.reports.ReportManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                CaesarConnector.getInstance().getServer().getCommandMap().register("report", "caesar",
                        new ReportCommand("report").setAliases(List.of("reports")));
                CaesarConnector.getInstance().setReportManager(new ReportManager());
                features.add(Feature.REPORT_SYSTEM);
                log.info("Registered command: /report");
                break;
            case BAN_SYSTEM:
                features.add(Feature.BAN_SYSTEM);
                CaesarConnector.getInstance().setPunishmentManager(new PunishmentManager());
                Bukkit.getPluginManager().registerEvents(PunishmentManager.instance(), CaesarConnector.getInstance());

                CaesarConnector.getInstance().getServer().getCommandMap().register("ban", "caesar",
                        new PunishCommand("ban").setAliases(List.of("bans", "mute", "unban", "unmute", "kick", "warn",
                                "punishments")));
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

    private void overrideVanillaCommands(boolean override) {
        if (!override) return;

        CommandMap commandMap = Bukkit.getCommandMap();

        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        knownCommands.remove("minecraft:ban");
        knownCommands.remove("minecraft:kick");
    }
}