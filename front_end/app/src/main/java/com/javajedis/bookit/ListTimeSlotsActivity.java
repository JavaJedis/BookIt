package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.javajedis.bookit.model.TimeSlotsModel;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.TimeSlots_RecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// understanding date formatting: https://chat.openai.com/share/809762e7-72d5-49c2-b2aa-c7b9234c3607

public class ListTimeSlotsActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "ListTimeSlotsActivity";

    ArrayList<TimeSlotsModel> timeSlotModels = new ArrayList<>();

    List<String> timeSlots = new ArrayList<>();

    private String outputDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_time_slots);

        getTimeSlots();
    }

    // ChatGPT Usage: Partial
    private void getTimeSlots() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/studyrooms/" + getIntent().getStringExtra("buildingCode") + "/" + getIntent().getStringExtra("roomNumber") + "/" + "slots";
        System.out.println(url);
        Log.d("ListTimeActivity", url);
        String date = getIntent().getStringExtra("date");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        outputDate = "";
        try {
            // Parse the input date string into a Date object
            assert date != null;
            Date convert = inputFormat.parse(date);
            assert convert != null;
            outputDate = outputFormat.format(convert);

            System.out.println("Output Date: " + outputDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // convert October to 10
        outputDate = outputDate.replace("/", "-");

        url += "?date=" + outputDate;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue((new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e(TAG, "GET request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    System.out.println("yo");
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        System.out.println(jsonResponse);
                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        String binarySlots = responseObject.getString("data");
                        System.out.println(binarySlots);

                        setUpTimeSlotModels(binarySlots);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.timeslots_recycler_view);
                                TimeSlots_RecyclerViewAdapter adapter = new TimeSlots_RecyclerViewAdapter(ListTimeSlotsActivity.this, timeSlotModels, ListTimeSlotsActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(ListTimeSlotsActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e(TAG, "No response.");
                    assert response.body() != null;
                    System.out.println(response.body());
                }
            }
        }));
    }

    private void setUpTimeSlotModels(String binarySlots) {

        timeSlots = generateTimeIntervals();
        System.out.println(timeSlots);

        int image = R.drawable.clock;

        for (int i = 0; i < binarySlots.length(); i++) {
            if (binarySlots.charAt(i) == '0') {
                timeSlotModels.add(new TimeSlotsModel(timeSlots.get(i), image, "book now"));
            }
            if (binarySlots.charAt(i) == '1') {
                timeSlotModels.add(new TimeSlotsModel(timeSlots.get(i), image, "get on wait-list"));
            }
        }
        System.out.println(timeSlotModels.size());
    }

    // ChatGPT Usage: Partial
    public static List<String> generateTimeIntervals() {
        List<String> intervals = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                @SuppressLint("DefaultLocale") String start = String.format("%02d%02d", hour, minute);
                int endHour = hour;
                int endMinute = minute + 30;
                if (endMinute >= 60) {
                    endHour = (hour + 1) % 24;
                    endMinute -= 60;
                }
                @SuppressLint("DefaultLocale") String end = String.format("%02d%02d", endHour, endMinute);
                if (start.equals("2330") && end.equals("0000")) {
                    end = "2400";
                }
                intervals.add(start + "-" + end);
            }
        }

        return intervals;
    }

    @Override
    public void onItemClick(int position) {
        TimeSlotsModel selectedModel = timeSlotModels.get(position);
        String selectedTimeSlot = selectedModel.getTimeInterval();
        String selectedStatus = selectedModel.getStatus();

        String[] startEndTimes = selectedTimeSlot.split("-");
        String startTime = startEndTimes[0];
        String endTime = startEndTimes[1];

        if (endTime.equals("0000")) {
            endTime = "2400";
        }

        System.out.println(outputDate);
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(getIntent().getStringExtra("buildingCode"));
        System.out.println(getIntent().getStringExtra("roomNumber"));

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        assert account != null;
        System.out.println(account.getEmail());

        OkHttpClient client = new OkHttpClient();

        String postUrl = "https://bookit.henrydhc.me/studyroom/";

        if (selectedStatus.equals("get on wait-list")) {
            postUrl += "waitlist";
        } else {
            postUrl += "book";
        }

        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("date", outputDate);
                jsonRequest.put("startTime", startTime);
                jsonRequest.put("endTime", endTime);
                jsonRequest.put("buildingCode", getIntent().getStringExtra("buildingCode"));
                jsonRequest.put("roomNo", getIntent().getStringExtra("roomNumber"));
                jsonRequest.put("token", account.getIdToken());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

            Request request = new Request.Builder()
                    .url(postUrl)
                    .post(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "POST request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.d(TAG, responseBody);
                        String message = "";
                        try {
                            JSONObject responseObject = new JSONObject(responseBody);
                            message = responseObject.getString("data");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        if (message.equals("Successfully added to the waitlist")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ListTimeSlotsActivity.this, "You have been added to the wait-list!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Intent bookingsIntent = new Intent(ListTimeSlotsActivity.this, BookingsActivity.class);
                            startActivity(bookingsIntent);
                        }
                    } else {
                        Log.e(TAG, "POST request failed with code: " + response.code());
                        assert response.body() != null;
                        System.out.println(response.body().string());
                        if (response.code() == 400) {
                            runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ListTimeSlotsActivity.this, "You have already booked this room!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}