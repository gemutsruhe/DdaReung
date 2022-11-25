package com.example.bikemap;

import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Direction {
    private ArrayList<Position> pointPos;
    private ArrayList<Position> route;
    private double weight;
    public static ArrayList<Position> crossRoadList;

    Direction(ArrayList<Position> pointPos, ArrayList<Position> route, double weight){
        this.pointPos = pointPos;
        this.route = route;
        this.weight = weight;
    }

    ArrayList<Position> getPointPosList(){
        return pointPos;
    }

    ArrayList<Position> getRouteList(){
        return route;
    }

    double getWeight() {
        return weight;
    }

    public static ArrayList<Direction> getRoute(LatLng start, LatLng end) {
        ArrayList<Direction> route = new ArrayList<>();

        for(int i = 0; i < crossRoadList.size(); i++) {
            Position waypointPos = crossRoadList.get(i);
            LatLng waypoint = waypointPos.getLatLng();
            if((Position.getDistance(start, end) * 1.3 >= Position.getDistance(start, waypoint) + Position.getDistance(end, waypoint)) && Math.min(Position.getDistance(start, waypoint), Position.getDistance(end, waypoint)) * 1.5 >= Math.max(Position.getDistance(start, waypoint), Position.getDistance(end, waypoint))) {
                AtomicReference<ArrayList<Position>> atomicPosList = new AtomicReference<>();
                Thread getPosList = new Thread() {
                    public void run(){
                        atomicPosList.set(getDirection(start, waypoint, end));
                    }
                };
                getPosList.start();
                try {
                    getPosList.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ArrayList<Position> posList = atomicPosList.get();
                ArrayList<Position> dividePosList = divideInterval(posList);
                Thread thread = new Thread() {
                    public void run(){
                        setElevation(dividePosList);
                    }
                };
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double weight = calcWeight(dividePosList);

                route.add(new Direction(posList, dividePosList, weight));
            }
        }

        double minWeight = Double.MAX_VALUE;

        for(int i = 0; i < route.size(); i++) {
            minWeight = Math.min(route.get(i).getWeight(), minWeight);
        }

        for(int i = 0; i < route.size(); i++) {
            if(minWeight * 1.3 < route.get(i).getWeight()) {
                route.remove(i);
                i--;
            }
        }

        for(int i = 0; i < route.size() - 1; i++) {
            for(int j = i + 1; j < route.size(); j++) {

                ArrayList<Position> route1 = new ArrayList<>(route.get(i).getPointPosList());
                ArrayList<Position> route2 = new ArrayList<>(route.get(j).getPointPosList());

                int route1Size = route1.size();
                int route2Size = route2.size();

                route1.retainAll(route2);

                if((double)route1.size() / route1Size >= 0.8 || (double)route1.size() / route2Size >= 0.8) {
                    if(route.get(i).getWeight() > route.get(j).getWeight()) {
                        route.remove(i);
                        i--;
                        j--;
                        break;
                    } else {
                        route.remove(j);
                        j--;
                        continue;
                    }
                }
            }
        }

        return route;
    }

    private static ArrayList<Position> getDirection(LatLng start, LatLng waypoint, LatLng end) { // openrouteservice의 api를 통해 경로를 가져온다(임시, 개선 예정)
        ArrayList<Position> posList = new ArrayList<>();

        String coord = "[[" + start.longitude + "," + start.latitude + "],[" + waypoint.longitude + "," + waypoint.latitude + "],[" + end.longitude + "," + end.latitude + "]]";
        StringBuilder urlBuilder = new StringBuilder("https://api.openrouteservice.org/v2/directions/cycling-regular?Authorization=5b3ce3597851110001cf6248588bd0d801724a5f850d4ecb744064e4");
        URL url;

        try {
            url = new URL(urlBuilder.toString());

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "5b3ce3597851110001cf6248588bd0d801724a5f850d4ecb744064e4");

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setDefaultUseCaches(false);

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("{\"coordinates\":" + coord + "}");
            wr.flush();

            BufferedReader rd;
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
            JSONArray routes = (JSONArray) jsonObject.get("routes");
            JSONObject objectTemp = (JSONObject)routes.get(0);
            JSONObject summary = (JSONObject)objectTemp.get("summary");
            String geometry = objectTemp.get("geometry").toString();
            JSONArray decodedGeometry = decodeGeometry(geometry, false);

            for(int i = 0; i < decodedGeometry.size(); i++) {
                JSONArray temp = (JSONArray)decodedGeometry.get(i);
                posList.add(new Position(temp.get(0).toString(), temp.get(1).toString()));
            }

            rd.close();
            conn.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return posList;
    }

    private static ArrayList<Position> divideInterval(ArrayList<Position> posList) { // Position Array를 15m(임시) 단위로 나누어 분할하여 ArrayList<Position>으로 return

        ArrayList<Position> dividePosList = new ArrayList<>();

        dividePosList.add(new Position(posList.get(0).getLatLng().latitude, posList.get(0).getLatLng().longitude));

        for(int i = 0; i < posList.size() - 1; i++) {
            LatLng latlng1 = posList.get(i).getLatLng(), latlng2 = posList.get(i + 1).getLatLng();
            double dist = Position.getDistance(latlng1, latlng2);
            int divideTimes = (int)dist / 15;

            for(int j = 1; j < divideTimes; j++) {
                dividePosList.add(new Position(latlng1.latitude + j * (latlng2.latitude - latlng1.latitude) / divideTimes, latlng1.longitude + j * (latlng2.longitude - latlng1.longitude) / divideTimes));
            }
            dividePosList.add(new Position(latlng2.latitude, latlng2.longitude));
        }

        return dividePosList;
    }

    private static JSONArray decodeGeometry(String encodedGeometry, boolean inclElevation) {
        JSONArray geometry = new JSONArray();
        int len = encodedGeometry.length();
        int index = 0, lat = 0, lng = 0, ele = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedGeometry.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedGeometry.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            if(inclElevation){
                result = 1;
                shift = 0;
                do {
                    b = encodedGeometry.charAt(index++) - 63 - 1;
                    result += b << shift;
                    shift += 5;
                } while (b >= 0x1f);
                ele += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            }
            JSONArray location = new JSONArray();
            location.add(String.valueOf(lat / 1E5));
            location.add(String.valueOf(lng / 1E5));
            geometry.add(location);
        }
        return geometry;
    }

    private static void setElevation(ArrayList<Position> posList){ // argument ArrayList<Position>의 elevation을 설정한다

        StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/elevation/json?locations=");

        for(int i = 0; i < posList.size() - 1; i++) {
            Position pos = posList.get(i);
            urlBuilder.append(pos.getLatLng().latitude + "," + pos.getLatLng().longitude + "|");
        }
        urlBuilder.append(posList.get(posList.size() - 1).getLatLng().latitude + "," + posList.get(posList.size() - 1).getLatLng().longitude);
        urlBuilder.append("&key=AIzaSyBDkaEcbQLVnapvHxfIFZFfwJAni4R52h4");
        URL url;

        try {
            url = new URL(urlBuilder.toString());

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            BufferedReader rd;
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
            JSONArray results = (JSONArray)jsonObject.get("results");
            for(int i = 0; i < results.size(); i++) {
                JSONObject object = (JSONObject)results.get(i);
                posList.get(i).setElevation(Double.parseDouble(object.get("elevation").toString()));
            }

            rd.close();
            conn.disconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static double calcWeight(ArrayList<Position> dividePosList) { // 주어진 route의 weight을 계산한다

        double weight = 0;

        for(int i = 0; i < dividePosList.size() - 1; i++) {
            double dist = Position.getDistance(dividePosList.get(i).getLatLng(), dividePosList.get(i + 1).getLatLng());
            double radian = Math.atan((dividePosList.get(i + 1).getEle() - dividePosList.get(i).getEle()) / dist);
            if(radian >= 0) weight += dist * (1 + (radian * 3)) * (1 + (radian * 3));
            else if(radian <= -0.7) weight -= dist * (radian - 1) * (radian - 1);
            else weight += dist;
        }

        return weight;
    }


}