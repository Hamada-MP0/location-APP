package com.example.locationapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

public class GPSActivity extends AppCompatActivity {
    private Button btnBack;
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private TextView tvOnlineLoc, tvOfflineLoc, tvDiff, tvAccelInfo, tvSatellites;
    private Location lastOnlineLoc = null;
    private Location lastOfflineLoc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        tvOnlineLoc = findViewById(R.id.tvOnlineLoc);
        tvOfflineLoc = findViewById(R.id.tvOfflineLoc);
        tvDiff = findViewById(R.id.tvDiff);
        tvAccelInfo = findViewById(R.id.tvAccelInfo);
        tvSatellites = findViewById(R.id.tvSatellites);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(GPSActivity.this, MainActivity.class));
            finish();
        });
        checkPermissions();
        getSensorSpecifications();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            startTracking();
        }
    }

    private void startTracking() {
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, location -> {
                lastOnlineLoc = location;
                tvOnlineLoc.setText(formatLocation(location));
                calculateDistance();
            });

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, location -> {
                lastOfflineLoc = location;
                tvOfflineLoc.setText(formatLocation(location));
                calculateDistance();
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locationManager.registerGnssStatusCallback(new GnssStatus.Callback() {
                    @Override
                    public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                        StringBuilder sb = new StringBuilder();
                        int count = status.getSatelliteCount();
                        sb.append("Satellites (").append(count).append(" visible)\n\n");

                        for (int i = 0; i < count; i++) {
                            float snr = status.getCn0DbHz(i);
                            boolean used = status.usedInFix(i);
                            sb.append("SNR: ").append(snr).append(" | ")
                                    .append(used ? "Used" : "Visible").append("\n");
                        }

                        tvSatellites.setText(sb.toString());
                    }
                }, new Handler(Looper.getMainLooper()));
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void calculateDistance() {
        if (lastOnlineLoc != null && lastOfflineLoc != null) {
            float distance = lastOnlineLoc.distanceTo(lastOfflineLoc);
            tvDiff.setText(String.format(Locale.US,"Distance: %.2f meters", distance));
        }
    }

    private void getSensorSpecifications() {
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accel != null) {
            String info = "Name: " + accel.getName() +
                    "\nVendor: " + accel.getVendor() +
                    "\nPower: " + accel.getPower() +
                    "\nRange: " + accel.getMaximumRange() +
                    "\nResolution: " + accel.getResolution();
            tvAccelInfo.setText(info);
        }
    }

    private String formatLocation(Location loc) {
        return "Lat: " + loc.getLatitude() +
                "\nLon: " + loc.getLongitude() +
                "\nAccuracy: " + loc.getAccuracy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startTracking();
        }
    }
}