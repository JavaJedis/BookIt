package com.javajedis.bookit.util;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class Authentication {

    public static String getCurrentAccountToken(Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account == null) {
            System.out.println("null");
            Toast.makeText(context, "You need to sign in to a google account", Toast.LENGTH_SHORT);
            return null;
        }
        System.out.println("token");
        return account.getIdToken();
    }

    public static String getCurrentAccountEmail(Context context) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        if (account == null) {
            System.out.println("null");
            Toast.makeText(context, "You need to sign in to a google account", Toast.LENGTH_SHORT);
            return null;
        }
        System.out.println("email");
        return account.getEmail();
    }

}
