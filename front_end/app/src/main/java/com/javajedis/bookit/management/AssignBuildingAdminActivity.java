package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.javajedis.bookit.DynamicBuildingActivity;
import com.javajedis.bookit.R;
import com.javajedis.bookit.SearchActivity;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.Buildings_RecyclerViewAdapter;
import com.javajedis.bookit.util.Constant;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class AssignBuildingAdminActivity extends AppCompatActivity implements RecyclerViewInterface {

    ArrayList<String> allBuildings;
    ArrayList<String> showingBuildingList;

    private Buildings_RecyclerViewAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_building);

        String adminEmail = getIntent().getStringExtra("AdminEmail");
        String userEmail = getIntent().getStringExtra("UserEmail");

        initBuildingData();

        SearchView searchView = findViewById(R.id.assign_building_searchView);
        searchView.clearFocus();
        searchView.setQueryHint("Search Buildings");

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

    }


    @Override
    public void onItemClick(int position) {
        Intent AdminManagementIntent = new Intent(AssignBuildingAdminActivity.this, AdminManagementActivity.class);



    }

    private void filterList(String newText) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String building : showingBuildingList) {
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

    private void initBuildingData() {
        OkHttpClient client = new OkHttpClient();
        String url = Constant.DOMAIN + "/building_all";
        Request request = new Request.Builder().url(url).get().build();

    }
}