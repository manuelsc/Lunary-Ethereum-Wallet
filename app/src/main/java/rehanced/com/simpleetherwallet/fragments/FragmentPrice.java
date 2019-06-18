package rehanced.com.simpleetherwallet.fragments;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.views.DontShowNegativeFormatter;
import rehanced.com.simpleetherwallet.views.HourXFormatter;
import rehanced.com.simpleetherwallet.views.WeekXFormatter;
import rehanced.com.simpleetherwallet.views.YearXFormatter;


public class FragmentPrice extends Fragment {

    private LineChart priceChart;
    private TextView price, chartTitle;
    private SwipeRefreshLayout swipeLayout;
    private ImageView left, right;
    private MainActivity ac;
    private LinearLayout colorPadding;
    private LinearLayout priceSwitch;

    private static final int[] TIMESTAMPS = new int[]{
            86400, // 24 hours
            604800, // Week
            2678400, // Month
            31536000 // Year
    };

    private static String[] TITLE_TEXTS;

    private static final int[] PERIOD = new int[]{
            300,
            1800,
            14400,
            86400
    };

    private int displayType = 1;
    private boolean displayInUsd = true; // True = USD, False = BTC
    private boolean refreshChart = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_price, container, false);

        ac = (MainActivity) getActivity();
        TITLE_TEXTS = new String[]{
                getString(R.string.last_24_hours),
                getString(R.string.last_7_days),
                getString(R.string.last_30_days),
                getString(R.string.last_year)
        };

        priceChart = (LineChart) rootView.findViewById(R.id.chart1);
        chartTitle = (TextView) rootView.findViewById(R.id.chartTitle);
        price = (TextView) rootView.findViewById(R.id.price);
        left = (ImageView) rootView.findViewById(R.id.wleft);
        right = (ImageView) rootView.findViewById(R.id.wright);
        priceSwitch = ((LinearLayout) rootView.findViewById(R.id.priceSwitch));

        priceSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayInUsd = !displayInUsd;
                refreshChart = true;
                update(true);
                general();

                if (ac != null && ac.getPreferences() != null) {
                    SharedPreferences.Editor editor = ac.getPreferences().edit();
                    editor.putBoolean("price_displayInUsd", displayInUsd);
                    editor.apply();
                }
            }
        });

        if (ac != null && ac.getPreferences() != null)
            displayInUsd = ac.getPreferences().getBoolean("price_displayInUsd", true);

        if (ac != null && ac.getPreferences() != null)
            displayType = ac.getPreferences().getInt("displaytype_chart", 1);

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previous();
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
        colorPadding = (LinearLayout) rootView.findViewById(R.id.colorPadding);

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateExchangeRates();
                update(false);
            }
        });

        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).track("Price Fragment");
        }

        swipeLayout.setRefreshing(true);
        update(true);
        general();

        priceChart.setVisibility(View.INVISIBLE);
        return rootView;
    }

    private void next() {
        refreshChart = true;
        displayType = (displayType + 1) % PERIOD.length;
        general();
        update(true);
    }

    private void previous() {
        refreshChart = true;
        displayType = displayType > 0 ? displayType - 1 : PERIOD.length - 1;
        general();
        update(true);
    }

    private void general() {
        priceChart.setVisibility(View.INVISIBLE);
        chartTitle.setText(TITLE_TEXTS[displayType]);
        colorPadding.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLittleDarker));
        if (ac != null && ac.getPreferences() != null) {
            SharedPreferences.Editor editor = ac.getPreferences().edit();
            editor.putInt("displaytype_chart", displayType);
            editor.apply();
        }
    }

    private void loadPriceData(final long time, int period) throws IOException {

        EtherscanAPI.getInstance().getPriceChart((System.currentTimeMillis() / 1000) - time, period, displayInUsd, new Callback() { // 1467321600,
            @Override
            public void onFailure(Call call, IOException e) {
                if(ac == null) return;
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onItemsLoadComplete();
                            ac.snackError(getString(R.string.err_no_con), Snackbar.LENGTH_LONG);
                        } catch(Exception e){}
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final ArrayList<Entry> yVals = new ArrayList<Entry>();
                try {
                    JSONArray data = new JSONArray(response.body().string());
                    double exchangeRate = ExchangeCalculator.getInstance().getRateForChartDisplay();
                    float commas = displayInUsd ? 100 : 10000;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject o = data.getJSONObject(i);
                        yVals.add(new Entry(o.getLong("date"), (float) Math.floor(o.getDouble("high") * exchangeRate * commas) / commas));
                    }
                    if(ac == null) return;
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            priceChart.setVisibility(View.VISIBLE);
                            onItemsLoadComplete();
                            if (isAdded()) {
                                setupChart(priceChart, getData(yVals), getResources().getColor(R.color.colorPrimaryLittleDarker));
                                update(false);
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void setupChart(LineChart chart, LineData data, int color) {
        ((LineDataSet) data.getDataSetByIndex(0)).setCircleColorHole(color);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setBackgroundColor(color);
        chart.setViewPortOffsets(0, 23, 0, 0);
        chart.setData(data);
        Legend l = chart.getLegend();
        l.setEnabled(false);

        chart.getAxisLeft().setEnabled(true);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setSpaceTop(10);
        chart.getAxisLeft().setSpaceBottom(30);
        chart.getAxisLeft().setAxisLineColor(0xFFFFFF);
        chart.getAxisLeft().setTextColor(0xFFFFFF);
        chart.getAxisLeft().setDrawTopYLabelEntry(true);
        chart.getAxisLeft().setLabelCount(10);

        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setAxisLineColor(0xFFFFFF);
        chart.getXAxis().setTextColor(0xFFFFFF);

        Typeface tf = Typeface.DEFAULT;

        // X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.removeAllLimitLines();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);

        xAxis.setTextColor(Color.argb(150, 255, 255, 255));

        if (displayType == 1 || displayType == 2) // Week and Month
            xAxis.setValueFormatter(new WeekXFormatter());
        else if (displayType == 0) //  Day
            xAxis.setValueFormatter(new HourXFormatter());
        else
            xAxis.setValueFormatter(new YearXFormatter()); // Year

        // Y Axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setTypeface(tf);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(Color.argb(150, 255, 255, 255));
        leftAxis.setValueFormatter(new DontShowNegativeFormatter(displayInUsd));
        chart.getAxisRight().setEnabled(false); // Deactivates horizontal lines

        chart.animateX(1300);
        chart.notifyDataSetChanged();
    }

    private LineData getData(ArrayList<Entry> yVals) {
        LineDataSet set1 = new LineDataSet(yVals, "");
        set1.setLineWidth(1.45f);
        set1.setColor(Color.argb(240, 255, 255, 255));
        set1.setCircleColor(Color.WHITE);
        set1.setHighLightColor(Color.WHITE);
        set1.setFillColor(getResources().getColor(R.color.chartFilled));
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setDrawFilled(true);
        set1.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return priceChart.getAxisLeft().getAxisMinimum();
            }
        });

        LineData data = new LineData(set1);
        return data;
    }

    public void updateExchangeRates() {
        try {
            refreshChart = false;
            ExchangeCalculator.getInstance().updateExchangeRates(ac != null ? ac.getPreferences().getString("maincurrency", "USD") : "USD", ac);
            onItemsLoadComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(boolean updateChart) {
        if (price != null)
            price.setText(displayInUsd ?
                    ExchangeCalculator.getInstance().displayUsdNicely(ExchangeCalculator.getInstance().getUSDPrice()) + " " + ExchangeCalculator.getInstance().getMainCurreny().getName() :
                    ExchangeCalculator.getInstance().displayEthNicely(ExchangeCalculator.getInstance().getBTCPrice()) + " BTC"
            );

        if(refreshChart && updateChart) {
            try {
                loadPriceData(TIMESTAMPS[displayType], PERIOD[displayType]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshChart = true;
        }
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        if (swipeLayout == null) return;
        if (colorPadding == null) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                colorPadding.setBackgroundColor(0xF05a7899);
            }
        });

    }
}