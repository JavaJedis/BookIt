package com.javajedis.bookit;

import static okhttp3.MediaType.parse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FilterActivity extends AppCompatActivity  implements RecyclerViewInterface {

    private Button startTimeButton;

    private int hour, min;
    private String day;
    private String duration;
    private String startTime;

    private Button filterButton;

    List<String> roomNames = new ArrayList<>();
    ArrayList<RoomModel> roomModels = new ArrayList<>();
    private Map<String, Map<String, String>> roomDictionary = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        startTimeButton = findViewById(R.id.start_time_button);
        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTimePicker(FilterActivity.this.getCurrentFocus());
            }
        });

        filterButton = findViewById(R.id.filter_bottom_button);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStudyRooms();
            }
        });
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

    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                int roundedMinute = (minute < 30) ? 0 : 30;

                hour = hourOfDay;
                min = roundedMinute;
                startTimeButton.setText(formatTime(hour, min));
            }
        };

        int style = AlertDialog.THEME_HOLO_DARK;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour, min, false);

        timePickerDialog.setTitle("select time");
        timePickerDialog.show();
    }

    private String formatTime(int hour, int min) {
        String amPm;
        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        } else {
            amPm = "AM";
            if (hour == 0) {
                hour = 12;
            }
        }

        startTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, min, amPm);
        return startTime;
    }


    // pop-up menu for days of the week and duration: https://chat.openai.com/share/021b3349-433a-4ded-bb34-3d2e23c61bf8
    public void showDayMenu(View view) {
        final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Day")
                .setItems(days, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedDay = days[which];
                        // Update the day_button text
                        Button dayButton = findViewById(R.id.day_button);
                        dayButton.setText(selectedDay);
                        day = selectedDay;
                    }
                })
                .show();
    }

    public void showDurationMenu(View view) {
        final String[] durations = {"30 minutes", "1 hour", "1.5 hours", "2 hours", "2.5 hours", "3 hours"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Duration")
                .setItems(durations, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedDuration = durations[which];
                        // Update the hours_button text
                        Button hoursButton = findViewById(R.id.hours_button);
                        hoursButton.setText(selectedDuration);
                        duration = selectedDuration;
                    }
                })
                .show();
    }

    private void getStudyRooms() {
        OkHttpClient client = new OkHttpClient();

        String getUrl = "https://bookit.henrydhc.me/filter";
        JSONObject json = new JSONObject();
        try {
            json.put("day", day);
            json.put("duration", duration);
            json.put("startTime", startTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(parse("application/json"), json.toString());

        Request request = new Request.Builder()
                .url(getUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("FilterActivity", "GET request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        System.out.println(jsonResponse);
//                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
//                        JSONArray roomsArray = new JSONArray(jsonResponse);
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
                                RN_RecyclerViewAdapter adapter = new RN_RecyclerViewAdapter(FilterActivity.this, roomModels, FilterActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(FilterActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e("ExploreActivity", "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("FilterActivity", "Request was not successful. Response code: " + response.code());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Intent roomInfoIntent = new Intent(FilterActivity.this, DynamicRoomActivity.class);

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