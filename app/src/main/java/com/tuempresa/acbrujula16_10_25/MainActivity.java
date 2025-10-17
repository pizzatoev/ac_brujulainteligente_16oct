package com.tuempresa.acbrujula16_10_25;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private ImageView imgBrujula;
    private TextView tvHeading, tvMode, tvDetection, tvHint;
    private FloatingActionButton btnOpenCamera;
    private TextView tvNorthMsg;
    
    private float currentDegree = 0f;
    private boolean isNorthDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupSensors();
        setupCameraButton();
    }

    private void initViews() {
        imgBrujula = findViewById(R.id.imgBrujula);
        tvHeading = findViewById(R.id.tvHeading);
        tvMode = findViewById(R.id.tvMode);
        tvDetection = findViewById(R.id.tvDetection);
        tvHint = findViewById(R.id.tvHint);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        tvNorthMsg = findViewById(R.id.tvNorthMsg);
    }

    private void setupSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void setupCameraButton() {
        btnOpenCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermission.launch(Manifest.permission.CAMERA);
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
            });

    private void openCamera() {
        Intent intent = new Intent(this, com.tuempresa.acbrujula16_10_25.ml.CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float degree = Math.round(event.values[0]);
            currentDegree = degree;
            
            // Rotar la imagen de la brújula
            imgBrujula.setRotation(-degree);
            
            // Actualizar el texto del heading
            String direction = getDirection(degree);
            tvHeading.setText(degree + "° (" + direction + ")");
            
            // Verificar si apunta al norte
            boolean pointingNorth = Math.abs(degree) < 10 || Math.abs(degree - 360) < 10;
            if (pointingNorth && !isNorthDetected) {
                isNorthDetected = true;
                tvNorthMsg.setVisibility(android.view.View.VISIBLE);
                tvMode.setText("Modo actual: Norte detectado - Cámara disponible");
            } else if (!pointingNorth && isNorthDetected) {
                isNorthDetected = false;
                tvNorthMsg.setVisibility(android.view.View.GONE);
                tvMode.setText("Modo actual: Solo brújula");
            }
        }
    }

    private String getDirection(float degree) {
        if (degree >= 337.5 || degree < 22.5) return "N";
        if (degree >= 22.5 && degree < 67.5) return "NE";
        if (degree >= 67.5 && degree < 112.5) return "E";
        if (degree >= 112.5 && degree < 157.5) return "SE";
        if (degree >= 157.5 && degree < 202.5) return "S";
        if (degree >= 202.5 && degree < 247.5) return "SO";
        if (degree >= 247.5 && degree < 292.5) return "O";
        if (degree >= 292.5 && degree < 337.5) return "NO";
        return "N";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No necesario para este caso
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}