package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.javajedis.bookit.R;
import com.javajedis.bookit.util.ServerRequests;

public class AddNewRoomActivity extends AppCompatActivity {
    private final String TAG = "AddNewRoomActivity";
    private TextView topTextView;
    private String selectedBuilding;
    private EditText roomNumberEditText;
    private String roomNumber;
    private EditText capacityEditText;
    private int capacity;
    private EditText fearturesEditText;
    private String features;
    private String adminEmail;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_room);

        selectedBuilding = getIntent().getStringExtra("building");
        adminEmail = getIntent().getStringExtra("AdminEmail");

        topTextView = findViewById(R.id.add_room_building_name_textView);
        String message = "Add study room to building: " + selectedBuilding;
        topTextView.setText(message);

        roomNumberEditText = findViewById(R.id.room_number_editText);

        capacityEditText = findViewById(R.id.capacity_editText);

        fearturesEditText = findViewById(R.id.features_editText);

        Button addRoomButton = findViewById(R.id.add_new_room_button);

        addRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomNumber = roomNumberEditText.getText().toString();
                capacity = Integer.parseInt(capacityEditText.getText().toString());
                features = fearturesEditText.getText().toString();

                if (roomNumber == null || features == null) {
                    Toast.makeText(AddNewRoomActivity.this, "please fill in all the info and submit", Toast.LENGTH_SHORT).show();
                } else if (capacity<=0) {
                    Toast.makeText(AddNewRoomActivity.this, "capacity must be positive", Toast.LENGTH_SHORT).show();
                } else {
                    ServerRequests.requestAddRoom(selectedBuilding, roomNumber, capacity, features);
                    Intent buildingManagementIntent = new Intent(AddNewRoomActivity.this, BuildingManagementActivity.class);
                    buildingManagementIntent.putExtra("AdminEmail", adminEmail);
                }
            }
        });
    }
}
