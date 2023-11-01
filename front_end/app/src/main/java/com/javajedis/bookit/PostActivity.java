package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class PostActivity extends AppCompatActivity {

    private final String TAG = "PostActivity";
    private EditText mEditTextMessage;
    private Button postCommentButton;
    private String post;
    private Boolean commenting;
    private String messageType;
    private String postURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        commenting = getIntent().getBooleanExtra("commenting", false);

        mEditTextMessage = findViewById(R.id.edit_text_message);

        postCommentButton = findViewById(R.id.post_comment_bottom_button);
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Posting comment");

                post = mEditTextMessage.getText().toString();
                if (post.length() == 0) {
                    Toast.makeText(PostActivity.this, "Please write something before trying to post.", Toast.LENGTH_SHORT).show();
                } else if (post.length() <= 20) {
                    Toast.makeText(PostActivity.this, "Please write something greater than 20 characters!", Toast.LENGTH_SHORT).show();
                } else if (post.length() > 200) {
                    Toast.makeText(PostActivity.this, "Please write something less than or equal to 200 characters!", Toast.LENGTH_SHORT).show();
                } else {
                    postURL = "https://bookit.henrydhc.me/studyrooms/" + getIntent().getStringExtra("buildingCode") + "/" + getIntent().getStringExtra("roomNumber");
                    if (commenting) {
                        postURL += "/comments";
                        messageType = "comment";
                    } else {
                        System.out.println("yo");
                        postURL += "/report";
                        messageType = "msg";
                    }
                    postComment();
                }
            }
        });
    }

    private void postComment() {
        OkHttpClient client = new OkHttpClient();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                assert account != null;
                jsonRequest.put("token", account.getIdToken());
                jsonRequest.put(messageType, post);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

            Request request = new Request.Builder().url(postURL).post(requestBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "POST request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.d(TAG, responseBody);
                        if (commenting) {
                            Intent commentsIntent = new Intent(PostActivity.this, CommentsActivity.class);
                            String codePlusNumber = getIntent().getStringExtra("buildingCode") + " " + getIntent().getStringExtra("roomNumber");
                            commentsIntent.putExtra("codePlusNumber", codePlusNumber);
                            startActivity(commentsIntent);
                        } else {
                            String loggedIn = account.getGivenName();

                            Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
                            mainIntent.putExtra("clientName", loggedIn);
                            startActivity(mainIntent);
                        }
                    } else {
                        Log.e(TAG, "POST request failed with code: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}