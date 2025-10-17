package com.tuempresa.acbrujula16_10_25.ml;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.tuempresa.acbrujula16_10_25.R;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView txtDetected;
    private ObjectDetectorML detector;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        txtDetected = findViewById(R.id.txtDetectedObject);

        detector = new ObjectDetectorML();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private final ActivityResultLauncher<String> requestPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startCamera();
            });

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analysis.setAnalyzer(cameraExecutor, image -> analyzeImage(image));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

            detector.processImage(image, objects -> runOnUiThread(() -> showObjects(objects)));
        }
        imageProxy.close();
    }

    private void showObjects(List<DetectedObject> objects) {
        if (objects.isEmpty()) {
            txtDetected.setText("Ningún objeto detectado");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (DetectedObject obj : objects) {
            String label = "Objeto sin clasificar";
            if (!obj.getLabels().isEmpty()) {
                label = obj.getLabels().get(0).getText();
            }

            Rect box = obj.getBoundingBox();
            String position;
            if (box.centerX() < previewView.getWidth() / 3f) position = "Izquierda";
            else if (box.centerX() > previewView.getWidth() * 2 / 3f) position = "Derecha";
            else position = "Centro";

            result.append(label).append(" → ").append(position).append("\n");
        }

        txtDetected.setText(result.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
