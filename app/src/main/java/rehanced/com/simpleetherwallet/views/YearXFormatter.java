package rehanced.com.simpleetherwallet.views;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class YearXFormatter implements IAxisValueFormatter {

    GregorianCalendar c = new GregorianCalendar();
    String[] months = new String[]{
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "Mai",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Okt",
            "Nov",
            "Dez"
    };

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        c.setTimeInMillis(((long) (value)) * 1000);
        return months[c.get(Calendar.MONTH)];
    }
}
