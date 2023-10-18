package com.javajedis.bookit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class DynamicBuildingActivity extends AppCompatActivity {

    TextView buildingName;
    TextView buildingCode;
    TextView type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_building);

        buildingName = findViewById(R.id.building_name_textView);
        buildingName.setText(getIntent().getStringExtra("buildingName"));

        buildingCode = findViewById(R.id.building_code_textView);
        buildingCode.setText(getIntent().getStringExtra("buildingCode"));

        type = findViewById(R.id.type_textView);
        type.setText(getIntent().getStringExtra("type"));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("building_code", buildingCode.getText().toString());
            jsonObject.put("building_name", buildingName.getText().toString());
            jsonObject.put("type", type.getText().toString());
        } catch (JSONException e) {
            Log.d("DynamicBuildingActivity", "Error making JSON Object");
        }

        String jsonString = jsonObject.toString();
        System.out.println(jsonString);
    }
}