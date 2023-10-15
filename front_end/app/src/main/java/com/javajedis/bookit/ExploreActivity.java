package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javajedis.bookit.databinding.ActivityExploreBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ExploreActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityExploreBinding binding;

    final private String ILS_BUILDINGS = "[{\"building_code\":\"ALSC\",\"building_name\":\"Abdul Ladha Science Student Centre (ALSC) - Various Informal Learning Spaces\",\"address\":\"2055 East Mall, Vancouver, BC V6T 1Z4\",\"lat\":49.26607735,\"lon\":-123.25137314363899},{\"building_code\":\"ALRD\",\"building_name\":\"Allard Hall  (ALRD) - 1st Floor\",\"address\":\"1822 East Mall, Vancouver, BC V6T 1Z1\",\"lat\":49.26999585,\"lon\":-123.25328031852874},{\"building_code\":\"NEST\",\"building_name\":\"AMS Student Nest (NEST) - Various Informal Learning Spaces\",\"address\":\"6133 University Blvd, Vancouver, BC V6T 1Z1\",\"lat\":49.2661147,\"lon\":-123.2492381}]";
    final private String CLASSROOM_BUILDINGS = "[{\"building_code\":\"AERL\",\"building_name\":\"Aquatic Ecosystems Research Laboratory\",\"address\":\"2202 Main Mall, Vancouver, BC V6T 1Z4\",\"hours\":\"Mon to Fri: 7:30AM - 5:00PM, Sat/Sun/Holidays: Closed\",\"lat\":49.2628901,\"lon\":-123.2513752},{\"building_code\":\"ALRD\",\"building_name\":\"Allard Hall\",\"address\":\"1822 East Mall, Vancouver, BC V6T 1Z1\",\"hours\":\"Mon to Thurs: 7:30AM - 9:00PM, Fri: 7:30AM - 8:00PM, Sat: 7:30AM - 6:00PM, Sun: 10:00AM - 6:00PM, Holidays: Refer to the Law Library hours\",\"lat\":49.26999585,\"lon\":-123.25328031852874},{\"building_code\":\"ANGU\",\"building_name\":\"Henry Angus\",\"address\":\"2053 Main Mall, Vancouver, BC V6T 1Z2\",\"hours\":\"Mon to Sun: 7:00AM - 9:00PM, Holidays: Closed\",\"lat\":49.2655042,\"lon\":-123.2538704}]";

    private Button ilsButton;
    private Button lectureHallsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ilsButton = findViewById(R.id.ils_button);
        ilsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ExploreActivity", "Setting locations of ILS buildings");
                try {
                    setLocations(ILS_BUILDINGS);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        lectureHallsButton = findViewById(R.id.lecture_halls_button);
        lectureHallsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ExploreActivity", "Setting locations of classroom buildings");
                try {
                    setLocations(CLASSROOM_BUILDINGS);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 14.0f;

        // Add a marker in Sydney and move the camera
        LatLng ubc = new LatLng(49.2606, -123.2460);
//        mMap.addMarker(new MarkerOptions().position(ubc).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubc, zoomLevel));

//        // from https://youtu.be/m6zcM6Q2qZU?si=gn7pNdr4ZeUKDgyl
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(@NonNull Marker marker) {
//                String buildingName = marker.getTitle();
//
//                Intent buildingInfoIntent = new Intent(ExploreActivity.this, DynamicBuildingActivity.class);
//                buildingInfoIntent.putExtra("buildingName", buildingName);
//                startActivity(buildingInfoIntent);
//                return false;
//            }
//        });
    }

    private void setLocations(String buildings) throws JSONException {
        String type = "";
        if (Objects.equals(buildings, ILS_BUILDINGS)) {
            type = "ILS";
        } else if (Objects.equals(buildings, CLASSROOM_BUILDINGS)) {
            type = "classroom";
        }

        mMap.clear();
        JSONArray jsonArray = new JSONArray(buildings);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            double lat = obj.getDouble("lat");
            double lon = obj.getDouble("lon");
            String name = obj.getString("building_name");

            LatLng pin = new LatLng(lat, lon);
            mMap.addMarker(new MarkerOptions().position(pin).title(name));
        }

        Intent buildingInfoIntent = new Intent(ExploreActivity.this, DynamicBuildingActivity.class);
        // from https://youtu.be/m6zcM6Q2qZU?si=gn7pNdr4ZeUKDgyl
        String finalType = type;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String buildingName = marker.getTitle();

                // find building name in jsonArray
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String name = obj.getString("building_name");
                        if (name.equals(buildingName)) {
                            buildingInfoIntent.putExtra("buildingCode", obj.getString("building_code"));
                        }
                    } catch (JSONException e) {
                        Log.d("ExploreActivity", "Error when trying to find building name in jsonArray");
                    }
                }

                buildingInfoIntent.putExtra("buildingName", buildingName);
                buildingInfoIntent.putExtra("type", finalType);
                startActivity(buildingInfoIntent);
                return false;
            }
        });
    }
}