package com.example.bikemap;

import com.google.android.gms.maps.model.LatLng;

public class Position implements Comparable<Position> {
    LatLng latlng;
    private double ele;

    Position (double lat, double lon) {
        latlng = new LatLng(lat, lon);
        this.ele = 0;
    }

    Position (String latStr, String lonStr) {
        latlng = new LatLng(Double.parseDouble(latStr), Double.parseDouble(lonStr));
        this.ele = 0;
    }

    Position (Object latObj, Object lonObj) {
        latlng = new LatLng(Double.parseDouble(latObj.toString()), Double.parseDouble(lonObj.toString()));
        this.ele = 0;
    }

    public LatLng getLatLng(){
        return latlng;
    }

    double getEle() {
        return this.ele;
    }

    void setElevation(double ele) {
        this.ele = ele;
    }

    @Override
    public int compareTo(Position o) {
        // TODO Auto-generated method stub
        if(this.latlng.latitude > o.latlng.latitude && this.latlng.longitude > o.latlng.longitude) {
            return 1;
        } else if (this.latlng.latitude == o.latlng.latitude && this.latlng.longitude == o.latlng.longitude){
            return 0;
        } else if (this.latlng.latitude < o.latlng.latitude && this.latlng.longitude < o.latlng.longitude) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        Position pos = (Position)o;
        if(this.latlng.latitude == pos.latlng.latitude && this.latlng.longitude == pos.latlng.longitude) {
            return true;
        } else return false;
    }

    public double getDistance(LatLng latlng) { // 두 Position의 Lat, Lon을 사용하여 두 점 사이의 거리를 계싼

        if ((this.latlng.latitude == latlng.latitude) && (this.latlng.longitude == latlng.longitude)) {
            return 0;
        }
        else {
            double theta = this.latlng.longitude - latlng.longitude;
            double dist = Math.sin(Math.toRadians(this.latlng.latitude)) * Math.sin(Math.toRadians(latlng.latitude)) + Math.cos(Math.toRadians(this.latlng.latitude)) * Math.cos(Math.toRadians(latlng.latitude)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1609.344;

            return dist;
        }
    }
}
