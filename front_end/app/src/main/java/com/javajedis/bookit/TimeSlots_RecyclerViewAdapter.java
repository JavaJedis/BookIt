package com.javajedis.bookit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;

public class TimeSlots_RecyclerViewAdapter extends RecyclerView.Adapter<TimeSlots_RecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<TimeSlotsModel> timeSlotsModels;

    public TimeSlots_RecyclerViewAdapter(Context context, ArrayList<TimeSlotsModel> timeSlotsModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.timeSlotsModels = timeSlotsModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public TimeSlots_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.timeslot_recycler_view_row, parent, false);
        return new TimeSlots_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlots_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.timeSlot.setText(timeSlotsModels.get(position).getTimeInterval());
        holder.clockImage.setImageResource(timeSlotsModels.get(position).getImage());
        holder.status.setText(timeSlotsModels.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        // returns how many items
        return timeSlotsModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView timeSlot;
        ImageView clockImage;

        TextView status;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            clockImage = itemView.findViewById(R.id.clock_imageView);
            timeSlot = itemView.findViewById(R.id.timeslot_name_textView);
            status = itemView.findViewById(R.id.timeslot_status_textView);

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
