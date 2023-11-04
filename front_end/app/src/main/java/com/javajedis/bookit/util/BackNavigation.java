package com.javajedis.bookit.util;

import android.content.Context;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.javajedis.bookit.CommentsActivity;
import com.javajedis.bookit.MainActivity;
import com.javajedis.bookit.management.BuildingManagementActivity;

public class BackNavigation {

    public static OnBackPressedCallback backToMain(AppCompatActivity activity) {
        return new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
                Intent intent = new Intent(activity, MainActivity.class);
                if (account != null) {
                    intent.putExtra("clientName", account.getGivenName());
                } else {
                    intent.putExtra("continueAsGuest", true);
                }
                activity.startActivity(intent);
                activity.finish();
            }
        };
    }

    public static OnBackPressedCallback backToBuildingManagement(AppCompatActivity activity) {
        return new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
                Intent intent = new Intent(activity, BuildingManagementActivity.class);
                if (account != null) {
                    intent.putExtra("AdminEmail", account.getEmail());
                }
                activity.startActivity(intent);
                activity.finish();
            }
        };
    }
}
