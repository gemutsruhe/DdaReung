package com.example.bikemap;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    static void getPOI(String query){
        StringBuilder urlBuilder = new StringBuilder("https://apis.openapi.sk.com/tmap/pois?version=1&areaLLCode=11&appKey=l7xx3637c689feac4e4ea50f3f9cd11a09ef");

        try {
            urlBuilder.append("&searchKeyword=" + URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
            JSONObject searchPoiInfo = (JSONObject)jsonObject.get("searchPoiInfo");
            JSONObject pois = (JSONObject)searchPoiInfo.get("pois");
            JSONArray poi = (JSONArray)pois.get("poi");
            System.out.println("TEST : " + poi.size());
            String data;
            for(int i = 0; i < poi.size(); i++) {
                JSONObject object = (JSONObject)poi.get(i);
                System.out.println("TEST : " + object.get("name") + " " + object.get("frontLat") + " " + object.get("frontLon"));
            }
            rd.close();
            conn.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
