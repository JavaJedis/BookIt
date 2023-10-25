package com.javajedis.bookit;

import static okhttp3.MediaType.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getUserType();

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

            Request request = new Request.Builder()
                    .url(getUrl)
                    .get()
                    .build();

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
}