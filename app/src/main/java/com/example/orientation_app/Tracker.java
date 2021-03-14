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
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.orientation_app.Interest;

//Inspiré de : https://howtodoandroid.medium.com/how-to-get-current-latitude-and-longitude-in-android-example-35437a51052a
public class Tracker extends Service implements LocationListener {

    private final Context context;

    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;

    volatile boolean exit = false, updateInProgress = false;

    TextView latitudeHere, longitudeHere, distance, bearing, nomInterest;
    Boussole boussole;
    Location location;

    public double angleOfResearch = 5.;
    List<Interest> interestPoints;
    String[][] arrayOfInf;

    public static int EARTH_RADIUS = 6371;
    public int arrayInfSize = 0;

    public boolean stopTheChecking = false, checkThreadExists = false;
    double latitude;
    double longitude;

    private static final long DISTANCE_MINIMUM_UPDATE = 5, TEMPS_MINI_UPDATE = 10;
    private final int REFRESH_MS_CHECK_THREAD = 50;
    protected LocationManager locationManager;

    Tracker(Context context, TextView lat, TextView longi, TextView distance, List<Interest> interestList, TextView bearing, TextView nomInterest, Boussole boussole) {
        this.context = context;
        this.latitudeHere = lat;
        this.longitudeHere = longi;
        this.distance = distance;
        this.interestPoints = interestList;
        this.bearing = bearing;
        this.location = getLocation();
        this.nomInterest = nomInterest;
        this.boussole = boussole;
        //getLocation();
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
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            TEMPS_MINI_UPDATE,
                            DISTANCE_MINIMUM_UPDATE, this);

                    //Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
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
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    // WIP : Retourne un tab de String en deux dimensions avec la valeur inférieur
    // étant dans la 3ème colonne ( 0 : name, 1 : distance, 2 : Moyenne du bearing d'avant et présent )
    public String[][] createOrientationTable(List<Interest> listOfInterests) {

        Collections.sort(listOfInterests, new InterestBearingComparator());
        for (Interest interest : listOfInterests) {
            Log.d("Bearing of", "" + interest.getName() + " bearing : " + interest.getBearingFromUser());
        }
        arrangeTheArray(listOfInterests);
        String[][] arrayInfBearings = new String[listOfInterests.size()][3];
        int i = 0;
        double medium;
        while (i < listOfInterests.size()) {
            if (i == 0)
                medium = (listOfInterests.get(listOfInterests.size() - 1).getBearingFromUser()
                        + listOfInterests.get(i).getBearingFromUser());
            else medium = (listOfInterests.get(i - 1).getBearingFromUser()
                    + listOfInterests.get(i).getBearingFromUser());

            if (i == 0 && medium > 360) {
                medium /= 2 - 180;
                arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                arrayInfBearings[i][2] = "" + medium;
                Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);
            } else if (i == 0) {
                medium = 13;
                arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                arrayInfBearings[i][2] = "" + medium;
                Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);

            } else {
                medium /= 2;
                arrayInfBearings[i][0] = listOfInterests.get(i).getName();
                arrayInfBearings[i][1] = "" + listOfInterests.get(i).getDistanceFromUser();
                arrayInfBearings[i][2] = "" + medium;
                Log.d("Medium[" + i + "] " + arrayInfBearings[i][0], "" + arrayInfBearings[i][2]);
            }
            i++;
        }
        this.arrayInfSize = listOfInterests.size();
        return arrayInfBearings;
    }

    public void arrangeTheArray(List<Interest> listOfInterests) {
        List<Integer> interestToRemove = new ArrayList<>();

        for (int i = 0; i < listOfInterests.size(); i++) {
            if (i == 0) {
                //Remove if there is less than the angle of Research available between the bearings of two interests encapsulating the first interest
                if (((360 - listOfInterests.get(listOfInterests.size() - 1).getBearingFromUser() + listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch) &&
                        ((listOfInterests.get(i + 1).getBearingFromUser() - listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch)) {
                    Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                    listOfInterests.remove(i);
                }
            } else {
                if (i == listOfInterests.size() - 1) {
                    if (((listOfInterests.get(i).getBearingFromUser() - listOfInterests.get(i - 1).getBearingFromUser()) <= angleOfResearch) &&
                            ((360 - listOfInterests.get(i).getBearingFromUser() + listOfInterests.get(0).getBearingFromUser()) <= angleOfResearch)) {

                        Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                        listOfInterests.remove(i);
                    }
                } else {
                    if (((listOfInterests.get(i).getBearingFromUser() - listOfInterests.get(i - 1).getBearingFromUser()) <= angleOfResearch) &&
                            ((listOfInterests.get(i + 1).getBearingFromUser() - listOfInterests.get(i).getBearingFromUser()) <= angleOfResearch)) {
                        Log.d("Remove[" + i + "] " + listOfInterests.get(i).getName(), "Bearing value" + listOfInterests.get(i).getBearingFromUser());
                        listOfInterests.remove(i);
                    }
                }
            }
        }


    }

    public void checkInArray(String[][] arrayInfBearings, double angle) {
        for (int i = 0; i < arrayInfSize; i++) {

            if (i == arrayInfSize - 1) {
                int finalI = i;
                distance.post(new Runnable() {
                    @Override
                    public void run() {
                        distance.setText("" + (float) Math.round(Double.parseDouble(arrayInfBearings[finalI][1]) * 1000) / 1000 + " kilomètres ");
                    }
                });
                nomInterest.post(new Runnable() {
                    @Override
                    public void run() {
                        nomInterest.setText("" + arrayInfBearings[finalI][0]);
                    }
                });
                break;
            } else if (Double.parseDouble(arrayInfBearings[i][2]) <= angle && Double.parseDouble(arrayInfBearings[i + 1][2]) > angle) {
                int finalI = i;
                distance.post(new Runnable() {
                    @Override
                    public void run() {
                        distance.setText("" + (float) Math.round(Double.parseDouble(arrayInfBearings[finalI][1]) * 1000) / 1000 + " kilomètres ");
                    }
                });
                nomInterest.post(new Runnable() {
                    @Override
                    public void run() {
                        nomInterest.setText("" + arrayInfBearings[finalI][0]);
                    }
                });
                break;
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean canGetLocation() {
        return canGetLocation;
    }

    public void updateHere(TextView latitudeHere, TextView longitudeHere, TextView distanceToPoint) {

        if (!updateInProgress) {
            updateInProgress = true;
            DoCalculation calculate = new DoCalculation();
            new Thread(calculate).start();
            if (!checkThreadExists) {
                checkThreadExists = true;
                Runnable checkArrayRun = new Tracker.StartCheckTheArray();
                new Thread(checkArrayRun).start();
            }
        } else {
            Toast.makeText(context, "Doucement sur le bouton !", Toast.LENGTH_LONG);
        }
    }

    public class StartCheckTheArray implements Runnable {

        @Override
        public void run() {
            while (!stopTheChecking) {
                checkInArray(arrayOfInf, boussole.getAzimuth());
                try {
                    Thread.sleep(REFRESH_MS_CHECK_THREAD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Work in progress
    public class DoCalculation implements Runnable {
        @Override
        public void run() {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            //Updates distances and bearings values
            for (Interest interest : interestPoints) {
                interest.findBearingFromUser(latitude, longitude);
                interest.findDistanceFromUser(latitude, longitude);
            }
            arrayOfInf = createOrientationTable(interestPoints);

            NumberFormat formatter2 = new DecimalFormat("#0.00000");
            latitudeHere.post(new Runnable() {
                @Override
                public void run() {
                    latitudeHere.setText("" + formatter2.format(latitude));
                }
            });
            longitudeHere.post(new Runnable() {
                @Override
                public void run() {
                    longitudeHere.setText("" + formatter2.format(longitude));
                }
            });

            Log.d("Thread findind distance", "Thread has finished to update");
            updateInProgress = false;
        }
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
}
