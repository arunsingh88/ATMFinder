package com.thinktanki.atmfinder.atm;

/**
 * Created by Arun Singh on 12/12/2016.
 */
public class ATM {
    private String atmName;
    private Float distance;
    private String atmAddress;
    private double latitude;
    private double longitude;
    private String icon;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getAtmName() {
        return atmName;
    }

    public void setAtmName(String atmName) {
        this.atmName = atmName;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public String getAtmAddress() {
        return atmAddress;
    }

    public void setAtmAddress(String atmAddress) {
        this.atmAddress = atmAddress;
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

}
