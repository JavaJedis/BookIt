package com.javajedis.bookit.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerRequests {
    private static final OkHttpClient client = new OkHttpClient();
    public static void requestAddAdmin(String adminEmail, String buildingCode, Context context) {
        // first create a new admin
        String postUrl = Constant.DOMAIN + "/user/admin";

        String superAdminToken = Authentication.getCurrentAccountToken(context);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("email", adminEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request request = new Request.Builder().url(postUrl).post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("AssignBuildingAdmin", "POST request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("AssignBuildingAdmin", responseBody);

                    requestAddBuildingToAdmin(adminEmail, buildingCode, context);
                }
            }
        });
    }

    public static void requestAddBuildingToAdmin(String adminEmail, String buildingCode, Context context) {

        String superAdminToken = Authentication.getCurrentAccountToken(context);

        // now add building to this admin
        String url = Constant.DOMAIN + "/user/admin/" + adminEmail + "/buildings";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("building", buildingCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("AssignBuildingAdmin", "POST request add building failed: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String responseBody = response.body().string();
                Log.d("AssignBuildingAdmin", responseBody);
            }
        });
    }

    public static void requestDeleteBuildingFromAdmin(String adminEmail, String buildingCode, Context context) {
        String deleteUrl = Constant.DOMAIN + "/user/admin/" + adminEmail + "/buildings";

        String superAdminToken = Authentication.getCurrentAccountToken(context);

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("building", buildingCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request deleteRequest = new Request.Builder().url(deleteUrl).delete(requestBody).build();

        client.newCall(deleteRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("DeleteAdminBuilding", "DELETE request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteAdminBuilding", responseBody);
                } else {
                    Log.e("DeleteAdminBuilding", "Delete request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestDeleteAdmin(String adminEmail, Context context) {
        String superAdminToken = Authentication.getCurrentAccountToken(context);
        String deleteUrl = Constant.DOMAIN + "/user/admin";
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("email", adminEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request deleteRequest = new Request.Builder().url(deleteUrl).delete(requestBody).build();

        client.newCall(deleteRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("DeleteAdmin", "Delete failed: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteAdmin", responseBody);
                } else {
                    Log.e("DeleteAdmin", "Delete request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestAddRoom(String building, String roomNumber, int capacity, String features, Context context) {
        // TODO need endpoints to do this
        String superAdminToken = Authentication.getCurrentAccountToken(context);

        String url = Constant.DOMAIN + "/user/admin";
        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("building", building);
            jsonRequest.put("roomNumber", roomNumber);
            jsonRequest.put("capacity", capacity);
            jsonRequest.put("features", features);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request request = new Request.Builder().url(url).delete(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("AddRoom", "Add failed: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("AddRoom", responseBody);
                } else {
                    Log.e("AddRoom", "AddRoom request failed with code: " + response.code());
                }
            }
        });

    }

    public static void requestDeleteRoom(String building, String roomNumber, Context context) {
        // TODO need endpoints to do this
    }

    public static void requestAddBuilding(String buildingName, String buildingCode, String buildingAddress, Context context) {
        // TODO need endpoints to do this
    }

    public static void requestDeleteBuilding(String buildingCode, Context context) {
        // TODO need endpoints to do this
    }
}