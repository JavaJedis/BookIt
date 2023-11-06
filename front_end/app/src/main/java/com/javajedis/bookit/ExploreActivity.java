package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.javajedis.bookit.databinding.ActivityExploreBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


// concise way to request location permissions: https://chat.openai.com/share/7d343f84-e7eb-4bff-81d0-d3fdfe68b2c2
public class ExploreActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String TAG = "ExploreActivity";

    private GoogleMap mMap;

    private String buildings;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
//            @Override
//            public void handleOnBackPressed() {
//                // Handle the back button event
//                finish();
//            }
//        };

        com.javajedis.bookit.databinding.ActivityExploreBinding binding = ActivityExploreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Button ilsButton = findViewById(R.id.ils_button);
        ilsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Setting locations of ILS buildings");
                String getUrl = "https://bookit.henrydhc.me/ils/building_all";
                getLocations(getUrl);
            }
        });

        Button lectureHallsButton = findViewById(R.id.lecture_halls_button);
        lectureHallsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Setting locations of Lecture Hall buildings");
//                try {
//                    setLocations(CLASSROOM_BUILDINGS);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
                String getUrl = "https://bookit.henrydhc.me/lecturehalls/building_all";
                getLocations(getUrl);
            }
        });

        Button studyRoomsButton = findViewById(R.id.study_rooms_button);
        studyRoomsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Setting locations of Study Room buildings");
                String getUrl = "https://bookit.henrydhc.me/studyrooms/building_all";
                getLocations(getUrl);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubc, zoomLevel));

        if (checkLocationPermission()) {
            // If permissions are already granted, proceed
            initMap();
        } else {
            // Request location permissions
            requestLocationPermission();
        }
    }

    // ChatGPT Usage: Yes

    // Check for location permissions
    private boolean checkLocationPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    // ChatGPT Usage: Yes

    // Request location permissions
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    // ChatGPT Usage: Yes
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize the map
                initMap();
            } else {
                // Permission denied, handle it (e.g., show a message)
                // You can show a message to the user or take other actions here
            }
        }
    }

    private void initMap() {
        // Your existing map initialization code

        // Enable My Location Layer on the map
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    private void setLocations(String buildings, String buildingType) throws JSONException {
        String type = "";
        float c = 0;
        if (Objects.equals(buildingType, "all_ils_buildings")) {
            type = "ils";
            c = BitmapDescriptorFactory.HUE_MAGENTA;
        } else if (Objects.equals(buildingType, "all_lecture_buildings")) {
            type = "lecture";
            c = BitmapDescriptorFactory.HUE_AZURE;
        } else if (Objects.equals(buildingType, "all_studyroom_buildings")) {
            type = "study";
            c = BitmapDescriptorFactory.HUE_ORANGE;
        }

        mMap.clear();
        JSONArray jsonArray = new JSONArray(buildings);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);

            double lat = obj.getDouble("lat");
            double lon = obj.getDouble("lon");
            String name = obj.getString("building_name");

            LatLng pin = new LatLng(lat, lon);

            // from https://youtu.be/g-YnGyBdV-s?si=nUgZa1jufreNc9H9
            MarkerOptions options = new MarkerOptions().position(pin).title(name);
            options.icon(BitmapDescriptorFactory.defaultMarker(c));
            mMap.addMarker(options);
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
                        Log.d(TAG, "Error when trying to find building name in jsonArray");
                    }
                }

                buildingInfoIntent.putExtra("buildingName", buildingName);
                buildingInfoIntent.putExtra("type", finalType);
                startActivity(buildingInfoIntent);
                return false;
            }
        });
    }

    private void getLocations(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue((new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "GET request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        System.out.println(jsonResponse);
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray jsonArray = responseObject.getJSONArray("data");
                        // Assuming "buildings" is always present in the first object
                        JSONObject firstObject = jsonArray.getJSONObject(0);
                        String buildingType = firstObject.getString("type");
                        System.out.println(buildingType);
                        JSONArray data = firstObject.getJSONArray("buildings");

                        // format
                        buildings = data.toString();
                        System.out.println(buildings);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    setLocations(buildings, buildingType);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error setting locations for ILS Buildings");
                                }
                            }
                        });
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "Get response not successful from server");
                }
            }
        }));
    }
}