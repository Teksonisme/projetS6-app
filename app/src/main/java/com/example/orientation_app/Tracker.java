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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

//Inspiré de : https://howtodoandroid.medium.com/how-to-get-current-latitude-and-longitude-in-android-example-35437a51052a
public class Tracker extends Service implements LocationListener {

    private final Context context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    TextView latitudeHere, longitudeHere, distance,bearing;
    Location location;
    List<Interest> interestPoints;

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    double latitude;
    double longitude;

    private static final long DISTANCE_MINIMUM_UPDATE = 5, TEMPS_MINI_UPDATE = 10;

    protected LocationManager locationManager;

    Tracker(Context context, TextView lat, TextView longi, TextView distance, List<Interest> interestList,TextView bearing) {
        this.context = context;
        this.latitudeHere = lat;
        this.longitudeHere = longi;
        this.distance = distance;
        this.interestPoints = interestList;
        this.bearing = bearing;
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
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            TEMPS_MINI_UPDATE,
                            DISTANCE_MINIMUM_UPDATE, this);

                    //Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            TEMPS_MINI_UPDATE,
                            DISTANCE_MINIMUM_UPDATE, this);

                    //Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        NumberFormat formatter = new DecimalFormat("#0.00000");
        if (this.canGetLocation()) {
            latitude = Double.parseDouble(formatter.format(this.getLatitude()));
            longitude = Double.parseDouble(formatter.format(this.getLongitude()));
            latitudeHere.setText(String.valueOf(latitude));
            longitudeHere.setText(String.valueOf(longitude));
        } else {
            Toast.makeText(context, "Permissions de géolocalisation refusées", Toast.LENGTH_SHORT).show();
        }
        return location;
    }

    // *** part of code from : https://www.geeksforgeeks.org/program-distance-two-points-earth/
    public void findDistance() {
        double earthRadius = 6371;
        double latitudeRad = Math.toRadians(latitude), longitudeRad = Math.toRadians(longitude);
        double latitudeInterestRad = Math.toRadians(interestPoints.get(0).getLatitude()), longitudeInterestRad = Math.toRadians(interestPoints.get(0).getLongitude());
        double dLat = latitudeRad - latitudeInterestRad;
        double dLong = longitudeRad - longitudeInterestRad;

        //distance
        double x = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(latitudeRad) * Math.cos(latitudeInterestRad)
                * Math.pow(Math.sin(dLong / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(x));
        NumberFormat formatter = new DecimalFormat("#0.00");
        String dist = formatter.format(earthRadius * c);

        this.distance.setText("" + dist + " kilomètres");

    }
    // Mixel answer : https://stackoverflow.com/questions/9457988/bearing-from-one-coordinate-to-another
    public void findBearing(){
        double latitudeRad = Math.toRadians(latitude), longitudeRad = Math.toRadians(longitude);
        double latitudeInterestRad = Math.toRadians(interestPoints.get(0).getLatitude()), longitudeInterestRad = Math.toRadians(interestPoints.get(0).getLongitude());

        double dLong = longitudeInterestRad - longitudeRad;

        double x = Math.atan2(Math.sin(dLong) * Math.cos(latitudeInterestRad),
                Math.cos(latitudeRad)*Math.sin(latitudeInterestRad) - Math.sin(latitudeRad) * Math.cos(latitudeInterestRad) * Math.cos(dLong));

        this.bearing.setText(""+(Math.toDegrees(x) + 360)%360);

    }
    public String[][] findAllBearing(List<Interest> interestPoints){
        double latitudeRad, latitudeInterestRad,longitudeRad, longitudeInterestRad, dLong;
        int i=0,j=0;
        String[][] interestsWithBearing = null;

        for(Interest interest : interestPoints){
            latitudeRad = Math.toRadians(latitude);
            longitudeRad = Math.toRadians(longitude);
            latitudeInterestRad = Math.toRadians(interest.getLatitude());
            longitudeInterestRad = Math.toRadians(interest.getLongitude());

            dLong = longitudeInterestRad - longitudeRad;

            double x = Math.atan2(Math.sin(dLong) * Math.cos(latitudeInterestRad),
                    Math.cos(latitudeRad)*Math.sin(latitudeInterestRad) - Math.sin(latitudeRad) * Math.cos(latitudeInterestRad) * Math.cos(dLong));

            interestsWithBearing[i][j] = interest.getName();
            interestsWithBearing[i][j+1] = ""+x;
            i++;
        }
        return interestsWithBearing;
    }
    public String searchInterestInFront(String[][] interests, double angle){
        String name = "None";
        int i = 0;
        for(String[] angleCompare : interests){
            if(angle>180){

            }
            else{

            }


        }

        return name;
    }
    @Override
    public void onLocationChanged(Location location) {
        getLocation();
        findDistance();
        findBearing();
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

    public boolean canGetLocation() {
        return canGetLocation;
    }
}
