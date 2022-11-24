package com.example.bikemap;

import com.google.android.gms.maps.model.LatLng;

public class Position implements Comparable<Position> {
    private final LatLng latlng;
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

    public static double getDistance(LatLng latlng1, LatLng latlng2) { // 두 Position의 Lat, Lon을 사용하여 두 점 사이의 거리를 계싼

        if ((latlng1.latitude == latlng2.latitude) && (latlng1.longitude == latlng2.longitude)) {
            return 0;
        }
        else {
            double theta = latlng1.longitude - latlng2.longitude;
            double dist = Math.sin(Math.toRadians(latlng1.latitude)) * Math.sin(Math.toRadians(latlng2.latitude)) + Math.cos(Math.toRadians(latlng1.latitude)) * Math.cos(Math.toRadians(latlng2.latitude)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1609.344;

            return dist;
        }
    }

    public static double getDistance(Position pos1, Position pos2) {
        LatLng latlng1 = pos1.getLatLng(), latlng2 = pos2.getLatLng();
        if ((latlng1.latitude == latlng2.latitude) && (latlng1.longitude == latlng2.longitude)) {
            return 0;
        }
        else {
            double theta = latlng1.longitude - latlng2.longitude;
            double dist = Math.sin(Math.toRadians(latlng1.latitude)) * Math.sin(Math.toRadians(latlng2.latitude)) + Math.cos(Math.toRadians(latlng1.latitude)) * Math.cos(Math.toRadians(latlng2.latitude)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1609.344;

            return dist;
        }
    }
}
