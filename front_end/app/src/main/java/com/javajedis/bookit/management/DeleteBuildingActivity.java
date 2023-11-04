package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.Building_Selection_RecyclerViewAdapter;
import com.javajedis.bookit.util.BackNavigation;
import com.javajedis.bookit.util.Constant;
import com.javajedis.bookit.util.ServerRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeleteBuildingActivity extends AppCompatActivity implements RecyclerViewInterface {
    private final String TAG = "DeleteBuildingActivity";

    private ArrayList<String> allBuildings = new ArrayList<>();

    private final Set<String> allBuildingSet = new HashSet<>();

    private Building_Selection_RecyclerViewAdapter adapter;

    private String selectedBuilding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_building);

        OnBackPressedCallback callback = BackNavigation.backToMain(this);
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        initBuildingData();

        Button deleteBuildingButton = findViewById(R.id.delete_building_button);
        deleteBuildingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBuilding != null) {
                    Intent deleteBuildingIntent = new Intent(DeleteBuildingActivity.this, DeleteBuildingActivity.class);
                    ServerRequests.requestDeleteBuilding(selectedBuilding, DeleteBuildingActivity.this, deleteBuildingIntent);
//                    allBuildings.remove(selectedBuilding);

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            adapter = new Building_Selection_RecyclerViewAdapter(DeleteBuildingActivity.this, allBuildings, DeleteBuildingActivity.this);
//                            RecyclerView recyclerView = findViewById(R.id.building_names_recyclerview);
//                            recyclerView.setAdapter(adapter);
//                            recyclerView.setLayoutManager(new LinearLayoutManager(DeleteBuildingActivity.this));
//                        }
//                    });
                } else {
                    Toast.makeText(DeleteBuildingActivity.this, "Please select a building!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button addBuildingButton = findViewById(R.id.add_building_button);

        addBuildingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addBuildingIntent = new Intent(DeleteBuildingActivity.this, AddNewBuildingActivity.class);
                startActivity(addBuildingIntent);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        selectedBuilding = allBuildings.get(position);

        Button deleteBuildingButton = findViewById(R.id.delete_building_button);
        deleteBuildingButton.setEnabled(true);
    }

    private void initBuildingData() {
//        initStudySpaceType("ils");
//        initStudySpaceType("lecturehalls");
        initStudySpaceType("studyrooms");
    }

    private void initStudySpaceType(String spaceType) {
        OkHttpClient client = new OkHttpClient();
        String url = Constant.DOMAIN + "/" + spaceType + "/building_all";
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue((new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "GET " + spaceType + " building request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Get a response from server: " + jsonResponse);
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray jsonArray = responseObject.getJSONArray("data");
                        // Assuming "buildings" is always present in the first object
                        JSONObject firstObject = jsonArray.getJSONObject(0);
                        JSONArray data = firstObject.getJSONArray("buildings");
                        // format
                        String buildings = data.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    initData(buildings);
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error setting locations for " + spaceType + " Buildings");
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Get response not successful from server: ");
                }
            }
        }));
    }

    private void initData (String data) throws JSONException{
        JSONArray jsonArray = new JSONArray(data);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String buildingCode = object.getString("building_code");
            allBuildingSet.add(buildingCode);
        }
        allBuildings = new ArrayList<>(allBuildingSet);
        Collections.sort(allBuildings);

        // Create a new adapter and set it to the RecyclerView
        adapter = new Building_Selection_RecyclerViewAdapter(DeleteBuildingActivity.this, allBuildings, DeleteBuildingActivity.this);
        RecyclerView recyclerView = findViewById(R.id.building_names_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DeleteBuildingActivity.this));
    }
}
