package com.example.bikemap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bikemap.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private ArrayList<Station> stationState;
    ArrayList<ArrayList<Polyline>> routePolyline;
    ArrayList<ArrayList<Double>> degreeList;
    int selected;
    Marker startMarkerLocation, startMarker;
    Marker endMarkerLocation, endMarker;

    double redSlopeMin;
    double yellowSlopeMin;
    boolean tiltCheck;

    private Marker currentMarker = null;
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;
    private View mLayout;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    SupportMapFragment mapFragment;

    SearchView searchView;
    ListView listView;
    ArrayList<Place> placeList;

    String uid;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bottomNavigationView = findViewById(R.id.mapsBottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:{
                        return true;
                    }
                    case R.id.navigation_user:{
                        Intent user_intent = new Intent(getApplicationContext(), UserActivity.class);
                        startActivityForResult(user_intent, 1001);
                        return true;
                    }
                }
                return false;
            }
        });

        AtomicReference<ArrayList<Station>> atomicStationState = new AtomicReference<>();
        Thread getStationData = new Thread() {
            public void run() {
                ArrayList<Station> station = DataProcess.getStationState();
                atomicStationState.set(station);
            }
        };

        getStationData.start();

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        getChangedData();
        try {
            getStationData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stationState = atomicStationState.get();

        DBHelper dbHelper = new DBHelper(MapsActivity.this, 1);
        Direction.crossRoadList = dbHelper.getCrossRoadList();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mLayout = findViewById(R.id.layout_maps);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        selected = -1;
        startMarker = null;
        startMarkerLocation = null;
        endMarker = null;
        endMarkerLocation = null;

        searchView = findViewById(R.id.search_view);
        listView = findViewById(R.id.list_view);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(14);
        mMap.setMaxZoomPreference(17);
        LatLng initPos = new LatLng(37.5666805, 126.9784147);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPos, 16));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    LatLng lastPos = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPos, 16));
                }
            }
        });


        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions( MapsActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        drawStation();

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

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                if(startMarkerLocation == null) {
                    if(startMarker != null) startMarker.remove();
                    startMarkerLocation = marker;
                    setLocation(startMarkerLocation.getPosition(), "start");
                } else if(startMarkerLocation.equals(marker)) {
                    startMarkerLocation = null;
                    startMarker.remove();
                } else if(endMarkerLocation == null){
                    if(endMarker != null) endMarker.remove();
                    endMarkerLocation = marker;
                    setLocation(startMarkerLocation.getPosition(), "end");
                } else if(endMarkerLocation.equals(marker)) {
                    endMarkerLocation = null;
                    endMarker.remove();
                }
                if(startMarkerLocation != null && endMarkerLocation != null) {
                    routePolyline = new ArrayList<>();
                    degreeList = new ArrayList<>();
                    drawRoute(startMarkerLocation.getPosition(), endMarkerLocation.getPosition());
                    startMarkerLocation = null;
                    endMarkerLocation = null;
                }
            }
        });
        ArrayList<String> items = new ArrayList<String>();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, items) ;
        listView.setAdapter(adapter);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setBackgroundColor(Color.WHITE);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Thread getPOI = new Thread(){
                    public void run(){
                        items.clear();
                        placeList = DataProcess.getPOI(query);
                        if(placeList == null) return ;
                        for(int i = 0; i < placeList.size(); i++) {
                            items.add(placeList.get(i).getName());
                        }
                    }
                };
                getPOI.start();
                try {
                    getPOI.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener(){
            @Override
            public boolean onClose() {
                listView.performItemClick(new View(getApplicationContext()), -1, 0);
                searchView.setBackgroundColor(Color.TRANSPARENT);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i != -1) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(placeList.get(i).getLatLng()));
                }
                searchView.setBackgroundColor(Color.TRANSPARENT);
                items.clear();
                adapter.notifyDataSetChanged();
                searchView.setQuery("", false);
                searchView.clearFocus();
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mCurrentLocatiion = location;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                startLocationUpdates();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                } else {
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermission()) {
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }


    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }



    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setLocation(LatLng latlng, String marker) {
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions().position(latlng).zIndex(500);


        if(marker.compareTo("start") == 0) {
            startMarker = mMap.addMarker(markerOptions);
            startMarker.setVisible(true);
        } else if(marker.compareTo("end") == 0) {
            endMarker = mMap.addMarker(markerOptions);
            endMarker.setVisible(true);
        }
    }

    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }
        return false;
    }

    private void drawStation(){
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

    private void drawRoute(LatLng start, LatLng end){
        routePolyline = new ArrayList<>();
        degreeList = new ArrayList<>();
        AtomicReference<ArrayList<Direction>> atomicRoute = new AtomicReference<>();
        Thread getRoute = new Thread(){
            public void run(){
                ArrayList<Direction> route = Direction.getRoute(start, end);
                atomicRoute.set(route);
            }
        };
        getRoute.start();
        try {
            getRoute.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Direction> route = atomicRoute.get();
        for(int i = 0; i < route.size(); i++) {
            routePolyline.add(new ArrayList<>());
            degreeList.add(new ArrayList<>());
            ArrayList<Position> direction = route.get(i).getRouteList();
            boolean redTilt = false;
            for(int j = 0; j < direction.size() - 1; j++) {
                LatLng latlng1 = direction.get(j).getLatLng();
                LatLng latlng2 = direction.get(j + 1).getLatLng();
                double dist = Position.getDistance(latlng1, latlng2);
                double radian = Math.atan((direction.get(j + 1).getEle() - direction.get(j).getEle()) / dist);
                double degree = Math.toDegrees(radian);
                if(degree >= redSlopeMin) {
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.RED).zIndex(i)));
                    redTilt = true;
                } else if(degree >= yellowSlopeMin){
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.YELLOW).zIndex(i)));
                } else {
                    routePolyline.get(i).add(mMap.addPolyline(new PolylineOptions().add(latlng1, latlng2).color(Color.GREEN).zIndex(i)));
                }
                degreeList.get(i).add(degree);
                routePolyline.get(i).get(j).setClickable(true);
            }
            if(tiltCheck == false && redTilt == true) {
                for(int j = 0; j < routePolyline.get(i).size(); j++) {
                    routePolyline.get(i).get(j).setClickable(false);
                    routePolyline.get(i).get(j).setVisible(false);
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
        if(resultCode == RESULT_OK){
            if(data.getExtras().get("result").toString().compareTo("trans") == 0) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                getChangedData();
                setPolylineColor();
            } else if(data.getExtras().get("result").toString().compareTo("logout") == 0) {
                this.finish();
            }
        }
    }

    private void getChangedData(){
        databaseReference.child("user").child(uid).child("YellowSlopeMinimum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yellowSlopeMin = snapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child("user").child(uid).child("RedSlopeMinimum").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                redSlopeMin = snapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.child("user").child(uid).child("tiltCheck").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tiltCheck = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPolylineColor(){
        if(routePolyline == null) return;
        for(int i = 0; i < routePolyline.size(); i++) {
            if(tiltCheck == true && routePolyline.get(i).get(0).isVisible() == false) {
                for(int j = 0; j < routePolyline.get(i).size(); j++) {
                    routePolyline.get(i).get(j).setClickable(true);
                    routePolyline.get(i).get(j).setVisible(true);
                }
            }
            boolean redTilt = false;
            for (int j = 0; j < routePolyline.get(i).size(); j++) {
                double degree = degreeList.get(i).get(j);
                if (degree >= redSlopeMin) {
                    routePolyline.get(i).get(j).setColor(Color.RED);
                    redTilt = true;
                } else if (degree >= yellowSlopeMin) {
                    routePolyline.get(i).get(j).setColor(Color.YELLOW);
                } else {
                    routePolyline.get(i).get(j).setColor(Color.GREEN);
                }
            }
            if(tiltCheck == false && redTilt == true) {
                for(int j = 0; j < routePolyline.get(i).size(); j++) {
                    routePolyline.get(i).get(j).setClickable(false);
                    routePolyline.get(i).get(j).setVisible(false);
                }
            }
        }
    }
}