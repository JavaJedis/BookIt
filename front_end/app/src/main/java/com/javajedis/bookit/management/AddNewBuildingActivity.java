package com.javajedis.bookit.management;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.javajedis.bookit.MainActivity;
import com.javajedis.bookit.R;
import com.javajedis.bookit.util.Constant;
import com.javajedis.bookit.util.ServerRequests;

public class AddNewBuildingActivity extends AppCompatActivity {
    private final String TAG = "AddNewRoomActivity";

    private EditText buildingNameEditText;
    private String buildingName;
    private EditText buildingCodeEditText;
    private String buildingCode;
    private EditText addressEditText;
    private String address;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_building);

        buildingNameEditText = findViewById(R.id.building_name_editText);
        
        buildingCodeEditText = findViewById(R.id.building_code_editText);
        
        addressEditText = findViewById(R.id.address_editText);

        Button addBuildingButton = findViewById(R.id.add_new_building_button);
        addBuildingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildingName = buildingNameEditText.getText().toString();
                buildingCode = buildingCodeEditText.getText().toString();
                address = addressEditText.getText().toString();
                if (buildingName == null || buildingCode == null || address == null) {
                    Toast.makeText(AddNewBuildingActivity.this, "Please fill in all the info and submit", Toast.LENGTH_SHORT).show();
                } else if (buildingCode.length() != Constant.BUILDING_CODE_LENGTH) {
                    Toast.makeText(AddNewBuildingActivity.this, "building code must have length of 4", Toast.LENGTH_SHORT).show();
                }
                else if (!buildingCode.matches("[A-Z]+")) { // From https://stackoverflow.com/questions/5238491/check-if-string-contains-only-letters
                    Toast.makeText(AddNewBuildingActivity.this, "building code contains capital letters only", Toast.LENGTH_SHORT).show();
                } else  {
                    ServerRequests.requestAddBuilding(buildingName, buildingCode, address);
                    Intent mainActivityIntent = new Intent(AddNewBuildingActivity.this, MainActivity.class);
                    startActivity(mainActivityIntent);
                }
            }
        });
    }
}
