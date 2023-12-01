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
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;

import java.util.ArrayList;

// From https://droidbyme.medium.com/android-recyclerview-with-single-and-multiple-selection-5d50c0c4c739
public class BuildingSelectionRecyclerViewAdapter extends RecyclerView.Adapter<BuildingSelectionRecyclerViewAdapter.MyViewHolder> {
    private final Context context;
    private ArrayList<String> buildings;
    private int checkedPosition = RecyclerView.NO_POSITION; // no default selection

    private final RecyclerViewInterface recyclerViewInterface;

    public BuildingSelectionRecyclerViewAdapter(Context context, ArrayList<String> buildings, RecyclerViewInterface recyclerViewInterface) {
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

    public ArrayList<String> getBuildings() {
        return buildings;
    }

    @NonNull
    @Override
    public BuildingSelectionRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.assign_buiding_recycler_view_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingSelectionRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.bind(buildings.get(position));
    }
    @Override
    public int getItemCount() {
        return buildings.size();
    }

    public String getSelected() {
        if (checkedPosition != RecyclerView.NO_POSITION) {
            return buildings.get(checkedPosition);
        }
        return null;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView buildingNames;
        private final ImageView checkMark;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            buildingNames = itemView.findViewById(R.id.building_name_row_textView);
            checkMark = itemView.findViewById(R.id.checkMark_imageView);
        }
        public void bind(String buildingCode) {
            if (checkedPosition == RecyclerView.NO_POSITION) {
                checkMark.setVisibility(View.GONE);
            } else {
                if (getAdapterPosition() == checkedPosition) {
                    checkMark.setVisibility(View.VISIBLE);
                } else {
                    checkMark.setVisibility(View.GONE);
                }
            }
            buildingNames.setText(buildingCode);
            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() == checkedPosition) {
                    checkedPosition = RecyclerView.NO_POSITION; // unselect the building on second click
                } else {
                    notifyItemChanged(checkedPosition);
                    checkedPosition = getAdapterPosition();
                }
                if (recyclerViewInterface != null) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            });
        }
    }
}
