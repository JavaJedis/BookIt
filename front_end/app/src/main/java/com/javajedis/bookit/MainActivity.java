package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.javajedis.bookit.management.AdminManagementActivity;
import com.javajedis.bookit.management.BuildingManagementActivity;
import com.javajedis.bookit.management.RoomManagementActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// this activity represents the home page of the app
public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    TextView helloMessageTextView;
    private String clientName;
    private Button exploreButton;
    private Button searchButton;
    private Button filterButton;
    private String userType;
    private Button bookingsButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getUserType();

        showSuperAdminView();

        exploreButton = findViewById(R.id.explore_button);
        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Trying to open map of UBC");

                Intent mapsIntent = new Intent(MainActivity.this, ExploreActivity.class);
                startActivity(mapsIntent);
            }
        });

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Trying to open SearchActivity");

                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });

        filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Trying to open FilterActivity");

                Intent filterIntent = new Intent(MainActivity.this, FilterActivity.class);
                startActivity(filterIntent);
            }
        });

        bookingsButton = findViewById(R.id.bookings_button);
        bookingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Trying to open BookingsActivity");

                Intent bookingsIntent = new Intent(MainActivity.this, BookingsActivity.class);
                startActivity(bookingsIntent);
            }
        });
    }

    // GET request while sending info to BE: https://chat.openai.com/share/c6b266b7-c9c2-4cd7-91f2-7a307b4ecc45
    private void getUserType() {
        OkHttpClient client = new OkHttpClient();

        String getUrl = "https://bookit.henrydhc.me/user/type";

        // perform a get request while sending account.getId()
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

//        getUrl += "?token=" + account.getIdToken();

        if (account != null) {
            getUrl += "?token=" + account.getIdToken();
            System.out.println(getUrl);
//            JSONObject jsonRequest = new JSONObject();
//            try {
//                jsonRequest.put("token", account.getIdToken());
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

//            RequestBody requestBody = RequestBody.create(parse("application/json"), jsonRequest.toString());

            Request request = new Request.Builder().url(getUrl).get().build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "GET request failed: " + e.getMessage());
                    userType = "regular";
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        System.out.println(responseBody);
                        // You can parse and process the response data as needed
                    } else {
                        Log.e(TAG, "Request was not successful. Response code: " + response.code());
                        userType = "regular";
                    }
                }
            });
        } else {
            Log.e(TAG, "User is not signed in.");
            userType = "regular";
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        Intent intent = getIntent();
        boolean continueAsGuest = intent.getBooleanExtra("continueAsGuest", false);
        if (account == null && !continueAsGuest) {
            // the user has not signed in, prompt user to do so
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }
        if (account != null) {
            updateHelloMessage(account);
        }
    }

    private void updateHelloMessage(GoogleSignInAccount account) {
        clientName = account.getGivenName();

        if (clientName == null) {
            clientName = "Guest";
        }

        Intent intent = getIntent();
        String loggedInName = intent.getStringExtra("clientName");
        if (loggedInName != null) {
            clientName = loggedInName;
        }
        helloMessageTextView = findViewById(R.id.hello_message_textview);
        helloMessageTextView.setText("Hello, " + clientName + ".");
    }

    private void showAdminView() {
        Button manageRoomButton = findViewById(R.id.manage_room_button);
        manageRoomButton.setVisibility(View.VISIBLE);
        ImageView manageRoomImageView = findViewById(R.id.manage_room_imageView);
        manageRoomImageView.setVisibility(View.VISIBLE);

        manageRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageRoomIntent = new Intent(MainActivity.this, RoomManagementActivity.class);
                // TODO: info needed to display room management activity
//                manageRoomIntent.putExtra("buildingName", );
//                manageRoomIntent.putExtra("buildingCode", );

                startActivity(manageRoomIntent);
            }
        });
    }

    private void showSuperAdminView() {
        Button manageBuildingButton = findViewById(R.id.manage_building_button);
        manageBuildingButton.setVisibility(View.VISIBLE);
        ImageView manageBuildingImageView = findViewById(R.id.manage_building_imageView);
        manageBuildingImageView.setVisibility(View.VISIBLE);

        manageBuildingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageBuildingIntent = new Intent(MainActivity.this, BuildingManagementActivity.class);
                // TODO: info needed to display building management activity

                startActivity(manageBuildingIntent);
            }
        });

        Button manageAdminButton = findViewById(R.id.manage_admin_button);
        manageAdminButton.setVisibility(View.VISIBLE);
        ImageView manageAdminImageView = findViewById(R.id.manage_admin_imageView);
        manageAdminImageView.setVisibility(View.VISIBLE);

        manageAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manageAdminIntent = new Intent(MainActivity.this, AdminManagementActivity.class);
                // TODO: email need to be changed
                manageAdminIntent.putExtra("AdminEmail", "abc@gmail.com");
                startActivity(manageAdminIntent);
            }
        });
    }
}