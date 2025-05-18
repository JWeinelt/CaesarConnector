package de.julianweinelt.caesar.event;

import de.julianweinelt.caesar.connection.CaesarLink;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CaesarLinkDisconnectEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final boolean erroneousState;
    private final CaesarLink link;

    public CaesarLinkDisconnectEvent(boolean erroneousState, CaesarLink link) {
        this.erroneousState = erroneousState;
        this.link = link;
    }

    public boolean isErroneousState() {
        return erroneousState;
    }

    public CaesarLink getLink() {
        return link;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}