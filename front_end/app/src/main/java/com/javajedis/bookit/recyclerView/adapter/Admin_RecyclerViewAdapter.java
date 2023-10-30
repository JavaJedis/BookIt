package com.javajedis.bookit.recyclerView.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;

import java.util.ArrayList;

public class Admin_RecyclerViewAdapter extends RecyclerView.Adapter<Buildings_RecyclerViewAdapter.MyViewHolder>{
    ArrayList<String> adminEmails;
    Context context;

    private final RecyclerViewInterface recyclerViewInterface;

    public Admin_RecyclerViewAdapter(Context context, ArrayList<String> adminEmails, RecyclerViewInterface recyclerViewInterface) {
        this.adminEmails = adminEmails;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public Admin_RecyclerViewAdapter(Context context, RecyclerViewInterface recyclerViewInterface) {
        this.adminEmails = new ArrayList<>();
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public void setFilterList(ArrayList<String> filterList) {
        this.adminEmails = filterList;
        notifyDataSetChanged();
    }

    public void setAdminEmails(ArrayList<String> adminEmails) {
        this.adminEmails = adminEmails;
    }
    @NonNull
    @Override
    public Buildings_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.building_recycler_view_row, parent, false);
        return new Buildings_RecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull Buildings_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.buildingNames.setText(adminEmails.get(position));
    }

    @Override
    public int getItemCount() {
        return adminEmails.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView buildingNames;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            buildingNames = itemView.findViewById(R.id.admin_user_row_textView);

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
