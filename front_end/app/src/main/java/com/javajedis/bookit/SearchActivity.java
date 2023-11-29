package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;

import android.os.Bundle;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.javajedis.bookit.recyclerview.RecyclerViewInterface;
import com.javajedis.bookit.recyclerview.adapter.BuildingsRecyclerViewAdapter;
import com.javajedis.bookit.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "SearchActivity";
    private final String[] allBuildingCodes = {"AERL", "ALRD", "ANGU", "ANSO", "AUDX", "BIOL", "BUCH", "CEME", "CHBE", "CHEM", "CIRS",
            "DMP", "EOS", "ESB", "FNH", "FORW", "FRDM", "FSC", "GEOG", "HEBB", "HENN", "IBLC", "IONA",
            "IRC", "LASR", "LIFE", "LSK", "MATH", "MATX", "MCLD", "MCML", "ORCH", "OSB1", "PHRM", "PCN",
            "SCRF", "SOWK", "SPPH", "SWNG", "UCEN", "WESB"};

    private final String[] allBuildingNames = allBuildingCodes;
    private final ArrayList<String> informalLearningSpaceBuildingCodes = new ArrayList<>();
    private final ArrayList<String> informalLearningSpaceBuildingNames = new ArrayList<>();
    private final ArrayList<String> lectureHallBuildingCodes = new ArrayList<>();
    private final ArrayList<String> lectureHallBuildingNames = new ArrayList<>();
    private final ArrayList<String> studyRoomBuildingCodes = new ArrayList<>();

    private final ArrayList<String> studyRoomBuildingNames = new ArrayList<>();
    private ArrayList<String> showingBuildingCodes;
    private ArrayList<String> showingBuildingNames;
    private ArrayList<String> showingBuildingList;
    private String requestBuildingType = "";
    private TextView guideText;
    private BuildingsRecyclerViewAdapter adapter;
    private Button informalLearningSpaceButton;
    private Button lectureHallButton;
    private Button studyRoomButton;
    private RecyclerView recyclerView;
    private long lastToastTime;

//    private boolean showFullName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initBuildingData();

        SearchView searchView = findViewById(R.id.building_searchView);
        searchView.clearFocus();
        searchView.setQueryHint("Search Study Spaces");
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

        adapter = new BuildingsRecyclerViewAdapter(SearchActivity.this,SearchActivity.this);
        recyclerView = findViewById(R.id.building_recyclerView);

        guideText = findViewById(R.id.guide_text);
        // change the request setting when clicking the button
        informalLearningSpaceButton = findViewById(R.id.ils_button);
        lectureHallButton = findViewById(R.id.lecture_halls_button);
        studyRoomButton = findViewById(R.id.study_rooms_button);

        setButtonListener(informalLearningSpaceButton, "ils");
        setButtonListener(lectureHallButton, "lecturehalls");
        setButtonListener(studyRoomButton, "studyrooms");
    }
    @Override
    public void onItemClick(int position) {
        Intent dynamiBuildingIntent = new Intent(SearchActivity.this, DynamicBuildingActivity.class);

        switch (requestBuildingType) {
            case "ils":
                dynamiBuildingIntent.putExtra("type", Constant.TYPE_ILS);
                break;
            case "studyrooms":
                dynamiBuildingIntent.putExtra("type", Constant.TYPE_STUDY_ROOM);
                break;
            case "lecturehalls":
                dynamiBuildingIntent.putExtra("type", Constant.TYPE_LECTURE_HALL);
                break;
            default:
                Log.e(TAG, "Error: invalid BuildingType in the database");
                break;
        }

        dynamiBuildingIntent.putExtra("buildingName", adapter.getBuildingNames().get(position));
        dynamiBuildingIntent.putExtra("buildingCode", adapter.getBuildingNames().get(position));

        startActivity(dynamiBuildingIntent);
    }
    private void filterList(String text) {
        ArrayList<String> filteredList = new ArrayList<>();
        for (String building : showingBuildingList) {
            if (building.toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(building);
            }
        }
        if (filteredList.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            // avoid showing text repeatedly
            if (currentTime - lastToastTime >= Constant.TOAST_MIN_DURATION_MILLI_SECOND) {
                Toast.makeText(this, "No such building", Toast.LENGTH_SHORT).show();
                lastToastTime = System.currentTimeMillis();
            }
        } else {
            adapter.setFilterList(filteredList);
        }
    }
    private void setButtonListener(Button button, String spaceType) {
        button.setOnClickListener(view -> {
            Log.d(TAG, "Getting locations of " + spaceType +  " buildings");
            guideText.setText("");
            requestBuildingType = spaceType;
            runOnUiThread(() -> {
                // decide which list to show
                switch (spaceType) {
                    case "":
                        showingBuildingCodes = new ArrayList<>();
                        showingBuildingNames = new ArrayList<>();

                        informalLearningSpaceButton.setBackgroundResource(R.drawable.bottom_button);
                        lectureHallButton.setBackgroundResource(R.drawable.top_button);
                        studyRoomButton.setBackgroundResource(R.drawable.top_button);
                        break;
                    case "ils":
                        showingBuildingCodes = informalLearningSpaceBuildingCodes;
                        showingBuildingNames = informalLearningSpaceBuildingNames;
                        // setting button background
                        informalLearningSpaceButton.setBackgroundResource(R.drawable.bottom_button);
                        lectureHallButton.setBackgroundResource(R.drawable.top_button);
                        studyRoomButton.setBackgroundResource(R.drawable.top_button);
                        break;
                    case "lecturehalls":
                        showingBuildingCodes = lectureHallBuildingCodes;
                        showingBuildingNames = lectureHallBuildingNames;
                        // setting button background
                        informalLearningSpaceButton.setBackgroundResource(R.drawable.top_button);
                        lectureHallButton.setBackgroundResource(R.drawable.bottom_button);
                        studyRoomButton.setBackgroundResource(R.drawable.top_button);
                        break;
                    case "studyrooms":
                        showingBuildingCodes = studyRoomBuildingCodes;
                        showingBuildingNames = studyRoomBuildingNames;
                        // setting button background
                        informalLearningSpaceButton.setBackgroundResource(R.drawable.top_button);
                        lectureHallButton.setBackgroundResource(R.drawable.top_button);
                        studyRoomButton.setBackgroundResource(R.drawable.bottom_button);
                        break;
                    default:
                        showingBuildingCodes = new ArrayList<>(Arrays.asList(allBuildingCodes));
                        showingBuildingCodes = new ArrayList<>(Arrays.asList(allBuildingNames));
                        break;
                }

                showingBuildingList = showingBuildingCodes;

                adapter.setBuildingNames(showingBuildingList);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
            });
        });
    }
    private void initBuildingData() {
        initStudySpaceType("ils");
        initStudySpaceType("lecturehalls");
        initStudySpaceType("studyrooms");
        Log.d(TAG, "in init BuildingData Finish" );
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
                        runOnUiThread(() -> {
                            try {
                                initData(spaceType, buildings);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error setting locations for " + spaceType + " Buildings");
                            }
                        });
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "Get response not successful from server: ");
                }
            }
        }));
    }
    private void initData (String spaceType, String data) throws JSONException{

        JSONArray jsonArray = new JSONArray(data);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String srBuildingName = object.getString("building_name");
            String buildingCode = object.getString("building_code");

            switch (spaceType){
                case "ils":
                    informalLearningSpaceBuildingCodes.add(buildingCode);
                    informalLearningSpaceBuildingNames.add(srBuildingName);
                    break;
                case "studyrooms":
                    studyRoomBuildingCodes.add(buildingCode);
                    studyRoomBuildingNames.add(srBuildingName);
                    break;
                case "lecturehalls":
                    lectureHallBuildingCodes.add(buildingCode);
                    lectureHallBuildingNames.add(srBuildingName);
                    break;
                default:
                    Log.e(TAG, "Error: invalid input for initData");
                    break;
            }
        }
        // showing study rooms by default
        if (Objects.equals(spaceType, Constant.DEFAULT_STUDY_SPACE_TYPE)) {
            runOnUiThread(() -> {
                showingBuildingList = studyRoomBuildingCodes;

                adapter.setBuildingNames(showingBuildingList);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
            });
        }
    }
}