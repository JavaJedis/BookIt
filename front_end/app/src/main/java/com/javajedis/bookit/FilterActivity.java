package com.javajedis.bookit;

import static okhttp3.MediaType.parse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.javajedis.bookit.model.RoomModel;
import com.javajedis.bookit.recyclerView.adapter.RN_RecyclerViewAdapter;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FilterActivity extends AppCompatActivity  implements RecyclerViewInterface {

    private Button startTimeButton;

    private boolean locationPermissionGranted = false;

    private int hour, min;
    private String day;
    private String duration;
    private String startTime;

    private Button filterButton;
    private Button dayButton;
    private String date;

    List<String> roomNames;
    ArrayList<RoomModel> roomModels = new ArrayList<>();
    private Map<String, Map<String, String>> roomDictionary = new HashMap<>();

    private double lat;
    private double lon;
    TextView filterInfoTextView;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        roomNames = new ArrayList<>();

        dayButton = findViewById(R.id.day_button);
        date = getIntent().getStringExtra("date");

        if (date != null) {
            dayButton.setText(date);
        }

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
                if (!locationPermissionGranted) {
                    ActivityCompat.requestPermissions(FilterActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                } else {
                    getLocationInfo();
                }
//                else {
//                    if (date == null || startTime == null || duration == null) {
//                        Log.d("FilterActivity", "Please select an appropriate date, start time, and duration");
//                    } else {
//                        System.out.println(date);
//                        System.out.println(startTime);
//                        System.out.println(duration);
//
//                        String militaryTime = "";
//                        String endTime = "";
//                        try {
//                            @SuppressLint("SimpleDateFormat") SimpleDateFormat regClock = new SimpleDateFormat("hh:mm a");
//                            Date convert = regClock.parse(startTime);
//                            @SuppressLint("SimpleDateFormat") SimpleDateFormat militaryClock = new SimpleDateFormat("HH:mm");
//                            assert convert != null;
//                            militaryTime = militaryClock.format(convert);
//
//                            endTime = addHoursToTime(militaryTime, duration);
//                        } catch (ParseException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        getStudyRooms(date, militaryTime, endTime);
//                    }
//                }
            }
        });

        filterInfoTextView = findViewById(R.id.filter_info_textView);
        filterInfoTextView.setText(Constant.FILTER_GUILD_TEXT);

        recyclerView = findViewById(R.id.study_rooms_filter_recycler_view);
    }

    private void getLocationInfo() {
        if (locationPermissionGranted) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    checkFilterStatus();
                    locationManager.removeUpdates(this);
                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    private void checkFilterStatus() {

        if (date == null || startTime == null || duration == null) {
            Log.d("FilterActivity", "Please select an appropriate date, start time, and duration");
        } else {
//            System.out.println(date);
//            System.out.println(startTime);
//            System.out.println(duration);

            String militaryTime = "";
//            String endTime = "";
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat regClock = new SimpleDateFormat("hh:mm a");
                Date convert = regClock.parse(startTime);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat militaryClock = new SimpleDateFormat("HH:mm");
                assert convert != null;
                militaryTime = militaryClock.format(convert);

//                endTime = addHoursToTime(militaryTime, duration);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            double durationDouble = 0.5;
            if (duration.equals("1 hour")) {
                durationDouble = 1;
            } else if (duration.equals("1.5 hours")) {
                durationDouble = 1.5;
            } else if (duration.equals("2 hours")) {
                durationDouble = 2;
            } else if (duration.equals("2.5 hours")) {
                durationDouble = 2.5;
            } else if (duration.equals("3 hours")) {
                durationDouble = 3;
            }

            @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM yyyy");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

            String formattedDate = "";
            try {
                Date date = inputFormat.parse(this.date);
                assert date != null;
                formattedDate = outputFormat.format(date);
//                System.out.println(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            militaryTime = militaryTime.replace(":", "");

            System.out.println(formattedDate);
            System.out.println(militaryTime);
            System.out.println(durationDouble);

            getStudyRooms(formattedDate, militaryTime, durationDouble);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                getLocationInfo();
            } else {
                locationPermissionGranted = false;
            }
        }
    }

    public static String addHoursToTime(String inputTime, String hoursToAdd) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = sdf.parse(inputTime);

            int hours = 0;
            int minutes = 0;

            // Parse the hours to add
            String[] parts = hoursToAdd.split(" ");
            for (int i = 0; i < parts.length; i += 2) {
                int value = Integer.parseInt(parts[i]);
                if (parts[i + 1].equals("hour") || parts[i + 1].equals("hours")) {
                    hours += value;
                } else if (parts[i + 1].equals("minute") || parts[i + 1].equals("minutes")) {
                    minutes += value;
                }
            }

            // Create a Calendar object and set it to the input time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Add the specified hours and minutes
            calendar.add(Calendar.HOUR, hours);
            calendar.add(Calendar.MINUTE, minutes);

            // Format the result as "HH:mm" and return
            String resultTime = sdf.format(calendar.getTime());

            return resultTime;
        } catch (Exception e) {
            // Handle any parsing errors here
            e.printStackTrace();
            return null;
        }
    }

    private void setUpRoomModels() {
        int image = R.drawable.office;

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
//        final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select a Day")
//                .setItems(days, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        String selectedDay = days[which];
//                        // Update the day_button text
//                        Button dayButton = findViewById(R.id.day_button);
//                        dayButton.setText(selectedDay);
//                        day = selectedDay;
//                    }
//                })
//                .show();
        Intent calendarIntent = new Intent(FilterActivity.this, CalendarActivity.class);
        calendarIntent.putExtra("fromFilter", true);
        startActivity(calendarIntent);
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

    private void getStudyRooms(String date, String startTime, double durationDouble) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filterInfoTextView.setText(Constant.FILTER_LOAD_TEXT);
                filterInfoTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
        OkHttpClient client = new OkHttpClient();

        String getUrl = "https://bookit.henrydhc.me/filter";
        Log.d("FilterActivity", getUrl);

        System.out.println(lat);
        System.out.println(lon);

        getUrl += "?startTime=" + startTime;
        getUrl += "&duration=" + durationDouble;
        getUrl += "&day=" + date;
        getUrl += "&lat=" + lat;
        getUrl += "&lon=" + lon;

        Request request = new Request.Builder()
                .url(getUrl)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                filterInfoTextView.setText(Constant.FILTER_ERROR_TEXT);
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (roomsArray.length() == 0) {
                                    filterInfoTextView.setText(Constant.FILTER_NO_MATCHING_TEXT);
                                } else {
                                    filterInfoTextView.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        for (int i = 0; i < roomsArray.length(); i++) {
                            JSONObject roomInfo = roomsArray.getJSONObject(i);

                            String number = roomInfo.optString("_id");
                            String name = roomInfo.optString("building_name");
                            String code = roomInfo.optString("building_code");
//                            String number = roomInfo.optString("room_no");
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
                            roomNames.add(key);
                            roomDictionary.put(key, roomDetails);
                        }

                        // update list of rooms
//                        roomNames.clear();
//                        roomNames.addAll(roomDictionary.keySet());

                        setUpRoomModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RN_RecyclerViewAdapter adapter = new RN_RecyclerViewAdapter(FilterActivity.this, roomModels, FilterActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(FilterActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e("FilterActivity", "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("FilterActivity", "Response not successful");
                    assert response.body() != null;
                    filterInfoTextView.setText(Constant.FILTER_ERROR_TEXT);
                    System.out.println(response.body().toString());
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
        roomInfoIntent.putExtra("type", "study");
        startActivity(roomInfoIntent);
    }
}