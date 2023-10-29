package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.DynamicBuildingActivity;
import com.javajedis.bookit.R;
import com.javajedis.bookit.SearchActivity;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.AdminUsers_RecyclerViewAdapter;
import com.javajedis.bookit.recyclerView.adapter.Buildings_RecyclerViewAdapter;
import com.javajedis.bookit.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminManagementActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "AdminManagementActivity";
    private ArrayList<String> allAdmins = new ArrayList<>();
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<String> showingList;
    private boolean showCurrentAdminMode = true;

    private String myEmail;
    private AdminUsers_RecyclerViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        myEmail = getIntent().getStringExtra("AdminEmail");

        initData(true);
        initData(false);

        SearchView searchView = findViewById(R.id.admin_user_searchView);
        searchView.clearFocus();
        if (showCurrentAdminMode) {
            searchView.setQueryHint("Search Admin By Email");
        } else {
            searchView.setQueryHint("Search User By Email");
        }
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

        adapter = new AdminUsers_RecyclerViewAdapter(AdminManagementActivity.this,AdminManagementActivity.this);
        RecyclerView recyclerView = findViewById(R.id.admin_user_recyclerView);

        ImageView adminImageView = findViewById(R.id.admin_imageView);
        ImageView userImageView = findViewById(R.id.user_imageView);


        Button showCurrentButton = findViewById(R.id.show_current_admin_button);
        showCurrentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Getting Admin data");
                showCurrentAdminMode = true;
                adminImageView.setVisibility(View.VISIBLE);
                userImageView.setVisibility(View.GONE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showingList = allAdmins;
                        adapter.setUserEmails(showingList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(AdminManagementActivity.this));
                    }
                });
            }
        });

        Button addNewButton = findViewById(R.id.add_new_admin_button);
        addNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Getting User data");
                showCurrentAdminMode = false;
                adminImageView.setVisibility(View.GONE);
                userImageView.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showingList = allUsers;
                        adapter.setUserEmails(showingList);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(AdminManagementActivity.this));
                    }
                });
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        if (showCurrentAdminMode) {
            Intent AssignBuildingIntent = new Intent(AdminManagementActivity.this, AssignBuildingAdminActivity.class);

            AssignBuildingIntent.putExtra("AdminEmail", myEmail);
            AssignBuildingIntent.putExtra("UserEmail", showingList.get(position));

            startActivity(AssignBuildingIntent);
        } else {
            Intent BuildingManagementIntent = new Intent(AdminManagementActivity.this, BuildingManagementActivity.class);

            BuildingManagementIntent.putExtra("UserEmail", showingList.get(position));

            startActivity(BuildingManagementIntent);
        }

    }

    private void filterList(String text) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String content : showingList) {
            if (content.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(content);
            }
        }
        if (filteredList.isEmpty()) {
            if (showCurrentAdminMode) {
                Toast.makeText(this, "No such Admin", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No such User", Toast.LENGTH_SHORT).show();
            }
        } else {
            adapter.setFilterList(filteredList);
        }
    }

    private void initData(boolean isAdmin) {
        Log.d(TAG, "in init Data" );
        OkHttpClient client = new OkHttpClient();

        String url;
        if (showCurrentAdminMode) {
            url = Constant.DOMAIN + "/admins_all";
        } else {
            url = Constant.DOMAIN + "/users_all";
        }

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

                        JSONArray data;
                        if (isAdmin) {
                            data = firstObject.getJSONArray("admins");
                        } else {
                            data = firstObject.getJSONArray("users");
                        }
                        // format
                        String userData = data.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    fillData(isAdmin, userData);
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

    private void fillData(boolean isAdmin, String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);


            if (isAdmin) {
                String adminEmail = object.getString("admin_email");
                allAdmins.add(adminEmail);
            } else {
                String userEmail = object.getString("user_email");
                allAdmins.add(userEmail);
            }
        }
    }
}
