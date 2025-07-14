package de.julianweinelt.caesar.reports;

import de.julianweinelt.caesar.CaesarConnector;
import de.julianweinelt.caesar.feature.Feature;
import de.julianweinelt.caesar.feature.Registry;
import lombok.Getter;
import lombok.Setter;

public class ReportManager {
    @Getter
    @Setter
    private ReportView view;

    public static ReportManager instance() {
        if (!Registry.instance().featureActive(Feature.REPORT_SYSTEM)) throw new IllegalStateException("Report system not active");
        return CaesarConnector.getInstance().getReportManager();
    }
}