package com.javajedis.bookit.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.model.TimeSlotsModel;
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;

import java.util.ArrayList;

public class TimeSlotsRecyclerViewAdapter extends RecyclerView.Adapter<TimeSlotsRecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<TimeSlotsModel> timeSlotsModels;

    public TimeSlotsRecyclerViewAdapter(Context context, ArrayList<TimeSlotsModel> timeSlotsModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.timeSlotsModels = timeSlotsModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public TimeSlotsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.timeslot_recycler_view_row, parent, false);
        return new TimeSlotsRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotsRecyclerViewAdapter.MyViewHolder holder, int position) {
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

    public TimeSlotsModel getItemAtPosition(int position) {
        return timeSlotsModels.get(position);
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
