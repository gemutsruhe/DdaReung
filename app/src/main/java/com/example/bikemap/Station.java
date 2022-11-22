package com.example.bikemap;

public class Station {
    private String name;
    private Position pos;
    private int rackNum;
    private int parkingNum;

    Station(String name, Position pos, int rackNum, int parkingNum) {
        this.name = name;
        this.pos = pos;
        this.rackNum = rackNum;
        this.parkingNum = parkingNum;
    }

    Station(Object name, Position pos, Object rackNum, Object parkingNum) {
        this.name = name.toString();
        this.pos = pos;
        this.rackNum = Integer.parseInt(rackNum.toString());
        this.parkingNum = Integer.parseInt(parkingNum.toString());
    }

    Position getPosition() {
        return pos;
    }

    int getRackNum() {
        return rackNum;
    }

    int getParkingNum() {
        return parkingNum;
    }

    String getStationName() { return name; }
}
