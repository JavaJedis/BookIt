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
import com.javajedis.bookit.model.RoomModel;
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;

import java.util.ArrayList;

public class RNRecyclerViewAdapter extends RecyclerView.Adapter<RNRecyclerViewAdapter.MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    Context context;
    ArrayList<RoomModel> roomModels;

    public RNRecyclerViewAdapter(Context context, ArrayList<RoomModel> roomModels, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.roomModels = roomModels;
        this.recyclerViewInterface = recyclerViewInterface;
    }
    @NonNull
    @Override
    public RNRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new RNRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RNRecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.roomName.setText(roomModels.get(position).getRoomName());
        holder.roomPlaceholder.setImageResource(roomModels.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        // returns how many items
        return roomModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView roomName;
        ImageView roomPlaceholder;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            roomPlaceholder = itemView.findViewById(R.id.room_placeholder_imageView);
            roomName = itemView.findViewById(R.id.name_textView);

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
