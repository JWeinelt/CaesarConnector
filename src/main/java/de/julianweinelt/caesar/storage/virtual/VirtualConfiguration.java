package de.julianweinelt.caesar.storage.virtual;

import de.julianweinelt.caesar.storage.LocalStorage;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VirtualConfiguration {
    public static VirtualConfiguration instance() {
        return LocalStorage.getInstance().getVConf();
    }

    private String datePattern;
    private String timeZone;
}
