package com.javajedis.bookit.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.model.BookingsModel;
import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;

import java.util.ArrayList;

public class BookingsRecyclerViewAdapter extends RecyclerView.Adapter<BookingsRecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<BookingsModel> bookingsModels;

    public BookingsRecyclerViewAdapter(Context context, ArrayList<BookingsModel> bookingsModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.bookingsModels = bookingsModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public BookingsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bookings_recycler_view_row, parent, false);
        return new BookingsRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingsRecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.room.setText(bookingsModels.get(position).getRoom());
        holder.image.setImageResource(bookingsModels.get(position).getImage());
        holder.timeSlot.setText(bookingsModels.get(position).getTimeSlot());
        holder.date.setText(bookingsModels.get(position).getDate());
        holder.action.setText(bookingsModels.get(position).getAction());
    }

    @Override
    public int getItemCount() {
        // returns how many items
        return bookingsModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView room;
        ImageView image;

        TextView timeSlot;

        TextView date;

        TextView action;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            image = itemView.findViewById(R.id.calendar_bookings_imageView);
            room = itemView.findViewById(R.id.room_bookings_textView);
            timeSlot = itemView.findViewById(R.id.timeslot_bookings_textView);
            date = itemView.findViewById(R.id.date_bookings_textView);
            action = itemView.findViewById(R.id.action_bookings_textView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
