package com.example.orientation_app;

public class InterestWithDistance{
    Interest interest;
    private double distance,bearing;

    InterestWithDistance(Interest interest,double distance, double bearing){
        this.interest = interest;

    }
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }


}
