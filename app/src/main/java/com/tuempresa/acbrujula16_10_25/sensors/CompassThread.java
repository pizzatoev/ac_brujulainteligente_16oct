package com.tuempresa.acbrujula16_10_25.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CompassThread extends Thread {

    private final Context context;
    private final SensorManager sensorManager;
    private boolean running = true;

    public CompassThread(Context context, SensorManager sensorManager) {
        this.context = context;
        this.sensorManager = sensorManager;
    }

    @Override
    public void run() {
        Log.d("CompassThread", "Hilo de brújula iniciado");

        float[] gravity = new float[3];       // Acelerómetro
        float[] geomagnetic = new float[3];   // Campo magnético
        float[] rotationMatrix = new float[9];
        float[] orientation = new float[3];

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        final float[] lastAcc = new float[3];
        final float[] lastMag = new float[3];

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    System.arraycopy(event.values, 0, lastAcc, 0, event.values.length);
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    System.arraycopy(event.values, 0, lastMag, 0, event.values.length);
            }
            @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_UI);

        while (running) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAcc, lastMag);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthRad = orientation[0];
                float azimuthDeg = (float) Math.toDegrees(azimuthRad);
                if (azimuthDeg < 0) azimuthDeg += 360;

                // Enviar broadcast con el valor real
                Intent intent = new Intent("com.app.ORIENTACION_ACTUAL");
                intent.putExtra("azimuth", azimuthDeg);
                context.sendBroadcast(intent);
                Log.d("CompassThread", "Azimut: " + azimuthDeg);
            }

            try { Thread.sleep(500); }
            catch (InterruptedException e) { running = false; }
        }

        sensorManager.unregisterListener(listener);
        Log.d("CompassThread", "Hilo de brújula detenido");
    }

    public void detener() {
        running = false;
    }
}
