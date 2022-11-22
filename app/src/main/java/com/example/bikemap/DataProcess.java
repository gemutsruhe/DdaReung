package com.example.bikemap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DataProcess {
    static ArrayList<Station> getStationState() {
        ArrayList<Station> stationState = new ArrayList<>();

        int start = 1;
        while(true) {
            StringBuilder urlBuilder = new StringBuilder("http://openapi.seoul.go.kr:8088/494971696c67656d313030444f546178/json/bikeList/" + start + "/" + (start + 999) + "/");

            int dataNum = 0;
            try {
                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                BufferedReader rd = null;
                if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject)jsonParser.parse(rd);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject rentBikeStatus = (JSONObject)jsonObject.get("rentBikeStatus");
                JSONArray rows = (JSONArray)rentBikeStatus.get("row");
                dataNum = rows.size();
                String data;
                for(int i = 0; i < rows.size(); i++) {
                    JSONObject object = (JSONObject)rows.get(i);
                    Station station = new Station(object.get("stationName"), new Position(object.get("stationLatitude"), object.get("stationLongitude")), object.get("rackTotCnt"), object.get("parkingBikeTotCnt"));
                    stationState.add(station);
                }
                rd.close();
                conn.disconnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(dataNum != 1000) break;
            start += 1000;
        }
        return stationState;
    }
}
