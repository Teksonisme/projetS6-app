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

    private final ImageView boussoleView;
    private final TextView angleToNorth;

    private final float[] mGravity = new float[3];
    private final float[] mGeomagnetic = new float[3];
    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientation = new float[9];

    public float getAzimuth() {
        return azimuth;
    }

    private float azimuth = 2f;
    private float correctAzimuth = 0f;
    private final SensorManager mSensorManager;

    Boussole(SensorManager sensorM, ImageView boussoleView, TextView angleToNorth){
        this.mSensorManager = sensorM;
        this.boussoleView = boussoleView;
        this.angleToNorth = angleToNorth;

        start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized(this){
            //Used to reduce noise of motion sensors ( filter )
            float alpha = 0.965f;
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpha *mGravity[0]+(1- alpha)*event.values[0];
                mGravity[1] = alpha *mGravity[1]+(1- alpha)*event.values[1];
                mGravity[2] = alpha *mGravity[2]+(1- alpha)*event.values[2];
            }
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeomagnetic[0] = alpha *mGeomagnetic[0]+(1- alpha)*event.values[0];
                mGeomagnetic[1] = alpha *mGeomagnetic[1]+(1- alpha)*event.values[1];
                mGeomagnetic[2] = alpha *mGeomagnetic[2]+(1- alpha)*event.values[2];
            }
            boolean success = SensorManager.getRotationMatrix(mRotationMatrix,mOrientation,mGravity,mGeomagnetic);
            if(success){
                float[] orientation = new float[3];
                SensorManager.getOrientation(mRotationMatrix,orientation);
                azimuth = (float)Math.toDegrees(orientation[0]);
                azimuth = (azimuth + 360)%360;

                Animation anim = new RotateAnimation(-correctAzimuth, -azimuth,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
                correctAzimuth = azimuth;

                anim.setDuration(20);
                anim.setRepeatCount(0);
                anim.setFillAfter(false);

                boussoleView.startAnimation(anim);
                angleToNorth.setText(""+Math.round(azimuth)+"°");

            }
        }
    }

    public void pause(){
        mSensorManager.unregisterListener(this);
    }
    public void start(){
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
