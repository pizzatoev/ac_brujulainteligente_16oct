package com.tuempresa.acbrujula16_10_25.ml;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.List;

public class ObjectDetectorML {
    
    private final ObjectDetector detector;
    private final Context context;
    
    public interface DetectionCallback {
        void onDetected(List<DetectedObject> objects);
    }
    
    public ObjectDetectorML(Context context) {
        this.context = context;
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
            .addOnSuccessListener(detectedObjects -> {
                // Procesar objetos detectados
                if (!detectedObjects.isEmpty()) {
                    DetectedObject obj = detectedObjects.get(0);
                    String label = "Objeto desconocido";

                    if (!obj.getLabels().isEmpty()) {
                        label = obj.getLabels().get(0).getText();
                    }

                    Log.d("ObjectDetectorML", "Objeto detectado: " + label);

                    // 🔹 Enviar broadcast con el objeto detectado
                    Intent intent = new Intent("com.app.BROADCAST_DETECCION");
                    intent.putExtra("objeto", label);
                    context.sendBroadcast(intent);
                }
                
                // Llamar al callback original
                callback.onDetected(detectedObjects);
            })
            .addOnFailureListener(e -> {
                // En caso de error, devolver lista vacía
                callback.onDetected(List.of());
            });
    }
    
    public void close() {
        detector.close();
    }
}