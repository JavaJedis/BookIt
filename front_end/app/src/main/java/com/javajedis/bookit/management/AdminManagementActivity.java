package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;
import com.javajedis.bookit.recyclerview.adapter.AdminRecyclerViewAdapter;
import com.javajedis.bookit.util.BackNavigation;
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
    private AdminRecyclerViewAdapter adapter;
    private long lastToastTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        OnBackPressedCallback callback = BackNavigation.backToMain(this);
        this.getOnBackPressedDispatcher().addCallback(this, callback);

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
        addNewButton.setOnClickListener(v -> {
            Intent assignBuildingAdminIntent = new Intent(AdminManagementActivity.this, AssignBuildingAdminActivity.class);
            startActivity(assignBuildingAdminIntent);
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent buildingManagementIntent = new Intent(AdminManagementActivity.this, BuildingManagementActivity.class);

        buildingManagementIntent.putExtra("AdminEmail", adapter.getAdminEmails().get(position));

        startActivity(buildingManagementIntent);
    }

    private void filterList(String text) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String content : allAdmins) {
            if (content.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(content);
            }
        }
        if (filteredList.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            // avoid showing text repeatedly
            if (currentTime - lastToastTime >= Constant.TOAST_MIN_DURATION_MILLI_SECOND) {
                Toast.makeText(this, "No such Admin", Toast.LENGTH_SHORT).show();
                lastToastTime = System.currentTimeMillis();
            }

        } else {
            adapter.setFilterList(filteredList);
        }
    }

    private void initAdminData() {
        Log.d(TAG, "in init Admin Data" );
        OkHttpClient client = new OkHttpClient();

        String url = Constant.DOMAIN + "/user/admin";

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
                        System.out.println(jsonResponse);
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray jsonArray = responseObject.getJSONArray("data");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String adminEmail = jsonArray.getString(i);
                            allAdmins.add(adminEmail);
                        }
                        runOnUiThread(() -> {
                            adapter = new AdminRecyclerViewAdapter(AdminManagementActivity.this,AdminManagementActivity.this);
                            RecyclerView recyclerView = findViewById(R.id.admin_user_recyclerView);
                            adapter.setAdminEmails(allAdmins);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(AdminManagementActivity.this));
                        });
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response is not successful");
                    assert response.body() != null;
                    System.out.println(response.body());
                }
            }
        }));
    }
}
