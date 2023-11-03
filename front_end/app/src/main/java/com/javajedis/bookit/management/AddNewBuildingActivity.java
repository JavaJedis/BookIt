package com.javajedis.bookit.management;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.javajedis.bookit.MainActivity;
import com.javajedis.bookit.R;
import com.javajedis.bookit.util.Constant;
import com.javajedis.bookit.util.ServerRequests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddNewBuildingActivity extends AppCompatActivity {
    private final String TAG = "AddNewRoomActivity";
    private EditText buildingNameEditText;
    private String buildingName;
    private EditText buildingCodeEditText;
    private String buildingCode;
    private EditText addressEditText;
    private String address;
    private final String[] openTimes = new String[7]; // 7 days per week
    private final String[] closeTimes = new String[7];
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_building);

        buildingNameEditText = findViewById(R.id.building_name_editText);
        
        buildingCodeEditText = findViewById(R.id.building_code_editText);
        
        addressEditText = findViewById(R.id.address_editText);

        Button openTimeMonButton = findViewById(R.id.open_time_monday_button);
        openTimeMonButton.setOnClickListener(v -> popTimePicker(0, openTimeMonButton, true));

        Button closeTimeMonButton = findViewById(R.id.close_time_monday_button);
        closeTimeMonButton.setOnClickListener(v -> popTimePicker(0, closeTimeMonButton, false));

        Button openTimeTueButton = findViewById(R.id.open_time_tuesday_button);
        openTimeTueButton.setOnClickListener(v -> popTimePicker(1, openTimeTueButton, true));

        Button closeTimeTueButton = findViewById(R.id.close_time_tuesday_button);
        closeTimeTueButton.setOnClickListener(v -> popTimePicker(1, closeTimeTueButton, false));

        Button openTimeWedButton = findViewById(R.id.open_time_wednesday_button);
        openTimeWedButton.setOnClickListener(v -> popTimePicker(2, openTimeWedButton, true));

        Button closeTimeWedButton = findViewById(R.id.close_time_wednesday_button);
        closeTimeWedButton.setOnClickListener(v -> popTimePicker(2, closeTimeWedButton, false));

        Button openTimeThurButton = findViewById(R.id.open_time_thursday_button);
        openTimeThurButton.setOnClickListener(v -> popTimePicker(3, openTimeThurButton, true));

        Button closeTimeThurButton = findViewById(R.id.close_time_thursday_button);
        closeTimeThurButton.setOnClickListener(v -> popTimePicker(3, closeTimeThurButton, false));

        Button openTimeFriButton = findViewById(R.id.open_time_friday_button);
        openTimeFriButton.setOnClickListener(v -> popTimePicker(4, openTimeFriButton, true));

        Button closeTimeFriButton = findViewById(R.id.close_time_friday_button);
        closeTimeFriButton.setOnClickListener(v -> popTimePicker(4, closeTimeFriButton, false));

        Button openTimeSatButton = findViewById(R.id.open_time_saturday_button);
        openTimeSatButton.setOnClickListener(v -> popTimePicker(5, openTimeSatButton, true));

        Button closeTimeSatButton = findViewById(R.id.close_time_saturday_button);
        closeTimeSatButton.setOnClickListener(v -> popTimePicker(5, closeTimeSatButton, false));

        Button openTimeSunButton = findViewById(R.id.open_time_sunday_button);
        openTimeSunButton.setOnClickListener(v -> popTimePicker(6, openTimeSunButton, true));

        Button closeTimeSunButton = findViewById(R.id.close_time_sunday_button);
        closeTimeSunButton.setOnClickListener(v -> popTimePicker(6, closeTimeSunButton, false));

        Button addBuildingButton = findViewById(R.id.add_new_building_button);
        addBuildingButton.setOnClickListener(v -> {
            buildingName = buildingNameEditText.getText().toString();
            buildingCode = buildingCodeEditText.getText().toString();
            address = addressEditText.getText().toString();

            boolean timePass = true;
            for (int i = 0; i < openTimes.length; i++) {
                if (openTimes[i] == null || closeTimes[i] == null) {
                    timePass = false;
                    Toast.makeText(AddNewBuildingActivity.this, "Please fill in all the info and submit", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            if (timePass) {
                if (Objects.equals(buildingName, "") || Objects.equals(buildingCode, "") || address.equals("")) {
                    Toast.makeText(AddNewBuildingActivity.this, "Please fill in all the info and submit", Toast.LENGTH_SHORT).show();
                } else if (buildingCode.length() < Constant.BUILDING_CODE_LENGTH_MIN || buildingCode.length() > Constant.BUILDING_CODE_LENGTH_MAX) {
                    Toast.makeText(AddNewBuildingActivity.this, "building code must have length of 4", Toast.LENGTH_SHORT).show();
                } else if (!buildingCode.matches("[0-9A-Z]+")) { // From https://stackoverflow.com/questions/5238491/check-if-string-contains-only-letters
                    Toast.makeText(AddNewBuildingActivity.this, "building code contains capital letters only", Toast.LENGTH_SHORT).show();
                } else {
                    Intent mainActivityIntent = new Intent(AddNewBuildingActivity.this, MainActivity.class);
//                    ArrayList<String> openTimesList = (ArrayList<String>) Arrays.asList(openTimes);
//                    ArrayList<String> closeTimesList = (ArrayList<String>) Arrays.asList(closeTimes);
                    ServerRequests.requestAddBuilding(buildingName, buildingCode, address, openTimes, closeTimes, AddNewBuildingActivity.this, mainActivityIntent);
                }
            }
        });
    }

    private void popTimePicker(int dayIndex, Button button, boolean isOpenTime) {
        final int[] hour = new int[1];
        final int[] min = new int[1];
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                int roundedMinute = (minute < 30) ? 0 : 30;

                hour[0] = hourOfDay;
                min[0] = roundedMinute;
                button.setText(formatTime(hour[0], min[0]));
                if (isOpenTime) {
                    openTimes[dayIndex] = formatTime(hour[0], min[0]);
                } else {
                    closeTimes[dayIndex] = formatTime(hour[0], min[0]);
                }
            }
        };

        int style = AlertDialog.THEME_HOLO_DARK;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, style, onTimeSetListener, hour[0], min[0], true);
        timePickerDialog.setTitle("select time");
        timePickerDialog.show();
    }

    private String formatTime(int hour, int min) {
        return String.format(Locale.getDefault(), "%02d%02d", hour, min);
    }
}
