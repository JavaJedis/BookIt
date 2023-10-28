package com.javajedis.bookit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    
    private Button previousButton;

    private Button nextButton;

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    private String buildingCode;
    private String roomNumber;

    private Boolean fromFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        fromFilter = getIntent().getBooleanExtra("fromFilter", false);

        if (!fromFilter) {
            String codePlusName = getIntent().getStringExtra("codePlusNumber");
            assert codePlusName != null;
            String[] parts = codePlusName.split(" ");
            buildingCode = parts[0];
            roomNumber = parts[1];
        }


        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

        previousButton = findViewById(R.id.previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousMonthAction();
            }
        });

        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextMonthAction();
            }
        });
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
//        ArrayList<String> daysInMonthArray = new ArrayList<>();
//        YearMonth yearMonth = YearMonth.from(date);
//
//        int daysInMonth = yearMonth.lengthOfMonth();
//
//        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
//        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
//
//        for (int i = 0; i <= 42; i++) {
//            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
//                daysInMonthArray.add("");
//            } else {
//                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
//            }
//        }
//        return daysInMonthArray;
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        LocalDate currentDate = LocalDate.now();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate day = selectedDate.withDayOfMonth(i);

            // Check if the day is within the next 14 days, including the current day
            boolean isSelectable = !day.isBefore(currentDate) && !day.isAfter(currentDate.plusDays(14));

            if (isSelectable) {
                daysInMonthArray.add(String.valueOf(i));
            } else {
                daysInMonthArray.add("X"); // Make the day unclickable
            }
        }

        // Add empty strings for days before the 1st of the month and after the last day
        for (int i = 0; i < dayOfWeek; i++) {
            daysInMonthArray.add(0, "");
        }
        for (int i = daysInMonth + dayOfWeek; i < 42; i++) {
            daysInMonthArray.add("");
        }

        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    @SuppressLint("WrongViewCast")
    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.month_year_textView);
    }

    private void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    private void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("") && !dayText.equals("X")) {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            if (fromFilter) {
                Intent filterIntent = new Intent(CalendarActivity.this, FilterActivity.class);
                filterIntent.putExtra("date", dayText + " " + monthYearFromDate(selectedDate));
                startActivity(filterIntent);
            } else {
                Intent timeSlotsIntent = new Intent(CalendarActivity.this, ListTimeSlotsActivity.class);
                timeSlotsIntent.putExtra("buildingCode", buildingCode);
                timeSlotsIntent.putExtra("roomNumber", roomNumber);
                timeSlotsIntent.putExtra("date", dayText + " " + monthYearFromDate(selectedDate));
                startActivity(timeSlotsIntent);
            }
        }
    }
}