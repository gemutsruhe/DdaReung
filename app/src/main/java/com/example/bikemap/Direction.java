package com.example.bikemap;

import java.util.ArrayList;

public class Direction {
    private ArrayList<Position> pointPos;
    private ArrayList<Position> route;
    private double weight;

    Direction(ArrayList<Position> pointPos, ArrayList<Position> route, double weight){
        this.pointPos = pointPos;
        this.route = route;
        this.weight = weight;
    }

    ArrayList<Position> getPointPos(){
        return pointPos;
    }

    ArrayList<Position> getRoute(){
        return route;
    }

    double getWeight() {
        return weight;
    }

}