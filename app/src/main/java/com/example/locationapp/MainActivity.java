package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnGPS, btnSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGPS = findViewById(R.id.btnGPS);
        btnSensor = findViewById(R.id.btnSensor);

        btnGPS.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GPSActivity.class));
        });

        btnSensor.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SensorActivity.class));
        });
    }
}