package com.javajedis.bookit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SearchResult_RecyclerViewAdapter extends RecyclerView.Adapter<SearchResult_RecyclerViewAdapter.MyViewHolder> {

    ArrayList<String> buildingNames;
    Context context;

    private final RecyclerViewInterface recyclerViewInterface;

    public SearchResult_RecyclerViewAdapter(Context context, ArrayList<String> buildingNames, RecyclerViewInterface recyclerViewInterface) {
        this.buildingNames = buildingNames;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public SearchResult_RecyclerViewAdapter(Context context, RecyclerViewInterface recyclerViewInterface) {
        this.buildingNames = new ArrayList<>();
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setFilterList(ArrayList<String> filterList) {
        this.buildingNames = filterList;
        notifyDataSetChanged();
    }

    public void setBuildingNames(ArrayList<String> buildingNames) {
        this.buildingNames = buildingNames;
    }
    @NonNull
    @Override
    public SearchResult_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.building_recycler_view_row, parent, false);
        return new MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResult_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.buildingNames.setText(buildingNames.get(position));
    }

    @Override
    public int getItemCount() {
        return buildingNames.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView buildingNames;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            buildingNames = itemView.findViewById(R.id.building_name_row_textView);

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
