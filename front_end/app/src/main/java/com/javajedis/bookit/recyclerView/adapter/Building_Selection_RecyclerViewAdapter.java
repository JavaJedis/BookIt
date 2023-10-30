package com.javajedis.bookit.recyclerView.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;

import java.util.ArrayList;

// From https://droidbyme.medium.com/android-recyclerview-with-single-and-multiple-selection-5d50c0c4c739
public class Building_Selection_RecyclerViewAdapter extends RecyclerView.Adapter<Building_Selection_RecyclerViewAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<String> buildings;
    private int checkedPosition = -1; // no default selection

    private final RecyclerViewInterface recyclerViewInterface;

    public Building_Selection_RecyclerViewAdapter(Context context, ArrayList<String> buildings, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.buildings = buildings;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setFilterList(ArrayList<String> filterList) {
        this.buildings = filterList;
        notifyDataSetChanged();
    }

    public void setCheckedPosition(int checkedPosition) {
        this.checkedPosition = checkedPosition;
    }

    public int getCheckedPosition() {
        return this.checkedPosition;
    }

    @NonNull
    @Override
    public Building_Selection_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.assign_buiding_recycler_view_row, parent, false);
        return new MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull Building_Selection_RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.bind(buildings.get(position));
    }
    @Override
    public int getItemCount() {
        return buildings.size();
    }

    public String getSelected() {
        if (checkedPosition != -1) {
            return buildings.get(checkedPosition);
        }
        return null;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView buildingNames;
        private ImageView checkMark;
        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            buildingNames = itemView.findViewById(R.id.building_name_row_textView);
            checkMark = itemView.findViewById(R.id.checkMark_imageView);
        }
        public void bind(String buildingCode) {
            if (checkedPosition == -1) {
                checkMark.setVisibility(View.GONE);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    checkMark.setVisibility(View.VISIBLE);
                } else {
                    checkMark.setVisibility(View.GONE);
                }
            }
            buildingNames.setText(buildingCode);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkMark.setVisibility(View.VISIBLE);
                    if (checkedPosition != getAdapterPosition()) {
                        notifyItemChanged(checkedPosition);
                        checkedPosition = getAdapterPosition();
                    }
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
