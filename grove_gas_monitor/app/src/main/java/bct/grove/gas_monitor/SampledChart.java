package bct.grove.gas_monitor;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;


public class SampledChart {
    private static final String SPACER = "  \u2027";

    private final int maxEntries;
    private final boolean showLegend;
    private final boolean showXAxis;
    private final String label;

    LineChart chart;
    ArrayList<Entry> entries1;
    ArrayList<Entry> entries2;
    ArrayList<Entry> entries3;
    ArrayList<Entry> entries4;

    protected boolean needsRefresh;


    public SampledChart(LineChart chart, String label, int maxEntries, boolean showLegend, boolean showXAxis) {
        this.label = label;
        this.maxEntries = maxEntries;
        this.showLegend = showLegend;
        this.showXAxis = showXAxis;
        this.chart = chart;
        needsRefresh = true;
        createChart();
    }

    private void createChart() {
        // we use 4 plots, each one has its own data entries
        entries1 = new ArrayList<>();
        entries2 = new ArrayList<>();
        entries3 = new ArrayList<>();
        entries4 = new ArrayList<>();

        // fill in the entry set with 0
        for (int i = 0; i < maxEntries; i++) {
            entries1.add(new Entry(i,0));
            entries2.add(new Entry(i,0));
            entries3.add(new Entry(i,0));
            entries4.add(new Entry(i,0));
        }

        // create line data sets for each sensor
        LineDataSet set1 = createLineDataSet("NO2" + SPACER, entries1, Color.rgb(200, 50, 50));
        LineDataSet set2 = createLineDataSet("C2H5OH" + SPACER, entries2, Color.rgb(200, 200, 50));
        LineDataSet set3 = createLineDataSet("VOC" + SPACER, entries3, Color.rgb(50, 200, 200));
        LineDataSet set4 = createLineDataSet("CO" + SPACER, entries4, Color.rgb(50, 50, 200));

        // put all sensor data lines into the same set in order to be displayed in the same graph
        LineData data = new LineData(set1, set2, set3, set4);
        data.setValueTextColor(Color.GRAY);
        data.setValueTextSize(9f);

        // tweak the visual properties of the graph
        chart.getLegend().setTextSize(20f);
        chart.getLegend().setFormSize(14);
        // hide the top chart's legend if required : it is displayed on the bottom chart
        chart.getLegend().setEnabled(showLegend);

        chart.getDescription().setText(label);
        chart.getDescription().setTextSize(16f);
        chart.getDescription().setPosition(40,16);
        chart.getDescription().setTextColor(Color.rgb(60, 60, 60));
        chart.getDescription().setTextAlign(Paint.Align.LEFT);
        // add padding around the chart
        chart.setExtraOffsets(5, 5, 5, 10);

        XAxis xaxis = chart.getXAxis();
        xaxis.setEnabled(showXAxis);

        // set the axis min and max values
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(3.3f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        // apply the data to the chart
        chart.setData(data);
    }

    private LineDataSet createLineDataSet(String label, ArrayList<Entry> entries, int color) {
        LineDataSet set;
        set = new LineDataSet(entries, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setCircleColor(color);
        //set.setCubicIntensity(0.1f);
        //set.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        set.setHighLightColor(Color.rgb(20,220,20));
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        return set;
    }

    private Entry updateEntry(Entry currentEntry, Entry previousEntry) {
        float val = currentEntry.getY();
        previousEntry.setY(val);
        return currentEntry;
    }


    public void onGasSampleChanged(float s1, float s2, float s3, float s4) {
        final int lastIndex = maxEntries - 1;
        entries1.get(lastIndex).setY(s1);
        entries2.get(lastIndex).setY(s2);
        entries3.get(lastIndex).setY(s3);
        entries4.get(lastIndex).setY(s4);

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        needsRefresh = true;
    }

    public void onGasSampled(float s1, float s2, float s3, float s4) {
        float val;
        Entry previousEntry1 = entries1.get(0);
        Entry previousEntry2 = entries2.get(0);
        Entry previousEntry3 = entries3.get(0);
        Entry previousEntry4 = entries4.get(0);

        // shuffle all entries to the left to accommodate the new value
        for (int i = 1; i < maxEntries; i++) {
            previousEntry1 = updateEntry(entries1.get(i), previousEntry1);
            previousEntry2 = updateEntry(entries2.get(i), previousEntry2);
            previousEntry3 = updateEntry(entries3.get(i), previousEntry3);
            previousEntry4 = updateEntry(entries4.get(i), previousEntry4);
            //System.out.print(" " + val);
        }
        // store the new value at the end of the entries
        previousEntry1.setY(s1);
        previousEntry2.setY(s2);
        previousEntry3.setY(s3);
        previousEntry4.setY(s4);

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        needsRefresh = true;
    }

    public void invalidate() {
        if (needsRefresh) {
            chart.invalidate();
        }
    }

    public void setVisible(boolean state) {
        chart.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
    }

}
