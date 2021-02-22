package com.example.orientation_app;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

//Inspiré de : https://howtodoandroid.medium.com/how-to-get-current-latitude-and-longitude-in-android-example-35437a51052a
public class Tracker extends Service implements LocationListener {

    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double latitude;
    double longitude;

    private static final long DISTANCE_MINIMUM_UPDATE = 5, TEMPS_MINI_UPDATE = 1;

    protected LocationManager locationManager;

    Tracker(Context context) {
        this.context = context;
        getLocation();
    }

    private Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                }
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            TEMPS_MINI_UPDATE,
                            DISTANCE_MINIMUM_UPDATE, this);

                    Log.d("GPS Enabled", "GPS Enabled");
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if(location!=null){
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
