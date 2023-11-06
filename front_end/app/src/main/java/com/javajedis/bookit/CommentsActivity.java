package com.javajedis.bookit;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.javajedis.bookit.recyclerView.adapter.Comments_RecyclerViewAdapter;
import com.javajedis.bookit.util.BackNavigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentsActivity extends AppCompatActivity {

    private final String TAG = "CommentsActivity";

    private String buildingCode;
    private String roomNumber;

    ArrayList<String> commentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        OnBackPressedCallback callback = BackNavigation.backToMain(this);
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        String codePlusNumber = getIntent().getStringExtra("codePlusNumber");

        assert codePlusNumber != null;
        String[] parts = codePlusNumber.split(" ");
        buildingCode = parts[0];
        roomNumber = parts[1];

        getComments();

        Button postComment = findViewById(R.id.post_comment_button);
        postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Going to post comment");
                Intent postIntent = new Intent(CommentsActivity.this, PostActivity.class);
                postIntent.putExtra("buildingCode", buildingCode);
                postIntent.putExtra("roomNumber", roomNumber);
                postIntent.putExtra("commenting", true);
                startActivity(postIntent);
            }
        });
    }

    private void getComments() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/studyrooms/" + buildingCode + "/" + roomNumber + "/comments";
        System.out.println(url);
        Log.d(TAG, url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue((new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "GET request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        System.out.println(jsonResponse);
//                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
//                        JSONArray roomsArray = new JSONArray(jsonResponse);
                        JSONArray commentsArray = responseObject.getJSONArray("data");

                        for (int i = 0; i < commentsArray.length(); i++) {
                            String comment = commentsArray.getString(i);
                            commentsList.add(comment);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.comments_recycler_view);
                                Comments_RecyclerViewAdapter adapter = new Comments_RecyclerViewAdapter(CommentsActivity.this, commentsList);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(CommentsActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }
}