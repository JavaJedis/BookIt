package com.javajedis.bookit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

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


public class LoginActivity extends AppCompatActivity {

    // checking login commit/push

    final static String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;

    private String deviceToken;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG,"Login result code: " + result.getResultCode());
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        deviceToken = task.getResult();

                        Log.d("FCM TOKEN", deviceToken);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        Log.w(TAG, "In the LoginActivity");
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(view -> signIn());

        Button guestButton = findViewById(R.id.guest_button);
        guestButton.setOnClickListener(view -> {
            Log.d(TAG, "Going back to MainActivity without signing into Google");

            Boolean continueAsGuest = true;
            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.putExtra("continueAsGuest", continueAsGuest);
            startActivity(mainIntent);
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            Log.w(TAG, "Trying to get Google account");
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // signed in successfully
            String loggedIn = account.getGivenName();
            Log.d(TAG, "signed in successfully to: " + loggedIn);
            sendUser(account);

            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            mainIntent.putExtra("clientName", loggedIn);
            startActivity(mainIntent);
            Log.w(TAG, "Going back to MainActivity");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void sendUser(GoogleSignInAccount account) {
        OkHttpClient client = new OkHttpClient();

        String postUrl = "https://bookit.henrydhc.me/user/login";

        try {
            JSONObject jsonRequest = new JSONObject();
            try {
//                jsonRequest.put("email", account.getEmail());
                jsonRequest.put("token", account.getIdToken());
                jsonRequest.put("device_token", deviceToken);
                System.out.println(account.getIdToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e("LoginActivity", "POST request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.d("LoginActivity", responseBody);
                    } else {
                        Log.e("LoginActivity", "POST request failed with code: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}