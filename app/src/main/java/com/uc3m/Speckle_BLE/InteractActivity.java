package com.uc3m.Speckle_BLE;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

import static android.view.View.*;

public class InteractActivity extends AppCompatActivity {

    public static final String EXTRA_DEVICE_ADDRESS = "mAddress";

    private final GattClient mGattClient = new GattClient();
    private Button mStartButton;
    private Button mStopButton;
    private Button mShowButton;
    private TextView tvStatus;
    private TextView tvBatteryLevel;
    private ImageView bat100;
    private ImageView bat80;
    private ImageView bat60;
    private ImageView bat40;
    private ImageView bat20;

    // Initialize graph data
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    private long tStart = 0;
    private boolean Start = true;

    private long tNow = 0;
    private long tElapsed = 0;
    private int width = 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interact_activity);

        // we get graph view instance
        final GraphView graph= (GraphView) findViewById(R.id.graph);
        // data
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        // customize viewport and interface
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(false);
        viewport.setMinY(40);
        viewport.setMaxY(180);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(width);
        viewport.setScrollable(true);
        graph.getGridLabelRenderer().setVerticalAxisTitle("bpm");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("time(s)");

        //series.setColor(R.color.colorAccent);


        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);
        mShowButton = findViewById(R.id.interact_button);

        tvStatus = findViewById(R.id.tv_Connection_Status);
        tvBatteryLevel = findViewById(R.id.tv_Battery);

        bat100 = findViewById(R.id.BAT100);
        bat80 = findViewById(R.id.BAT80);
        bat60 = findViewById(R.id.BAT60);
        bat40 = findViewById(R.id.BAT40);
        bat20 = findViewById(R.id.BAT20);

        mStartButton.setEnabled(false);
        mStopButton.setEnabled(false);
        mShowButton.setEnabled(false);

        mStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGattClient.sendClick(10);
                Toast.makeText(InteractActivity.this, "Let's start!", Toast.LENGTH_SHORT).show();
                mShowButton.setText(String.format("Reading bpm. \n Please Wait :)"));
                mStopButton.setEnabled(true);
                graph.removeSeries(series);
                series = new LineGraphSeries<DataPoint>();
                series.setColor(Color.argb(100, 216, 27, 96));
                series.setThickness(10);
                graph.addSeries(series);
                Start = true;
                mStartButton.setEnabled(false);

            }
        });

        mStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGattClient.sendClick(20);
                Toast.makeText(InteractActivity.this, "System stopped", Toast.LENGTH_SHORT).show();
                mStopButton.setEnabled(false);
                mStartButton.setEnabled(true);

            }
        });

        String address = getIntent().getStringExtra(EXTRA_DEVICE_ADDRESS);
        mGattClient.onCreate(this, address, new GattClient.OnHeartRateReadListener() {
            @Override
            public void onHeartRateRead(final String value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Start){
                            tStart =  System.currentTimeMillis();
                            Start = false;
                        }
                        // convert to integer
                        int int_value;
                        try {
                            int_value = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            int_value = 0;
                        }

                        mStopButton.setEnabled(true);

                        // update the value
                        mShowButton.setText(String.format("%d bpm",int_value));

                        // update the graph
                        tNow = System.currentTimeMillis();
                        tElapsed = (int) (tNow - tStart) / 1000;

                        // here, we choose to display max 60 points on the viewport and we scroll to end
                        if (tElapsed < width){
                            series.appendData(new DataPoint(tElapsed, int_value), false, width);
                        }else {
                            series.appendData(new DataPoint(tElapsed, int_value), true, width);
                        }
                    }
                });
            }

            @Override
            public void onBatteryRead(final int value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // update the value
                        tvBatteryLevel.setText(String.format("Battery level: %d",value));
                        setBatteryIcon(value);
                    }
                });
            }

            @Override
            public void onConnected(final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStartButton.setEnabled(success);
                        if (success) {
                            tvStatus.setText("Status: Connected");

                        }
                        if (!success) {
                            Toast.makeText(InteractActivity.this, "Connection error", Toast.LENGTH_LONG).show();
                            tvStatus.setText("Status: Connection error");
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan, menu);
        menu.findItem(R.id.menu_info).setVisible(true);
        menu.findItem(R.id.menu_stop).setVisible(false);
        menu.findItem(R.id.menu_scan).setVisible(false);
        menu.findItem(R.id.menu_scan).setEnabled(false);
        menu.findItem(R.id.menu_stop).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_info:
                startInfoActivity();
                break;
        }
        return true;
    }

    private void startInfoActivity(){
        Intent intent = new Intent(this,InfoActivity.class);
        startActivity(intent);
    }

    public void setBatteryIcon(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value > 80) {
                    bat100.setVisibility(View.VISIBLE);
                    bat80.setVisibility(View.GONE);
                    bat60.setVisibility(View.GONE);
                    bat40.setVisibility(View.GONE);
                    bat20.setVisibility(View.GONE);
                } else if (value > 60) {
                    bat100.setVisibility(View.GONE);
                    bat80.setVisibility(View.VISIBLE);
                    bat60.setVisibility(View.GONE);
                    bat40.setVisibility(View.GONE);
                    bat20.setVisibility(View.GONE);
                } else if (value > 40) {
                    bat100.setVisibility(View.GONE);
                    bat80.setVisibility(View.GONE);
                    bat60.setVisibility(View.VISIBLE);
                    bat40.setVisibility(View.GONE);
                    bat20.setVisibility(View.GONE);
                } else if (value > 20) {
                    bat100.setVisibility(View.GONE);
                    bat80.setVisibility(View.GONE);
                    bat60.setVisibility(View.GONE);
                    bat40.setVisibility(View.VISIBLE);
                    bat20.setVisibility(View.GONE);
                } else {
                    bat100.setVisibility(View.GONE);
                    bat80.setVisibility(View.GONE);
                    bat60.setVisibility(View.GONE);
                    bat40.setVisibility(View.GONE);
                    bat20.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGattClient.onDestroy();
    }

}
