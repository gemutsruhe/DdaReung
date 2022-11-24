package com.example.bikemap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bikemap.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private ArrayList<Station> stationState;
    private ArrayList<Position> crossRoadList;

    ArrayList<ArrayList<Polyline>> routePolyline;
    int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AtomicReference<ArrayList<Station>> atomicStationState = new AtomicReference<>();
        Thread getStationData = new Thread(){
            public void run(){
                ArrayList<Station> station = DataProcess.getStationState();
                atomicStationState.set(station);
            }
        };

        getStationData.start();
        try {
            getStationData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stationState = atomicStationState.get();

        DBHelper dbHelper = new DBHelper(MapsActivity.this, 1);
        crossRoadList = dbHelper.getCrossRoadList();

        Thread getPOI = new Thread(){
            public void run(){
                DataProcess.getPOI("중앙대학교");
            }
        };
        getPOI.start();
        selected = -1;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng pos = new LatLng(37.503064, 126.947617);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
        mMap.setMinZoomPreference(14);
        mMap.setMaxZoomPreference(17);
// 14 ~ 17
        drawStation(pos);

        drawRoute(new LatLng(37.504960, 126.953850), new LatLng(37.508841, 126.928875), crossRoadList);
        //drawRoute(new LatLng(37.480963, 126.953154), new LatLng(37.503064, 126.947617), crossRoadList);
    }

    private void drawStation(LatLng pos){
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker, null);
        View marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker, null);
        TextView tv_marker;
        tv_marker = marker_root_view.findViewById(R.id.tv_marker);

        for(int i = 0; i < stationState.size(); i++) {
            Station station = stationState.get(i);
            Position stationPos = station.getPosition();

            LatLng coord = stationPos.getLatLng();

            MarkerOptions markerOptions = new MarkerOptions().position(coord).title(station.getStationName()).zIndex(station.getParkingNum());
            tv_marker.setText(Integer.toString(station.getParkingNum()));
            mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker_root_view))));
        }
    }

    private void drawRoute(LatLng start, LatLng end, ArrayList<Position> crossRoadList){
        routePolyline = new ArrayList<>();
        AtomicReference<ArrayList<Direction>> atomicRoute = new AtomicReference<>();
        Thread getRoute = new Thread(){
            public void run(){
                ArrayList<Direction> route = Direction.getRoute(start, end, crossRoadList);
                atomicRoute.set(route);
            }
        };
        getRoute.start();
        try {
            getRoute.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener(){
            @Override
            public void onPolylineClick(Polyline polyline) {
                if(selected == -1) {
                    selectRoute((int) polyline.getZIndex());
                    selected = (int) polyline.getZIndex();
                }
                else {
                    unselectRoute();
                    selected = -1;
                }
            }
        });

        ArrayList<Direction> route = atomicRoute.get();
        for(int i = 0; i < route.size(); i++) {
            routePolyline.add(new ArrayList<>());
            PolylineOptions polyline = new PolylineOptions();

            ArrayList<Position> direction = route.get(i).getRouteList();

            for(int j = 0; j < direction.size() - 1; j++) {
                LatLng latlng1 = direction.get(j).getLatLng();
                LatLng latlng2 = direction.get(j + 1).getLatLng();
                double dist = Position.getDistance(latlng1, latlng2);
                double radian = Math.atan((direction.get(j + 1).getEle() - direction.get(j).getEle()) / dist);
                double degree = Math.toDegrees(radian);

                if(degree >= 15) {
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.RED).zIndex(i)));
                } else if(degree >= 5){
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.YELLOW).zIndex(i)));
                } else {
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.GREEN).zIndex(i)));
                }
                routePolyline.get(i).get(j).setClickable(true);
            }
        }
    }

    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private void selectRoute(int zIndex){
        for(int i = 0; i < routePolyline.size(); i++) {
            if(zIndex == i) continue;

            ArrayList<Polyline> directionPolyline = routePolyline.get(i);

            for(int j = 0; j < directionPolyline.size(); j++) {
                directionPolyline.get(j).setVisible(false);
            }
        }
    }

    private void unselectRoute() {
        for(int i = 0; i < routePolyline.size(); i++) {
            ArrayList<Polyline> directionPolyline = routePolyline.get(i);
            for(int j = 0; j < directionPolyline.size(); j++) {
                directionPolyline.get(j).setVisible(true);
            }
        }
    }
}