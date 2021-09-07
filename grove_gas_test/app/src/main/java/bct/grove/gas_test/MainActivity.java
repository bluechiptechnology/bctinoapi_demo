package bct.grove.gas_test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import bct.inoapi.InoCurator;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InoCurator.runSketch(new GasInoSketch());
    }

    protected void onStop()
    {
        InoCurator.stop();
        super.onStop();
        System.exit(0);
    }
}