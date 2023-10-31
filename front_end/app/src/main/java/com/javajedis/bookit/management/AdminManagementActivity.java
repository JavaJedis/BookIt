package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.Admin_RecyclerViewAdapter;
import com.javajedis.bookit.util.Constant;

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

public class AdminManagementActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "AdminManagementActivity";
    private final ArrayList<String> allAdmins = new ArrayList<>();
    private ArrayList<String> showingAdminList;
    private Admin_RecyclerViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        initAdminData();

        SearchView searchView = findViewById(R.id.admin_user_searchView);
        searchView.clearFocus();

        searchView.setQueryHint("Search Admin By Email");

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

        Button addNewButton = findViewById(R.id.add_new_admin_button);
        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent assignBuildingAdminIntent = new Intent(AdminManagementActivity.this, AssignBuildingAdminActivity.class);
                startActivity(assignBuildingAdminIntent);
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent buildingManagementIntent = new Intent(AdminManagementActivity.this, BuildingManagementActivity.class);

        buildingManagementIntent.putExtra("AdminEmail", showingAdminList.get(position));
        buildingManagementIntent.putExtra("userType", "superadmin");

        startActivity(buildingManagementIntent);
    }

    private void filterList(String text) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String content : showingAdminList) {
            if (content.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(content);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No such Admin", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilterList(filteredList);
        }
    }

    private void initAdminData() {
        Log.d(TAG, "in init Admin Data" );
        OkHttpClient client = new OkHttpClient();

        String url = Constant.DOMAIN + "/admins_all";

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

                        JSONArray data = firstObject.getJSONArray("admins");
                        // format
                        String dataString = data.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    fillData(dataString);
                                    adapter = new Admin_RecyclerViewAdapter(AdminManagementActivity.this,AdminManagementActivity.this);
                                    RecyclerView recyclerView = findViewById(R.id.admin_user_recyclerView);
                                    showingAdminList = allAdmins;
                                    adapter.setAdminEmails(showingAdminList);
                                    recyclerView.setAdapter(adapter);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(AdminManagementActivity.this));
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

    private void fillData(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);

            String adminEmail = object.getString("admins_email");
            allAdmins.add(adminEmail);
        }
    }
}
