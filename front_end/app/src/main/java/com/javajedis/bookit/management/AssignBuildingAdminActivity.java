package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.Building_Selection_RecyclerViewAdapter;
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

public class AssignBuildingAdminActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "AssignBuildingAdminActivity";
    private ArrayList<String> allBuildings = new ArrayList<>();
    private ArrayList<String> showingList = new ArrayList<>();
    private Building_Selection_RecyclerViewAdapter adapter;
    private EditText editText;
    private String newAdminEmail;
    private String selectedBuilding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_building);

        initStudyRoomData();

        SearchView searchView = findViewById(R.id.assign_building_searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        Button confirmAddButton = findViewById(R.id.submit_assign_admin_button);
        confirmAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedBuilding != null) {
                    Intent adminManagementIntent = new Intent(AssignBuildingAdminActivity.this, AdminManagementActivity.class);
                    editText = findViewById(R.id.new_admin_email);
                    newAdminEmail = editText.getText().toString();
                    ServerRequests.requestAddAdmin(newAdminEmail, selectedBuilding, AssignBuildingAdminActivity.this, adminManagementIntent);
                } else {
                    Toast.makeText(AssignBuildingAdminActivity.this, "Must select a building assign to this admin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        selectedBuilding = adapter.getSelected();
        Button confirmAddButton = findViewById(R.id.submit_assign_admin_button);
        confirmAddButton.setEnabled(true);
    }

    private void filterList(String newText) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String building : allBuildings) {
            if (building.toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(building);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No such building", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilterList(filteredList);
        }
    }

    private void initStudyRoomData() {
        Log.d(TAG, "in init StudyRoomData" );
        OkHttpClient client = new OkHttpClient();

        String url = Constant.DOMAIN + "/studyrooms/building_all";

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue((new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "GET data request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray jsonArray = responseObject.getJSONArray("data");
                        // Assuming "buildings" is always present in the first object
                        JSONObject firstObject = jsonArray.getJSONObject(0);
                        JSONArray data = firstObject.getJSONArray("buildings");
                        // format
                        String studyRooms = data.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONArray jsonArray = new JSONArray(studyRooms);
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject object = jsonArray.getJSONObject(i);
                                        String userEmail = object.getString("building_code");
                                        allBuildings.add(userEmail);
                                    }
                                    // setup adapter when data is ready
                                    showingList = allBuildings;
                                    adapter = new Building_Selection_RecyclerViewAdapter(AssignBuildingAdminActivity.this, showingList, AssignBuildingAdminActivity.this);
                                    RecyclerView recyclerView = findViewById(R.id.assign_building_recyclerView);
                                    recyclerView.setAdapter(adapter);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(AssignBuildingAdminActivity.this));
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error setting locations for Buildings");
                                }
                            }
                        });
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                }
            }
        }));
    }
}