package com.example.orientation_app;

public class Interest {

    private final String name, type;
    private final float latitude;
    private final float longitude;
    private double distanceFromUser = 0;
    private double bearingFromUser = 0;


    Interest(String name, float latitude, float longitude, String type) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }


    // *** part of code from : https://www.geeksforgeeks.org/program-distance-two-points-earth/
    public void findDistanceFromUser(double latitudeUser, double longitudeUser) {

        double x = Math.pow(Math.sin((Math.toRadians(latitudeUser) - Math.toRadians(this.latitude)) / 2), 2)
                + Math.cos(Math.toRadians(latitudeUser)) * Math.cos(Math.toRadians(this.longitude))
                * Math.pow(Math.sin((Math.toRadians(longitudeUser) - Math.toRadians(this.longitude)) / 2), 2);
        this.distanceFromUser = 2 * Tracker.EARTH_RADIUS * Math.asin(Math.sqrt(x));
    }

    // Mixel answer : https://stackoverflow.com/questions/9457988/bearing-from-one-coordinate-to-another
    public void findBearingFromUser(double latitudeUser, double longitudeUser) {
        double dLong = Math.toRadians(this.longitude) - Math.toRadians(longitudeUser);

        double x = Math.atan2(Math.sin(dLong) * Math.cos(Math.toRadians(this.latitude)),
                Math.cos(Math.toRadians(latitudeUser)) * Math.sin(Math.toRadians(this.latitude)) - Math.sin(Math.toRadians(latitudeUser)) * Math.cos(Math.toRadians(this.latitude)) * Math.cos(dLong));
        this.bearingFromUser = (x * 180 / Math.PI + 360) % 360;

    }

    public double getBearingFromUser() {
        return bearingFromUser;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

}
