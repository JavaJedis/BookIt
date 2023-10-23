package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    private String[] allBuildingCodes = {"AERL", "ALRD", "ANGU", "ANSO", "AUDX", "BIOL", "BUCH", "CEME", "CHBE", "CHEM", "CIRS",
            "DMP", "EOS", "ESB", "FNH", "FORW", "FRDM", "FSC", "GEOG", "HEBB", "HENN", "IBLC", "IONA",
            "IRC", "LASR", "LIFE", "LSK", "MATH", "MATX", "MCLD", "MCML", "ORCH", "OSB1", "PHRM", "PCN",
            "SCRF", "SOWK", "SPPH", "SWNG", "UCEN", "WESB"};

    private String[] allBuildingNames;
    private List<String> informalLearningSpaceBuildingCodes;
    private List<String> informalLearningSpaceBuildingNames;
    private List<String> lectureHallBuildingCodes;
    private List<String> lectureHallBuildingNames;
    private List<String> studyRoomBuildingCodes;
    private List<String> studyRoomBuildingNames;
    private String[] showingBuildingList;
    private String requestBuildingType = "";
    private final String DOMAIN = "https://bookit.henrydhc.me";
    ListView buildingList;
    ArrayAdapter<String> arrayAdapter;

    TextView guideText;

    MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // TODO: text position need to be fixed
        guideText = findViewById(R.id.deixis);
        String DEIXIS = "Please choose a learning space class by choosing one of the buttons below!";
        guideText.setText(DEIXIS);

        boolean init = initBuildingData();

        showingBuildingList = allBuildingCodes;

        buildingList = findViewById(R.id.building_list);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, showingBuildingList);
        buildingList.setAdapter(arrayAdapter);

        // change the request setting when clicking the button
        Button informalLearningSpaceButton = findViewById(R.id.ils_button);
        informalLearningSpaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SearchActivity", "Getting locations of ILS buildings");
                guideText.setText("");
                requestBuildingType = "ils";
                menuItem.setEnabled(true);
            }
        });

        Button lectureHallButton = findViewById(R.id.lecture_halls_button);
        lectureHallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SearchActivity", "Getting locations of lecture hall buildings");
                guideText.setText("");
                requestBuildingType = "lecturehalls";
                menuItem.setEnabled(true);
            }
        });

        Button studyRoomButton = findViewById(R.id.sutdy_rooms_button);
        studyRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SearchActivity", "Getting locations of study room buildings");
                guideText.setText("");
                requestBuildingType = "studyrooms";
                menuItem.setEnabled(true);
            }
        });
    }

    // from https://www.youtube.com/watch?v=M3UDh9mwBd8
    // TODO: option menu doesn't show on the screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        menuItem = menu.findItem(R.id.action_search);

        menuItem.setEnabled(false);
        SearchView searchView = (SearchView) menuItem.getActionView();
        if (searchView == null) {
            Log.e("SearchActivity", "cannot find menu item search view");
        } else {
            searchView.setQueryHint("Type here to Search");
        }

        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private boolean initBuildingData() {
        OkHttpClient client = new OkHttpClient();

        String ilsUrl = DOMAIN + "/ils" + "/building_all";
        String lhUrl = DOMAIN + "/lecturehalls" + "/building_all";
        String srUrl = DOMAIN + "/studyrooms" + "/building_all";

        Request ilsRequest = new Request.Builder()
                .url(ilsUrl)
                .get()
                .build();
        Request lhRequest = new Request.Builder()
                .url(lhUrl)
                .get()
                .build();
        Request srRequest = new Request.Builder()
                .url(srUrl)
                .get()
                .build();

        Log.d("SearchActivity", "in init BuildingData 1" );

        boolean[] ilsSuccess = {false};

        boolean[] lhSuccess = {false};

        boolean[] srSuccess = {false};

        // three requests to backend server to avoid repeat get request.
        client.newCall(ilsRequest).enqueue((new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("SearchActivity", "GET ils building request failed: " + e.getMessage());
                ilsSuccess[0] = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray data = responseObject.getJSONArray("data");
                        Log.d("SearchActivity", "in init BuildingData 2" );
                        // extract info
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            String ilsBuildingName = object.getString("building_name");
                            String buildingCode = object.getString("building_code");
                            informalLearningSpaceBuildingCodes.add(buildingCode);
                            informalLearningSpaceBuildingNames.add(ilsBuildingName);
                        }
                        ilsSuccess[0] = true;

                    } catch (IOException | JSONException e) {
                        Log.e("SearchActivity", "Error reading response: " + e.getMessage());
                    }
                }
            }
        }));
        client.newCall(lhRequest).enqueue((new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("SearchActivity", "GET ils building request failed: " + e.getMessage());
                lhSuccess[0] = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray data = responseObject.getJSONArray("data");
                        Log.d("SearchActivity", "in init BuildingData 2" );
                        // extract info
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            String lhBuildingName = object.getString("building_name");
                            String buildingCode = object.getString("building_code");
                            lectureHallBuildingCodes.add(buildingCode);
                            lectureHallBuildingNames.add(lhBuildingName);
                        }
                        lhSuccess[0] = true;
                    } catch (IOException | JSONException e) {
                        Log.e("SearchActivity", "Error reading response: " + e.getMessage());
                    }
                }
            }
        }));
        client.newCall(srRequest).enqueue((new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("SearchActivity", "GET ils building request failed: " + e.getMessage());
                srSuccess[0] = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        JSONArray data = responseObject.getJSONArray("data");
                        Log.d("SearchActivity", "in init BuildingData 2" );
                        // extract info
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            String srBuildingName = object.getString("building_name");
                            String buildingCode = object.getString("building_code");
                            studyRoomBuildingCodes.add(buildingCode);
                            studyRoomBuildingNames.add(srBuildingName);
                        }
                        srSuccess[0] = false;
                    } catch (IOException | JSONException e) {
                        Log.e("SearchActivity", "Error reading response: " + e.getMessage());
                    }
                }
            }
        }));

//        while (!(ilsSuccess[0] && lhSuccess[0] && srSuccess[0])) {
//            // TODO: need some other way to wait for the server response
//        }
        if (ilsSuccess[0] && lhSuccess[0] && srSuccess[0]) {
            Set<String> codeSet = new HashSet<String>();
            Set<String> nameSet = new HashSet<String>();

            codeSet.addAll(informalLearningSpaceBuildingCodes);
            codeSet.addAll(lectureHallBuildingCodes);
            codeSet.addAll(studyRoomBuildingCodes);

            nameSet.addAll(informalLearningSpaceBuildingNames);
            nameSet.addAll(lectureHallBuildingNames);
            nameSet.addAll(studyRoomBuildingNames);

            allBuildingCodes = codeSet.toArray(new String[0]);
            allBuildingNames = nameSet.toArray(new String[0]);
            Arrays.sort(allBuildingCodes);
            Arrays.sort(allBuildingNames);
        }
        Log.d("SearchActivity", "in init BuildingData Finish" );
        return ilsSuccess[0] && lhSuccess[0] && srSuccess[0];
    }

}