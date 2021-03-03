package com.example.orientation_app;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Boussole {

    // ** Programmer world : https://www.youtube.com/watch?v=Dqg1A4hy-jI
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    ImageView boussoleView;
    TextView angleToNorth;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    double angleNorth360,lastValue = -1;

    Context context;

    Boussole(Context context, SensorManager sensorManager, ImageView boussoleView, TextView angleToNorth) {
        this.context = context;
        this.sensorManager = sensorManager;
        this.boussoleView = boussoleView;
        this.angleToNorth = angleToNorth;
    }

    public void boussoleStart() {

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelerometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                angleNorth360 = ((Math.toDegrees(floatOrientation[0]) + 360)) % 360;


                lastValue = angleNorth360;
                boussoleView.setRotation((float) (-floatOrientation[0] * 180 / 3.14159));
                angleToNorth.setText("" + angleNorth360 + "°");

        }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListenerMagneticField = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                angleNorth360 = ((Math.toDegrees(floatOrientation[0]) + 360)) % 360;


                    lastValue = angleNorth360;
                    boussoleView.setRotation((float) (-floatOrientation[0] * 180 / 3.14159));
                    angleToNorth.setText("" + angleNorth360 + "°");

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelerometer, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListenerMagneticField, sensorMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
