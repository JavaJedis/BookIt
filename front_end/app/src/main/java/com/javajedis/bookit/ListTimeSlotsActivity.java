package com.javajedis.bookit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.javajedis.bookit.model.TimeSlotsModel;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.recyclerView.adapter.TimeSlots_RecyclerViewAdapter;

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

public class ListTimeSlotsActivity extends AppCompatActivity implements RecyclerViewInterface {

    ArrayList<TimeSlotsModel> timeSlotModels = new ArrayList<>();

    List<String> timeSlots = new ArrayList<>();

    private String outputDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_time_slots);

        getTimeSlots();
    }

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
            Date convert = inputFormat.parse(date);
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
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("ListTimeActivity", "GET request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("yo");
                    try {
                        assert response.body() != null;
                        String jsonResponse = response.body().string();
                        System.out.println(jsonResponse);
//                        // parse
                        JSONObject responseObject = new JSONObject(jsonResponse);
//                        JSONArray roomsArray = new JSONArray(jsonResponse);
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
                        Log.e("ListTimeSlotsActivity", "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("ListTimeSlotsActivity", "No response.");
                    System.out.println(response.body().toString());
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
                timeSlotModels.add(new TimeSlotsModel(timeSlots.get(i), image, "get on waitlist"));
            }
        }
        System.out.println(timeSlotModels.size());
    }

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

        System.out.println(outputDate);
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(getIntent().getStringExtra("buildingCode"));
        System.out.println(getIntent().getStringExtra("roomNumber"));

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        System.out.println(account.getEmail());

        OkHttpClient client = new OkHttpClient();

        String postUrl = "https://bookit.henrydhc.me/studyroom/book";

        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("date", outputDate);
                jsonRequest.put("startTime", startTime);
                jsonRequest.put("endTime", endTime);
                jsonRequest.put("buildingCode", getIntent().getStringExtra("buildingCode"));
                jsonRequest.put("roomNo", getIntent().getStringExtra("roomNumber"));
                assert account != null;
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
                    Log.e("ListTimeSlotsActivity", "POST request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.d("ListTimeSlotsActivity", responseBody);
                    } else {
                        Log.e("ListTimeSlotsActivity", "POST request failed with code: " + response.code());
                        System.out.println(response.body().string());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}