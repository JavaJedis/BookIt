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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoomManagementActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "RoomManagementActivity";

    private final ArrayList<String> allStudyRooms = new ArrayList<>();

    private final ArrayList<String> showingList = new ArrayList<>();

    private Building_Selection_RecyclerViewAdapter adapter;

    private String selectedRoom;
    private String building;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        OnBackPressedCallback callback = BackNavigation.backToBuildingManagement(this);
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        building = getIntent().getStringExtra("building");

        initRoomData();

        Button deleteRoomButton = findViewById(R.id.delete_study_room_button);

        deleteRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedRoom != null) {
                    Intent roomManagementIntent = new Intent(RoomManagementActivity.this, RoomManagementActivity.class);
                    roomManagementIntent.putExtra("building", building);
                    ServerRequests.requestDeleteRoom(building, selectedRoom, RoomManagementActivity.this, roomManagementIntent);
                } else {
                    Toast.makeText(RoomManagementActivity.this, "Please select a room!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        selectedRoom = allStudyRooms.get(position);
        Button deleteRoomButton = findViewById(R.id.delete_study_room_button);
        deleteRoomButton.setEnabled(true);
    }

    private void initRoomData() {
        OkHttpClient client = new OkHttpClient();
        String url = Constant.DOMAIN + "/studyrooms/" + building;

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
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray roomsArray = responseObject.getJSONArray("data");
                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject roomInfo = roomsArray.getJSONObject(i);
                            String roomNumber = roomInfo.optString("_id");
                            allStudyRooms.add(roomNumber);

                            String code = roomInfo.optString("building_code");
                            String showingText = code + " " + roomNumber;
                            showingList.add(showingText);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.room_names_recyclerview);
                                adapter = new Building_Selection_RecyclerViewAdapter(RoomManagementActivity.this, showingList, RoomManagementActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(RoomManagementActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e(TAG, "Response not successful");
                    assert response.body() != null;
                    System.out.println(response.body());
                }
            }
        }));
    }


}
