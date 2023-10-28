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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingsActivity extends AppCompatActivity implements RecyclerViewInterface {

    ArrayList<BookingsModel> bookingsModels = new ArrayList<>();

    List<String> bookingRoomNames = new ArrayList<>();

    private Map<String, Map<String, String>> bookingsDictionary = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        getBookings();
    }

    private void getBookings() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/user/bookings";
        System.out.println(url);
        Log.d("BookingsActivity", url);
        String date = getIntent().getStringExtra("date");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        url += "?token=" + account;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue((new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("BookingsActivity", "GET request failed: " + e.getMessage());
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
                        JSONArray bookingArray = responseObject.getJSONArray("data");
                        System.out.println(bookingArray);

                        for (int i = 0; i < bookingArray.length(); i++) {
                            JSONObject bookingInfo = bookingArray.getJSONObject(i);

                            String name = bookingInfo.optString("roomCode");
                            String date = bookingInfo.optString("date");
                            String startTime = bookingInfo.optString("startTime");
                            String endTime = bookingInfo.optString("endTime");

                            Map<String, String> bookingDetails = new HashMap<>();
                            bookingDetails.put("date", date);
                            bookingDetails.put("startTime", startTime);
                            bookingDetails.put("endTime", endTime);

                            bookingsDictionary.put(name, bookingDetails);
                        }

                        // update list of rooms
                        bookingRoomNames.clear();
                        bookingRoomNames.addAll(bookingsDictionary.keySet());

                        setUpBookingModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.bookings_recyclerView);
                                Bookings_RecyclerViewAdapter adapter = new Bookings_RecyclerViewAdapter(BookingsActivity.this, bookingsModels, BookingsActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(BookingsActivity.this));
                            }
                        });
                    } catch (IOException e) {
                        Log.e("BookingsActivity", "Error reading response: " + e.getMessage());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("BookingsActivity", "No response.");
                    assert response.body() != null;
                    System.out.println(response.body().string());
                }
            }
        }));
    }

    private void setUpBookingModels() {
        String currentTime = getCurrentTime();

        int image = R.drawable.calendar_bookings;

        for (String name : bookingRoomNames) {
            int startTimeInt = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(bookingsDictionary.get(name)).get("startTime")));
            int endTimeInt = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(bookingsDictionary.get(name)).get("endTime")));
            int currentTimeInt = Integer.parseInt(currentTime);

            String actionStatus = "";
            String dateBooking = Objects.requireNonNull(bookingsDictionary.get(name)).get("dateTime");
            if (isCurrentDateBeforeOrEqual(dateBooking)) {
                if (startTimeInt > currentTimeInt) {
                    actionStatus = "click to cancel";
                } else if (currentTimeInt < endTimeInt) {
                    actionStatus = "click to confirm";
                } else {
                    actionStatus = "expired";
                }
            } else {
                actionStatus = "expired";
            }

            String timeslotCombined = Objects.requireNonNull(bookingsDictionary.get(name)).get("startTime") + "-" + Objects.requireNonNull(bookingsDictionary.get(name)).get("endTime");

            bookingsModels.add(new BookingsModel(name, image, timeslotCombined, dateBooking, actionStatus));
        }
    }

    public static boolean isCurrentDateBeforeOrEqual(String inputDateStr) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = new Date();
        try {
            Date inputDate = dateFormat.parse(inputDateStr);
            return !currentDate.after(inputDate) || dateFormat.format(currentDate).equals(inputDateStr);
        } catch (ParseException e) {
            System.err.println("Error parsing the input date");
            return false;
        }
    }

    public static String getCurrentTime() {
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Create a custom time format (HHmm)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");

        // Format the current time as a string in the desired format

        return currentTime.format(formatter);
    }

    @Override
    public void onItemClick(int position) {

    }
}