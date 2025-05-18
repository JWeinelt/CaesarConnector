package de.julianweinelt.caesar.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CaesarExtensionLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String pluginName;
    private final String pluginVersion;
    private final String[] pluginAuthor;
    private final String pluginDescription;

    public CaesarExtensionLoadEvent(String pluginName, String pluginVersion, String[] pluginAuthor,
                                    String pluginDescription) {
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        this.pluginAuthor = pluginAuthor;
        this.pluginDescription = pluginDescription;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String[] getPluginAuthor() {
        return pluginAuthor;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
