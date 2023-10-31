package com.javajedis.bookit;

import android.app.Service;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TAG = "MyFirebaseMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

//        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
//
//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }
//
//        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
//        sendNotification(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
    }

//    private void sendNotification(String from, String body) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(MyFirebaseMessagingService.this.getApplication(), from + " -> " + body, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
