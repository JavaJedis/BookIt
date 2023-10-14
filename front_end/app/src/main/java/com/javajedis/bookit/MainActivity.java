package com.javajedis.bookit;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

// this activity represents the home page of the app
public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    TextView helloMessageTextView;
    private String clientName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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