package com.javajedis.bookit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

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
    }
}