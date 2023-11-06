package com.javajedis.bookit;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.javajedis.bookit.model.BookingsModel;
import com.javajedis.bookit.recyclerView.adapter.Bookings_RecyclerViewAdapter;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;
import com.javajedis.bookit.util.BackNavigation;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// comparing dates: https://chat.openai.com/share/f6cabbea-7068-4c51-8810-5ab58da8feff

public class BookingsActivity extends AppCompatActivity implements RecyclerViewInterface {

    private final String TAG = "BookingsActivity";

    ArrayList<BookingsModel> bookingsModels = new ArrayList<>();

    List<String> bookingIDs = new ArrayList<>();

    private final Map<String, Map<String, String>> bookingsDictionary = new HashMap<>();

    private double lat;
    private double lon;
    private boolean locationPermissionGranted = false;

    String currentID;
//    private boolean isMapsRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookings);

        OnBackPressedCallback callback = BackNavigation.backToMain(this);
        this.getOnBackPressedDispatcher().addCallback(this, callback);

        getBookings();
    }

    private void getBookings() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/user/bookings";
        System.out.println(url);
        Log.d("BookingsActivity", url);
//        String date = getIntent().getStringExtra("date");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        assert account != null;
        url += "?token=" + account.getIdToken();
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
                        System.out.println(bookingArray.length());

                        for (int i = 0; i < bookingArray.length(); i++) {
                            JSONObject bookingInfo = bookingArray.getJSONObject(i);

                            String _id = bookingInfo.optString("_id");
                            String name = bookingInfo.optString("roomCode");
                            String date = bookingInfo.optString("date");
                            String startTime = bookingInfo.optString("startTime");
                            String endTime = bookingInfo.optString("endTime");

                            boolean confirmed;
                            try {
                                confirmed = bookingInfo.optBoolean("confirmed");
                            } catch (Exception e) {
                                confirmed = false;
                            }

                            Map<String, String> bookingDetails = new HashMap<>();
                            bookingDetails.put("name", name);
                            bookingDetails.put("date", date);
                            bookingDetails.put("startTime", startTime);
                            bookingDetails.put("endTime", endTime);
                            bookingDetails.put("confirmed", Boolean.toString(confirmed));

                            bookingsDictionary.put(_id, bookingDetails);
                        }

                        System.out.println(bookingsDictionary.size());

                        // update list of rooms
                        bookingIDs.clear();
                        bookingIDs.addAll(bookingsDictionary.keySet());

                        System.out.println(bookingIDs.size());

                        setUpBookingModels();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recyclerView = findViewById(R.id.bookings_recyclerView);
                                Bookings_RecyclerViewAdapter adapter = new Bookings_RecyclerViewAdapter(BookingsActivity.this, bookingsModels, BookingsActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(BookingsActivity.this));
                                System.out.println(adapter.getItemCount());
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
                    System.out.println(response.body().string());
                }
            }
        }));
    }

    private void setUpBookingModels() {
        String currentTime = getCurrentTime();

        int image = R.drawable.calendar_bookings;

        for (String _id : bookingIDs) {
            int startTimeInt = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(bookingsDictionary.get(_id)).get("startTime")));
            int endTimeInt = Integer.parseInt(Objects.requireNonNull(Objects.requireNonNull(bookingsDictionary.get(_id)).get("endTime")));
            int currentTimeInt = Integer.parseInt(currentTime);

            String actionStatus;
            String name = Objects.requireNonNull(bookingsDictionary.get(_id)).get("name");
            String dateBooking = Objects.requireNonNull(bookingsDictionary.get(_id)).get("date");
            System.out.println(dateBooking);

            if (isCurrentDateBefore(dateBooking)) {
                actionStatus = "click to cancel";
            } else if (isCurrentDateEqual(dateBooking)) {
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

            String confirmed = Objects.requireNonNull(bookingsDictionary.get(_id)).get("confirmed");

            assert confirmed != null;
            if (confirmed.equals("true")) {
                actionStatus = "confirmed";
            }

            String timeslotCombined = Objects.requireNonNull(bookingsDictionary.get(_id)).get("startTime") + "-" + Objects.requireNonNull(bookingsDictionary.get(_id)).get("endTime");

            bookingsModels.add(new BookingsModel(name, image, timeslotCombined, dateBooking, actionStatus));
            System.out.println(bookingsModels.size());
        }
    }

    // ChatGPT Usage: Yes
    public static boolean isCurrentDateBefore(String inputDateStr) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = new Date();
        try {
            Date inputDate = dateFormat.parse(inputDateStr);
            return !currentDate.after(inputDate);
        } catch (ParseException e) {
            System.err.println("Error parsing the input date");
            return false;
        }
    }

    // ChatGPT Usage: Yes
    public static boolean isCurrentDateEqual(String inputDateStr) {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date currentDate = new Date();
        try {
            Date inputDate = dateFormat.parse(inputDateStr);
            return dateFormat.format(currentDate).equals(inputDateStr);
        } catch (ParseException e) {
            System.err.println("Error parsing the input date");
            return false;
        }
    }

    public static String getCurrentTime() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        return currentTime.format(formatter);
    }

    @Override
    public void onItemClick(int position) {

        currentID = bookingIDs.get(position);
        Map<String, String> bookingInfo = bookingsDictionary.get(currentID);

        BookingsModel bm = bookingsModels.get(position);

//        assert bookingInfo != null;
//        if (Objects.equals(bookingInfo.get("action"), "expired")) {
//            Toast.makeText(BookingsActivity.this, "Your booking has expired!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (Objects.equals(bookingInfo.get("action"), "click to cancel")) {
//            System.out.println("trying to cancel");
//            cancelBooking(id);
//        }
//
//        if (Objects.equals(bookingInfo.get("action"), "click to confirm")) {
//            confirmBooking();
//        }

        assert bookingInfo != null;
        if (bm.getAction().equals("expired")) {
            Toast.makeText(BookingsActivity.this, "Your booking has expired!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bm.getAction().equals("confirmed")) {
            Toast.makeText(BookingsActivity.this, "You have already confirmed your booking!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bm.getAction().equals("click to cancel")) {
            System.out.println("trying to cancel");
            cancelBooking();
        }

        if (bm.getAction().equals("click to confirm")) {
            if (!locationPermissionGranted) {
                ActivityCompat.requestPermissions(BookingsActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }  else {
                getLocationInfo();
            }
//            confirmBooking();
        }

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

                    confirmBooking();
                    locationManager.removeUpdates(this);
                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//        isMapsRunning = false;
//    }

    private void confirmBooking() {
        OkHttpClient client = new OkHttpClient();

        String postUrl = "https://bookit.henrydhc.me/user/bookings/" + currentID;

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        try {
            JSONObject jsonRequest = new JSONObject();
            try {
//                jsonRequest.put("email", account.getEmail());
                assert account != null;
                jsonRequest.put("token", account.getIdToken());
                jsonRequest.put("lat", lat);
                jsonRequest.put("lon", lon);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

            Request request = new Request.Builder()
                    .url(postUrl)
                    .put(requestBody)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "PUT request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        assert response.body() != null;
                        String responseBody = response.body().string();
                        Log.d(TAG, responseBody);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BookingsActivity.this, "Your booking has been confirmed!", Toast.LENGTH_SHORT).show();
                                Objects.requireNonNull(bookingsDictionary.get(currentID)). put("confirmed", "true");
                                currentID = "";
                                bookingsModels.clear();
                                setUpBookingModels();
                                RecyclerView recyclerView = findViewById(R.id.bookings_recyclerView);
                                Bookings_RecyclerViewAdapter adapter = new Bookings_RecyclerViewAdapter(BookingsActivity.this, bookingsModels, BookingsActivity.this);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(BookingsActivity.this));
                                System.out.println(adapter.getItemCount());
                            }
                        });
                    } else {
                        Log.e(TAG, "PUT request failed with code: " + response.code());
                        System.out.println(response.body().string());
                        if (response.code() == 400) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BookingsActivity.this, "You are too far away from your booking!", Toast.LENGTH_SHORT).show();
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

    private void cancelBooking() {
        OkHttpClient client = new OkHttpClient();
        String url = "https://bookit.henrydhc.me/user/bookings/" + currentID;
        System.out.println(url);
        Log.d("BookingsActivity", url);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        assert account != null;
        url += "?token=" + account.getIdToken();
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue((new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "DELETE request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("yo");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BookingsActivity.this, "Your booking has been cancelled!", Toast.LENGTH_SHORT).show();
                            bookingIDs.remove(currentID);
                            bookingsDictionary.remove(currentID);
                            currentID = "";
                            bookingsModels.clear();
                            setUpBookingModels();
                            RecyclerView recyclerView = findViewById(R.id.bookings_recyclerView);
                            Bookings_RecyclerViewAdapter adapter = new Bookings_RecyclerViewAdapter(BookingsActivity.this, bookingsModels, BookingsActivity.this);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(BookingsActivity.this));
                            System.out.println(adapter.getItemCount());
                        }
                    });
                } else {
                    Log.e(TAG, "No response.");
                    assert response.body() != null;
                    System.out.println(response.body().string());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BookingsActivity.this, "Unable to cancel booking. Oops!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }));
    }
}