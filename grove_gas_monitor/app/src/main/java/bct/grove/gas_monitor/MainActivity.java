package bct.grove.gas_monitor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.text.DecimalFormat;


import bct.hwapi.HardwareConfiguration;
import bct.hwapi.SysInfo;
import bct.inoapi.InoCurator;

public class MainActivity extends Activity implements GasListener {

    private static final String MSG_CONNECTION_ERROR = "Error: invalid sensor values!\nCheck the sensor connection.";
    private static final DecimalFormat FORMAT = new DecimalFormat("0.000");

    SampledChart minuteChart;
    SampledChart hourChart;
    SampledChart dayChart;

    boolean showDayChart;

    SampleHistory sampleHistory;
    Runnable chartRefresh;
    private int errorCounter;
    TextView sensorValue1;
    TextView sensorValue2;
    TextView sensorValue3;
    TextView sensorValue4;

    TextView sensorXppm1;
    TextView sensorXppm2;
    TextView sensorXppm3;
    TextView sensorXppm4;


    XPpmCalc xPpmCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSensorUiLabels();
        createCharts();

        sampleHistory = new SampleHistory();

        // Start the InoSketch that communicates with the sensor and samples the gas
        // values every 2 seconds.
        InoCurator.runSketch(new GasInoSketch(this, 2000));

        setButtonHandlers();

        chartRefresh = new Runnable() {
            @Override
            public void run() {
                minuteChart.invalidate();
                hourChart.invalidate();
                dayChart.invalidate();
            }
        };

        // Create XPPM calculator to convert measured sensor voltages to Experimental PPM values.
        // The calculator requires the VCC level of the Grove module. TM1 modules expose 3.06V,
        // other BCT's HMI boards use 3.3V on VCC pin of the expansion header.
        float vcc = SysInfo.getHardwareConfiguration() == HardwareConfiguration.BCT_DB1 ? 3.3f : 3.06f;
        xPpmCalc = XPpmCalc.getInstance(vcc);
    }

    // Get the UI labels for displaying sensor's values
    private void getSensorUiLabels() {
        sensorValue1 = findViewById(R.id.tv_val1);
        sensorValue2 = findViewById(R.id.tv_val2);
        sensorValue3 = findViewById(R.id.tv_val3);
        sensorValue4 = findViewById(R.id.tv_val4);

        sensorXppm1 = findViewById(R.id.tv_xppm1);
        sensorXppm2 = findViewById(R.id.tv_xppm2);
        sensorXppm3 = findViewById(R.id.tv_xppm3);
        sensorXppm4 = findViewById(R.id.tv_xppm4);
    }

    private void createCharts() {
        minuteChart = new SampledChart(
                findViewById(R.id.chart1),
                "Last Minute",
                30,
                false,
                false
        );
        hourChart = new SampledChart(
                findViewById(R.id.chart2),
                "Last Hour",
                60,
                true,
                true
        );
        dayChart = new SampledChart(
                findViewById(R.id.chart3),
                "Last 24 Hrs",
                24,
                true,
                true
        );
        dayChart.setVisible(false);
        showDayChart = false;
    }

    private void setButtonHandlers() {
        Button button;

        // Hour button handler
        button = (Button) findViewById(R.id.button_hour);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showDayChart) {
                    return;
                }
                showDayChart = false;
                dayChart.setVisible(false);
                hourChart.setVisible(true);
            }
        });

        // Day button handler
        button = (Button) findViewById(R.id.button_day);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showDayChart) {
                    return;
                }
                showDayChart = true;
                hourChart.setVisible(false);
                dayChart.setVisible(true);
            }
        });
   }

    @Override
    protected void onStop() {
        InoCurator.stop();
        super.onStop();
        System.exit(0);
    }

    private void updateSensorValuePanel (float s1, float s2, float s3, float s4, float x1, float x2, float x3, float x4) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (s1 < 0) {
                    sensorValue1.setText("N/A");
                    sensorValue2.setText("N/A");
                    sensorValue3.setText("N/A");
                    sensorValue4.setText("N/A");

                    sensorXppm1.setText("");
                    sensorXppm2.setText("");
                    sensorXppm3.setText("");
                    sensorXppm4.setText("");

                }
                sensorValue1.setText(FORMAT.format(s1).concat(" V"));
                sensorValue2.setText(FORMAT.format(s2).concat(" V"));
                sensorValue3.setText(FORMAT.format(s3).concat(" V"));
                sensorValue4.setText(FORMAT.format(s4).concat(" V"));

                sensorXppm1.setText(Integer.toString((int)x1).concat(" xppm"));
                sensorXppm2.setText(Integer.toString((int)x2).concat(" xppm"));
                sensorXppm3.setText(Integer.toString((int)x3).concat(" xppm"));
                sensorXppm4.setText(Integer.toString((int)x4).concat(" xppm"));
            }
        });
    }

    @Override
    // This method is called from within the GasInoSketch when the new values
    // are sampled from the sensors.
    public void onGasSampled(float s1, float s2, float s3, float s4) {
        float val;
        System.out.println("gas sensor voltage:" + s1 + ", " + s2 + ", " + s3 + ", " + s4);

        // Handle invalid values - when a communication error occurred.
        if (s1 == 0f && s2 == 0f && s3 == 0f && s4 == 0f) {
            errorCounter++;
            // Display the message after 2 consecutive errors.
            if (errorCounter == 2) {
                displayMessage(MSG_CONNECTION_ERROR);
            }

            // Ensure the message is shown repeatedly if the errors persist.
            else if (errorCounter >= 10) {
                errorCounter = 0;
            }
            updateSensorValuePanel(-1, 0, 0, 0, 0, 0, 0, 0);
            return;
        }

        // convert voltages to Experimental PPM values
        float x1 = xPpmCalc.getXppm102B(s1);
        float x2 = xPpmCalc.getXppm302B(s2);
        float x3 = xPpmCalc.getXppm502B(s3);
        float x4 = xPpmCalc.getXppm702B(s4);

        // Display the numeric values in the top right panel.
        updateSensorValuePanel(s1, s2, s3, s4, x1, x2, x3, x4);
        errorCounter = 0;

        // add the new sample to the Minute chart
        minuteChart.onGasSampled(s1, s2, s3, s4);

        // add the new sample to the history collector
        boolean minutePassed = sampleHistory.addSample(s1, s2, s3, s4);

        // update the bottom charts (both Day and Hour) once every minute
        if (minutePassed) {
            updateBottomChart();
        }

        // refresh the charts on the screen
        runOnUiThread(chartRefresh);
    }

    private void updateBottomChart() {
        // update hour chart
        HistoryRecord hr = sampleHistory.getLastMinuteRecord();
        System.out.println("Minute average: " + (float)hr.s1 + ", " + (float)hr.s2 + ", " + (float)hr.s3 + ", " + (float)hr.s4);
        hourChart.onGasSampled((float) hr.s1, (float) hr.s2, (float) hr.s3, (float) hr.s4);

        //update day chart
        hr = sampleHistory.getLastHourRecord();
        if (sampleHistory.isHourEvent()) {
            dayChart.onGasSampled((float) hr.s1, (float) hr.s2, (float) hr.s3, (float) hr.s4);
        } else {
            dayChart.onGasSampleChanged((float) hr.s1, (float) hr.s2, (float) hr.s3, (float) hr.s4);
        }
    }

    private void displayMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}