package rehanced.com.simpleetherwallet.views;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class DontShowNegativeFormatter implements IAxisValueFormatter {

    private boolean dispalyInUsd;

    public DontShowNegativeFormatter(boolean dispalyInUsd) {
        this.dispalyInUsd = dispalyInUsd;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (dispalyInUsd) {
            return value >= 0 ? ((int) value) + "" : "";
        } else {
            return value >= 0 ? Math.floor(value * 1000) / 1000 + "" : "";
        }
    }
}
