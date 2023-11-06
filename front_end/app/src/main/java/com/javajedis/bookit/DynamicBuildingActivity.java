package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.javajedis.bookit.model.RoomModel;
import com.javajedis.bookit.recyclerView.adapter.RN_RecyclerViewAdapter;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// code on how to use RecyclerView: https://youtu.be/Mc0XT58A1Z4?si=qYZ41YoIpvQ_4wL0,
//                                  https://youtu.be/7GPUpvcU1FE?si=s3ph_3ehYePOocRk

public class DynamicBuildingActivity extends AppCompatActivity implements RecyclerViewInterface {
    private final String TAG = "DynamicBuildingActivity";
    ArrayList<RoomModel> roomModels = new ArrayList<>();
    List<String> roomNames = new ArrayList<>();

    private final Map<String, Map<String, String>> roomDictionary = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_building);

        if (Objects.equals(getIntent().getStringExtra("type"), "ils")) {
            getBuildingInfoILS();
        } else if (Objects.equals(getIntent().getStringExtra("type"), "lecture")) {
            getBuildingInfoLecture();
        } else {
            getBuildingInfoStudy();
        }
//        getBuildingInfo();
    }

    private void getBuildingInfoStudy() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/studyrooms/" + getIntent().getStringExtra("buildingCode");
        System.out.println(url);
        Log.d(TAG, url);
        Request request = new Request.Builder().url(url).get().build();

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
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray roomsArray = responseObject.getJSONArray("data");
                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject roomInfo = roomsArray.getJSONObject(i);

                            String number = roomInfo.optString("_id");
                            String name = roomInfo.optString("building_name");
                            String code = roomInfo.optString("building_code");
                            String capacity = roomInfo.optString("capacity");
                            String address = roomInfo.optString("building_address");
                            JSONArray featuresArray = roomInfo.getJSONArray("features");

                            Map<String, String> roomDetails = new HashMap<>();
                            roomDetails.put("name", name);
                            roomDetails.put("address", address);
                            roomDetails.put("capacity", capacity);

                            String[] parts = featuresArray.toString().replaceAll("\\[|\\]", "").replaceAll("\"", "").split(",");

                            String description = "Features: ";
                            for (String part : parts) {
                                description += part;
                                if (!part.equals(parts[parts.length - 1])) {
                                    description += ", ";
                                }
                            }

                            roomDetails.put("description", description);

                            // update map of rooms
                            String key = code + " " + number;
                            roomDictionary.put(key, roomDetails);
                        }

                        // update list of rooms
                        roomNames.clear();
                        roomNames.addAll(roomDictionary.keySet());

                        setUpRoomModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.room_names_recyclerview);
                                RN_RecyclerViewAdapter adapter = new RN_RecyclerViewAdapter(DynamicBuildingActivity.this, roomModels, DynamicBuildingActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(DynamicBuildingActivity.this));
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

    private void setUpRoomModels() {
        int image;
        if (Objects.equals(getIntent().getStringExtra("type"), "ils")) {
            image = R.drawable.student_desk;
        } else if (Objects.equals(getIntent().getStringExtra("type"), "lecture")) {
            image = R.drawable.education;
        } else {
            image = R.drawable.office;
        }

        for (int i = 0; i < roomNames.size(); i++) {
            roomModels.add(new RoomModel(roomNames.get(i), image));
        }
    }

    private void getBuildingInfoILS() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/ils/" + getIntent().getStringExtra("buildingCode");
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
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray roomsArray = responseObject.getJSONArray("data");
                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject roomInfo = roomsArray.getJSONObject(i);

                            String name = roomInfo.optString("name");
                            String capacity = roomInfo.optString("capacity");
                            String address = roomInfo.optString("address");
                            String description = roomInfo.optString("description");
                            String image_url = roomInfo.optString("image_url");

                            Map<String, String> roomDetails = new HashMap<>();
                            roomDetails.put("address", address);
                            roomDetails.put("capacity", capacity);
                            roomDetails.put("description", description);
                            roomDetails.put("image_url", image_url);

                            // update map of rooms
                            roomDictionary.put(name, roomDetails);
                        }

                        // update list of rooms
                        roomNames.clear();
                        roomNames.addAll(roomDictionary.keySet());

                        setUpRoomModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.room_names_recyclerview);
                                RN_RecyclerViewAdapter adapter = new RN_RecyclerViewAdapter(DynamicBuildingActivity.this, roomModels, DynamicBuildingActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(DynamicBuildingActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }));
    }

    private void getBuildingInfoLecture() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/lecturehalls/" + getIntent().getStringExtra("buildingCode");
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
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray roomsArray = responseObject.getJSONArray("data");
                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject roomInfo = roomsArray.getJSONObject(i);

                            String name = roomInfo.optString("building name");
                            String roomCode = roomInfo.optString("room code");
                            String buildingCode = roomInfo.optString("building code");
                            String hours = roomInfo.optString("hours");
                            String address = roomInfo.optString("address");
                            String capacity = roomInfo.optString("capacity");
                            JSONObject unavailableTimes = roomInfo.optJSONObject("unavailable_times");
                            String image_url = roomInfo.optString("classroom_image_url");

                            Map<String, String> roomDetails = new HashMap<>();
                            roomDetails.put("buildingName", name);
                            roomDetails.put("hours", hours);
                            roomDetails.put("address", address);
                            roomDetails.put("capacity", capacity);
                            roomDetails.put("unavailableTimes", String.valueOf(unavailableTimes));
                            roomDetails.put("image_url", image_url);

                            // update map of rooms
                            String key = buildingCode + " " + roomCode;
                            roomDictionary.put(key, roomDetails);
                        }

                        // update list of rooms
                        roomNames.clear();
                        roomNames.addAll(roomDictionary.keySet());

                        setUpRoomModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.room_names_recyclerview);
                                RN_RecyclerViewAdapter adapter = new RN_RecyclerViewAdapter(DynamicBuildingActivity.this, roomModels, DynamicBuildingActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(DynamicBuildingActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }));
    }

    @Override
    public void onItemClick(int position) {
        Intent roomInfoIntent = new Intent(DynamicBuildingActivity.this, DynamicRoomActivity.class);

        String roomName = roomNames.get(position);
        Map<String, String> roomDetails = roomDictionary.get(roomName);

        roomInfoIntent.putExtra("roomName", roomName);
        assert roomDetails != null;
        roomInfoIntent.putExtra("address", roomDetails.get("address"));
        roomInfoIntent.putExtra("capacity", roomDetails.get("capacity"));
        roomInfoIntent.putExtra("description", roomDetails.get("description"));
        roomInfoIntent.putExtra("image_url", roomDetails.get("image_url"));
        roomInfoIntent.putExtra("hours", roomDetails.get("hours"));
        roomInfoIntent.putExtra("unavailableTimes", roomDetails.get("unavailableTimes"));
        roomInfoIntent.putExtra("type", getIntent().getStringExtra("type"));
        startActivity(roomInfoIntent);
    }
}