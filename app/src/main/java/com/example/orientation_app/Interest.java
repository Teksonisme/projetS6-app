package com.example.orientation_app;

import android.util.Log;

public class Interest {

    private final String name, type;
    private double latitude;
    private double longitude;
    private double distanceFromUser = 0;
    private double bearingFromUser = 0;


    Interest(String name, double latitude, double longitude, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }


    // *** https://www.movable-type.co.uk/scripts/latlong.html
    public void findDistanceFromUser(double latitudeUser, double longitudeUser) {

        double dLat = this.latitude - latitudeUser,dLong = this.longitude - longitudeUser;


        double x = (dLat*Math.PI/180./2.)*(dLat*Math.PI/180./2.)
                + Math.cos(latitudeUser*Math.PI/180.) * Math.cos(this.latitude*Math.PI/180.)
                * (dLong*Math.PI/180./2.)*(dLong*Math.PI/180./2.);
        if(x < 0){
            this.distanceFromUser = 2 * Tracker.EARTH_RADIUS * Math.atan2(Math.sqrt(-x),Math.sqrt(1+x));
        }
        else{
            this.distanceFromUser = 2 * Tracker.EARTH_RADIUS * Math.atan2(Math.sqrt(x),Math.sqrt(1-(x)));
        }

        Log.d("Interest distance",""+name+" : "+getDistanceFromUser());
    }

    // Mixel answer : https://stackoverflow.com/questions/9457988/bearing-from-one-coordinate-to-another
    public void findBearingFromUser(double latitudeUser, double longitudeUser) {
        double dLong = Math.toRadians(this.longitude) - Math.toRadians(longitudeUser);

        double x = Math.atan2(Math.sin(dLong) * Math.cos(Math.toRadians(this.latitude)),
                Math.cos(Math.toRadians(latitudeUser)) * Math.sin(Math.toRadians(this.latitude)) - Math.sin(Math.toRadians(latitudeUser)) * Math.cos(Math.toRadians(this.latitude)) * Math.cos(dLong));
        this.bearingFromUser = (x * 180 / Math.PI + 360) % 360;
        //Log.d("Interest bearing",""+name+" : "+getBearingFromUser());

    }

    public double getBearingFromUser() {
        return bearingFromUser;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

}
