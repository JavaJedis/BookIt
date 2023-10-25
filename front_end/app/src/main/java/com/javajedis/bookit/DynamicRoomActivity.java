package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DynamicRoomActivity extends AppCompatActivity {

    private ImageView roomImage;
    private TextView roomName;
    private TextView address;
    private TextView capacity;
    private TextView description;

    private TextView hours;

    private Button directionsButton;

    private boolean locationPermissionGranted = false;
    private boolean isDetailsActivityRunning = false;
    private String cityGeo;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_room);

        // load image from URL: https://youtu.be/V1uxaRfSqu8?si=lW-nzkUHVHw870iW

        roomImage = findViewById(R.id.room_imageView);

        Picasso.get()
                .load(getIntent().getStringExtra("image_url"))
                .into(roomImage);

        roomName = findViewById(R.id.room_name_textView);
        roomName.setText(getIntent().getStringExtra("roomName"));

        address = findViewById(R.id.address_textView);
        address.setText("Address: " + getIntent().getStringExtra("address"));

        capacity = findViewById(R.id.capacity_textView);
        capacity.setText("Capacity: " + getIntent().getStringExtra("capacity"));

        description = findViewById(R.id.description_textView);
        description.setText(getIntent().getStringExtra("description"));

        if (Objects.equals(getIntent().getStringExtra("type"), "lecture")) {
            JSONObject unavailable;
            try {
                unavailable = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("unavailableTimes")));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            StringBuilder availability = new StringBuilder("Building Availability: " + getIntent().getStringExtra("hours") + "\n");
            availability.append("The room is closed/occupied from: \n");
            Iterator<String> days = unavailable.keys();
            while (days.hasNext()) {
                String day = days.next();
                try {
                    availability.append(day).append(": ").append(unavailable.getString(day)).append("\n");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            hours = findViewById(R.id.hours_textView);
            hours.setText(availability);
        }

        directionsButton = findViewById(R.id.directions_button);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationPermissionGranted) {
                    // not granted yet
                    ActivityCompat.requestPermissions(DynamicRoomActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    if (!isDetailsActivityRunning) {
                        getLocationInfo();
                    }
//                    getLocationInfo();
                }
            }
        });
    }

    private void getLocationInfo() {
        if (locationPermissionGranted) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Geocoder geocoder = new Geocoder(DynamicRoomActivity.this, Locale.getDefault());

                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        assert addresses != null;
                        if (!addresses.isEmpty()) {
                            System.out.println(addresses);
                            cityGeo = addresses.get(0).getAddressLine(0);
                        } else {
                            cityGeo = "Not Found";
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        cityGeo = "Error";
                    }
                    startDetailsActivity();
                    locationManager.removeUpdates(this);
                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getLocationInfo();
            } else {
                locationPermissionGranted = false;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isDetailsActivityRunning = false; // Reset the flag when the MainActivity is stopped
    }

    private void startDetailsActivity() {
        if (!isDetailsActivityRunning) {
            isDetailsActivityRunning = true;

            // opening Google Maps for navigation: https://youtu.be/WiMa7nh7rF4?si=m8RUFufQeYRbCpXb

            Uri uri = Uri.parse("https://www.google.com/maps/dir/" + cityGeo + "/" + getIntent().getStringExtra("address"));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.out.println(cityGeo);
        }
    }
}