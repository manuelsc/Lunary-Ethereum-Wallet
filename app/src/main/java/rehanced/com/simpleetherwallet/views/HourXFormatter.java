package rehanced.com.simpleetherwallet.views;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class HourXFormatter implements IAxisValueFormatter {

    GregorianCalendar c = new GregorianCalendar();

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        c.setTimeInMillis(((long) (value)) * 1000);
        return c.get(Calendar.HOUR_OF_DAY) + ":00 ";
    }
}
