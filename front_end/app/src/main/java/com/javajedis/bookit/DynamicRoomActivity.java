package com.javajedis.bookit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DynamicRoomActivity extends AppCompatActivity {

    private ImageView roomImage;
    private TextView roomName;
    private TextView address;
    private TextView capacity;
    private TextView description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_room);

        roomImage = findViewById(R.id.room_imageView);

        Picasso.get()
                .load(getIntent().getStringExtra("image_url"))
                .into(roomImage);

        roomName = findViewById(R.id.room_name_textView);
        roomName.setText(getIntent().getStringExtra("roomName"));

        address = findViewById(R.id.address_textView);
        address.setText(getIntent().getStringExtra("address"));

        capacity = findViewById(R.id.capacity_textView);
        capacity.setText(getIntent().getStringExtra("capacity"));

        description = findViewById(R.id.description_textView);
        description.setText(getIntent().getStringExtra("description"));
    }
}