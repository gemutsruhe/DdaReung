package com.example.bikemap;

import com.google.android.gms.maps.model.LatLng;

public class Place {
    private String name;
    private LatLng latlng;

    Place(String name, LatLng latlng){
        this.name = name;
        this.latlng = latlng;
    }

    public String getName(){
        return name;
    }

    public LatLng getLatLng(){
        return latlng;
    }
}
