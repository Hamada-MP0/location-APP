package com.example.locationapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private TextView tvAccelX, tvAccelY, tvAccelZ, tvAccelMag;
    private TextView tvGyroPitch, tvGyroRoll, tvGyroYaw, tvGyroStatus;
    private TextView tvUseCaseTitle, tvUseCaseDesc, tvUseCaseTip;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvAccelX    = findViewById(R.id.tvAccelX);
        tvAccelY    = findViewById(R.id.tvAccelY);
        tvAccelZ    = findViewById(R.id.tvAccelZ);
        tvAccelMag  = findViewById(R.id.tvAccelMag);

        tvGyroPitch  = findViewById(R.id.tvGyroPitch);
        tvGyroRoll   = findViewById(R.id.tvGyroRoll);
        tvGyroYaw    = findViewById(R.id.tvGyroYaw);
        tvGyroStatus = findViewById(R.id.tvGyroStatus);

        tvUseCaseTitle = findViewById(R.id.tvUseCaseTitle);
        tvUseCaseDesc  = findViewById(R.id.tvUseCaseDesc);
        tvUseCaseTip   = findViewById(R.id.tvUseCaseTip);

        tvInfo = findViewById(R.id.tvInfo);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        loadSensorInfo();
    }

    private void loadSensorInfo() {
        if (accelerometer != null) {
            String name   = accelerometer.getName();
            String vendor = accelerometer.getVendor();
            float  range  = accelerometer.getMaximumRange();
            float  res    = accelerometer.getResolution();

            String detectedType = detectAccelerometerType(vendor, name);

            String info = "Sensor name: " + name + "\n"
                    + "Vendor: " + vendor + "\n"
                    + String.format(Locale.US, "Resolution: %.10f m/s²\n", res)
                    + String.format(Locale.US, "Max range: %.4f m/s²\n", range)
                    + "Detected type: " + detectedType;

            tvInfo.setText(info);
        } else {
            tvInfo.setText("Accelerometer not available!");
        }
    }

    private String detectAccelerometerType(String vendor, String name) {
        String v = vendor.toLowerCase(Locale.US);
        String n = name.toLowerCase(Locale.US);

        if (v.contains("st") || v.contains("stmicro") || n.contains("lsm") || n.contains("lis")) {
            return "MEMS (STMicro LSM series)";
        } else if (v.contains("bosch") || n.contains("bma") || n.contains("bmi")) {
            return "MEMS (Bosch BMA/BMI series)";
        } else if (v.contains("kionix") || n.contains("kxcj") || n.contains("kxtj")) {
            return "Capacitive MEMS (Kionix)";
        } else if (v.contains("invensense") || n.contains("mpu") || n.contains("icm")) {
            return "MEMS (InvenSense MPU series)";
        } else if (v.contains("analog") || n.contains("adxl")) {
            return "Capacitive MEMS (Analog Devices)";
        } else {
            return "Capacitive MEMS (Unknown vendor)";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (gyroscope != null)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double mag = Math.sqrt(x * x + y * y + z * z);

            tvAccelX.setText(String.format(Locale.US, "%.4f m/s²", x));
            tvAccelY.setText(String.format(Locale.US, "%.4f m/s²", y));
            tvAccelZ.setText(String.format(Locale.US, "%.4f m/s²", z));
            tvAccelMag.setText(String.format(Locale.US, "%.4f m/s²", mag));

            updateMotionStatus(mag);
            updateUseCaseDetector(mag);
        }

        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double gyroMag = Math.sqrt(x * x + y * y + z * z);

            tvGyroPitch.setText(String.format(Locale.US, "%.4f rad/s", x));
            tvGyroRoll.setText(String.format(Locale.US,  "%.4f rad/s", y));
            tvGyroYaw.setText(String.format(Locale.US,   "%.4f rad/s", z));

            if (gyroMag < 0.1) {
                tvGyroStatus.setText("Status: Stable");
            } else if (gyroMag < 1.0) {
                tvGyroStatus.setText("Status: Slight Rotation");
            } else {
                tvGyroStatus.setText("Status: Rotating");
            }
        }
    }

    private void updateMotionStatus(double mag) {
        TextView tvMotion = findViewById(R.id.tvMotionStatus);
        if (mag < 1.5) {
            tvMotion.setText("Motion: STILL");
        } else if (mag < 5) {
            tvMotion.setText("Motion: WALKING");
        } else if (mag < 12) {
            tvMotion.setText("Motion: RUNNING");
        } else {
            tvMotion.setText("Motion: FAST MOVEMENT");
        }
    }

    private void updateUseCaseDetector(double mag) {
        if (mag < 1.5) {
            tvUseCaseTitle.setText("PHONE IS STILL");
            tvUseCaseDesc.setText("No significant movement detected");
            tvUseCaseTip.setText("Try: walk, run, shake, or drop slightly");
        } else if (mag < 5) {
            tvUseCaseTitle.setText("WALKING DETECTED");
            tvUseCaseDesc.setText("Rhythmic step motion recognized");
            tvUseCaseTip.setText("Best sensor: Capacitive MEMS");
        } else if (mag < 12) {
            tvUseCaseTitle.setText("RUNNING DETECTED");
            tvUseCaseDesc.setText("High-frequency motion detected");
            tvUseCaseTip.setText("Best sensor: Capacitive MEMS");
        } else {
            tvUseCaseTitle.setText("IMPACT / FAST MOTION");
            tvUseCaseDesc.setText("Extreme G-force or drop detected");
            tvUseCaseTip.setText("Best sensor: Piezoelectric / Piezoresistive");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}