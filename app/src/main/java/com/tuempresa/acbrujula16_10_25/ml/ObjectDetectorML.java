package com.tuempresa.acbrujula16_10_25.ml;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

public class ObjectDetectorML {
    
    private final ObjectDetector detector;
    
    public interface DetectionCallback {
        void onDetected(List<DetectedObject> objects);
    }
    
    public ObjectDetectorML() {
        // Configurar el detector de objetos con configuración básica
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();
        
        detector = ObjectDetection.getClient(options);
    }
    
    public void processImage(@NonNull InputImage image, @NonNull DetectionCallback callback) {
        detector.process(image)
            .addOnSuccessListener(callback::onDetected)
            .addOnFailureListener(e -> {
                // En caso de error, devolver lista vacía
                callback.onDetected(List.of());
            });
    }
    
    public void close() {
        detector.close();
    }
}