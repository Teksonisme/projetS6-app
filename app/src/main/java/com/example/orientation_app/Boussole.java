package com.example.orientation_app;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

// Inspired of : https://www.youtube.com/watch?v=RcqXFxqIAW4
public class Boussole implements SensorEventListener {

    private ImageView boussoleView;
    private TextView angleToNorth;

    private float[] mGravity = new float[3];
    private float[] mGeomagnetic = new float[3];
    private float[] mRotationMatrix = new float[9];
    private float[] mOrientation = new float[9];

    public float getAzimuth() {
        return azimuth;
    }

    private float azimuth = 2f;
    private float correctAzimuth = 0f;
    private SensorManager mSensorManager;

    //Used to reduce noise of motion sensors ( filter )
    private final float alpha = 0.99f;

    Boussole(SensorManager sensorM, ImageView boussoleView, TextView angleToNorth){
        this.mSensorManager = sensorM;
        this.boussoleView = boussoleView;
        this.angleToNorth = angleToNorth;

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this){
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpha*mGravity[0]+(1-alpha)*event.values[0];
                mGravity[1] = alpha*mGravity[1]+(1-alpha)*event.values[1];
                mGravity[2] = alpha*mGravity[2]+(1-alpha)*event.values[2];
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic[0] = alpha*mGeomagnetic[0]+(1-alpha)*event.values[0];
                mGeomagnetic[1] = alpha*mGeomagnetic[1]+(1-alpha)*event.values[1];
                mGeomagnetic[2] = alpha*mGeomagnetic[2]+(1-alpha)*event.values[2];
            }
            boolean success = SensorManager.getRotationMatrix(mRotationMatrix,mOrientation,mGravity,mGeomagnetic);
            if(success){
                float[] orientation = new float[3];
                SensorManager.getOrientation(mRotationMatrix,orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360)%360;

                Animation anim = new RotateAnimation(-correctAzimuth, -azimuth,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                correctAzimuth = azimuth;

                anim.setDuration(100);
                anim.setRepeatCount(0);
                anim.setFillAfter(true);

                boussoleView.startAnimation(anim);
                angleToNorth.setText(""+azimuth);
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
