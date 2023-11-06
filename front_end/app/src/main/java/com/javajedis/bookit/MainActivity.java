package com.javajedis.bookit;

//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
//import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.javajedis.bookit.management.AdminManagementActivity;
import com.javajedis.bookit.management.BuildingManagementActivity;
import com.javajedis.bookit.management.DeleteBuildingActivity;

import org.json.JSONException;
import org.json.JSONObject;

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
    private Button bookingsButton;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount account;

    private String deviceToken;

    private Button signOutButton;

    private Boolean permissionPostNotification = false;

    private String[] permissions;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions = new String[]{
                Manifest.permission.POST_NOTIFICATIONS
        };

        if (!permissionPostNotification) {
            requestPermissionNotification();
    }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("111894204425-9rmckprjgu7mgamsq6mdfum7m5jt1m0g.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        account = GoogleSignIn.getLastSignedInAccount(this);

        getUserTypeAndSetView();

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
                if (account == null) {
//                    signIn();
//                    updateHelloMessage(account);
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    Log.d(TAG, "Trying to open BookingsActivity");

                    Intent bookingsIntent = new Intent(MainActivity.this, BookingsActivity.class);
                    startActivity(bookingsIntent);
                }
            }
        });

        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    private void requestPermissionNotification() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            permissionPostNotification = true;
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d("Permission", "first else");
                // debug if needed
            } else {
                Log.d("Permission", "second else");
                // debug if needed
            }
            requestPermissionLauncherNotification.launch(permissions[0]);
        }
    }
    
    private ActivityResultLauncher<String> requestPermissionLauncherNotification =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted->{
                if (isGranted) {
                    permissionPostNotification = true;
                } else {
                    permissionPostNotification = false;
                    showPermissionDialog("Notification Permission");
                }
            });

    public void showPermissionDialog(String permission) {
        new AlertDialog.Builder(
                MainActivity.this
        ).setTitle("Alert for Permission")
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void getUserTypeAndSetView() {
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
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        System.out.println(responseBody);
                        // You can parse and process the response data as needed
                        Log.d(TAG, "Got response from server: " + responseBody);
                        try {
                            JSONObject responseObject = new JSONObject(responseBody);
                            String userType = responseObject.getString("data");
                            switch (userType) {
                                case "superadmin":
                                    showSuperAdminView();
                                    break;
                                case "admin":
                                    showAdminView();
                                    break;
                                case "user":
                                    // do nothing right now
                                    break;
                                default:
                                    Log.e(TAG, "Error: unknown user type : " + responseBody);
                                    break;
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e(TAG, "Request was not successful. Response code: " + response.code());
                        assert response.body() != null;
                        System.out.println(response.body().string());
                    }
                }
            });
        } else {
            Log.e(TAG, "User is not signed in.");
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

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Log out successful!");
                        Toast.makeText(MainActivity.this, "You have been signed out", Toast.LENGTH_SHORT).show();

                    }
                });
        Intent loginBuildingIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginBuildingIntent);
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
        String helloMessage = "Hello, " + clientName + ".";
        helloMessageTextView.setText(helloMessage);
    }

    private void showAdminView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button manageRoomButton = findViewById(R.id.manage_room_button);
                manageRoomButton.setVisibility(View.VISIBLE);
                ImageView manageRoomImageView = findViewById(R.id.manage_room_imageView);
                manageRoomImageView.setVisibility(View.VISIBLE);

                manageRoomButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent buildingManagementIntent = new Intent(MainActivity.this, BuildingManagementActivity.class);
                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

                        assert account != null;
                        buildingManagementIntent.putExtra("AdminEmail", account.getEmail());

                        startActivity(buildingManagementIntent);
                    }
                });
            }
        });
    }

    private void showSuperAdminView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button manageBuildingButton = findViewById(R.id.manage_building_button);
                manageBuildingButton.setVisibility(View.VISIBLE);
                ImageView manageBuildingImageView = findViewById(R.id.manage_building_imageView);
                manageBuildingImageView.setVisibility(View.VISIBLE);

                manageBuildingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent deleteBuildingIntent = new Intent(MainActivity.this, DeleteBuildingActivity.class);
                        startActivity(deleteBuildingIntent);
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
                        startActivity(manageAdminIntent);
                    }
                });
            }
        });
    }
}