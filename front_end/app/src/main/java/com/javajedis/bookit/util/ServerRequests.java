package com.javajedis.bookit.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

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
    public static void requestAddAdmin(String adminEmail, String buildingCode, Context context, Intent onResponseRedirectIntent) {
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

                    requestAddBuildingToAdmin(adminEmail, buildingCode, context, onResponseRedirectIntent);
                }
            }
        });
    }

    public static void requestAddBuildingToAdmin(String adminEmail, String buildingCode, Context context, Intent onResponseRedirectIntent) {

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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("AssignBuildingAdmin", "POST request add building failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseBody = response.body().string();
                Log.d("AssignBuildingAdmin", responseBody);
                context.startActivity(onResponseRedirectIntent);
            }
        });
    }

    public static void requestDeleteBuildingFromAdmin(String adminEmail, String buildingCode, Context context, Intent onResponseRedirectIntent) {
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("DeleteAdminBuilding", "DELETE request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteAdminBuilding", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("DeleteAdminBuilding", "Delete request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestDeleteAdmin(String adminEmail, Context context, Intent onResponseRedirectIntent) {
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
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("DeleteAdmin", "Delete failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteAdmin", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("DeleteAdmin", "Delete request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestAddRoom(String buildingCode, String roomNumber, int capacity, ArrayList<String> features, Context context, Intent onResponseRedirectIntent) {
        String adminToken = Authentication.getCurrentAccountToken(context);

        String url = Constant.DOMAIN + "/studyrooms/" + buildingCode;
        JSONObject jsonRequest = new JSONObject();

        try {
            jsonRequest.put("token", adminToken);
            jsonRequest.put("room_no", roomNumber);
            jsonRequest.put("capacity", capacity);
            jsonRequest.put("features", features.toArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("AddRoom", "Add failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("AddRoom", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("AddRoom", "AddRoom request failed with code: " + response.code());
                }
            }
        });

    }

    public static void requestDeleteRoom(String buildingCode, String roomNumber, Context context, Intent onResponseRedirectIntent) {
        String adminToken = Authentication.getCurrentAccountToken(context);

        String url = Constant.DOMAIN + "/studyrooms/" + buildingCode + "/" + roomNumber + "?token=" + adminToken;

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("DeleteRoom", "Delete failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteRoom", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("DeleteRoom", "DeleteBuilding request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestAddBuilding(String buildingName, String buildingCode, String buildingAddress, String[] openTimes, String[] closeTimes, Context context, Intent onResponseRedirectIntent) {
        String superAdminToken = Authentication.getCurrentAccountToken(context);

        String url = Constant.DOMAIN + "/studyrooms/building";

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", superAdminToken);
            jsonRequest.put("building_code", buildingCode);
            jsonRequest.put("building_name", buildingName);
            jsonRequest.put("building_address", buildingAddress);
            jsonRequest.put("open_times", openTimes);
            jsonRequest.put("close_times", closeTimes);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonRequest.toString());

        Request request = new Request.Builder().url(url).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("AddBuilding", "Add failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("AddBuilding successful", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("AddBuilding", "AddBuilding request failed with code: " + response.code());
                }
            }
        });
    }

    public static void requestDeleteBuilding(String buildingCode, Context context, Intent onResponseRedirectIntent) {
        String superAdminToken = Authentication.getCurrentAccountToken(context);

        String url = Constant.DOMAIN + "/studyrooms/" + buildingCode + "?token=" + superAdminToken;

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("DeleteBuilding", "Delete failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    Log.d("DeleteBuilding", responseBody);
                    context.startActivity(onResponseRedirectIntent);
                } else {
                    Log.e("DeleteBuilding", "DeleteBuilding request failed with code: " + response.code());
                }
            }
        });
    }
}