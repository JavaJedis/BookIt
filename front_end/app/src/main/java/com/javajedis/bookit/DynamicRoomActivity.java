package com.javajedis.bookit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DynamicRoomActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    private boolean locationPermissionGranted = false;

    private boolean isMapsRunning = false;

    private String cityGeo;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleSignInAccount account;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
            }
    );

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_room);

        Log.w(TAG, "In the LoginActivity");
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("111894204425-9rmckprjgu7mgamsq6mdfum7m5jt1m0g.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        account = GoogleSignIn.getLastSignedInAccount(this);

        // load image from URL: https://youtu.be/V1uxaRfSqu8?si=lW-nzkUHVHw870iW

        ImageView roomImage = findViewById(R.id.room_imageView);

        if (getIntent().getStringExtra("image_url") != null) {
            Picasso.get()
                    .load(getIntent().getStringExtra("image_url"))
                    .into(roomImage);
        } else {
            roomImage.setImageResource(R.drawable.general_study_room);
        }

        TextView roomName = findViewById(R.id.room_name_textView);
        roomName.setText(getIntent().getStringExtra("roomName"));

        TextView address = findViewById(R.id.address_textView);
        address.setText("Address: " + getIntent().getStringExtra("address"));

        TextView capacity = findViewById(R.id.capacity_textView);
        capacity.setText("Capacity: " + getIntent().getStringExtra("capacity"));

        TextView description = findViewById(R.id.description_textView);
        description.setText(getIntent().getStringExtra("description"));

        if (Objects.equals(getIntent().getStringExtra("type"), "lecture")) {
            JSONObject unavailable = new JSONObject();
            try {
                unavailable = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("unavailableTimes")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            StringBuilder availability = new StringBuilder("Building Availability: " + getIntent().getStringExtra("hours") + "\n");
            availability.append("The room is closed/occupied from: \n");
            Iterator<String> days = unavailable.keys();
            while (days.hasNext()) {
                String day = days.next();
                try {
                    availability.append(day).append(": ").append(unavailable.getString(day)).append("\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            TextView hours = findViewById(R.id.hours_textView);
            hours.setText(availability);
        }

        Button bookNowButton = findViewById(R.id.book_now_button);
        Button viewCommentsButton = findViewById(R.id.view_comments_button);
        Button reportButton = findViewById(R.id.report_button);

        if (Objects.equals(getIntent().getStringExtra("type"), "study")) {
            bookNowButton.setVisibility(View.VISIBLE);
            viewCommentsButton.setVisibility(View.VISIBLE);
            reportButton.setVisibility(View.VISIBLE);
        } else {
            bookNowButton.setVisibility(View.GONE);
            viewCommentsButton.setVisibility(View.GONE);
            reportButton.setVisibility(View.GONE);
        }

        bookNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (account == null) {
                    signIn();
                }
                else {
                    Intent calendarIntent = new Intent(DynamicRoomActivity.this, CalendarActivity.class);
                    calendarIntent.putExtra("codePlusNumber", getIntent().getStringExtra("roomName"));
                    calendarIntent.putExtra("fromFilter", false);
                    startActivity(calendarIntent);
                }
            }
        });

        viewCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentsIntent = new Intent(DynamicRoomActivity.this, CommentsActivity.class);
                commentsIntent.putExtra("codePlusNumber", getIntent().getStringExtra("roomName"));
                startActivity(commentsIntent);
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DynamicRoomActivity", "Going to report");
                Intent postIntent = new Intent(DynamicRoomActivity.this, PostActivity.class);
                String codePlusNumber = getIntent().getStringExtra("roomName");
                assert codePlusNumber != null;
                String[] parts = codePlusNumber.split(" ");
                postIntent.putExtra("buildingCode", parts[0]);
                postIntent.putExtra("roomNumber", parts[1]);
                postIntent.putExtra("commenting", false);
                startActivity(postIntent);
            }
        });

        Button directionsButton = findViewById(R.id.directions_button);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!locationPermissionGranted) {
                    // not granted yet
                    ActivityCompat.requestPermissions(DynamicRoomActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    if (!isMapsRunning) {
                        getLocationInfo();
                    }
//                    getLocationInfo();
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.w(TAG, "Trying to get Google account");
            account = completedTask.getResult(ApiException.class);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
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
                    startGoogleMaps();
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
        isMapsRunning = false; // Reset the flag when the MainActivity is stopped
    }

    private void startGoogleMaps() {
        if (!isMapsRunning) {
            isMapsRunning = true;

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