package com.javajedis.bookit.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;
import com.javajedis.bookit.recyclerview.RecyclerViewInterface;

import java.util.ArrayList;

public class AdminRecyclerViewAdapter extends RecyclerView.Adapter<AdminRecyclerViewAdapter.MyViewHolder>{
    ArrayList<String> adminEmails;
    Context context;

    private final RecyclerViewInterface recyclerViewInterface;

    public AdminRecyclerViewAdapter(Context context, ArrayList<String> adminEmails, RecyclerViewInterface recyclerViewInterface) {
        this.adminEmails = adminEmails;
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    public AdminRecyclerViewAdapter(Context context, RecyclerViewInterface recyclerViewInterface) {
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

    public ArrayList<String> getAdminEmails() {
        return adminEmails;
    }

    @NonNull
    @Override
    public AdminRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.admin_management_recycler_view_row, parent, false);
        return new AdminRecyclerViewAdapter.MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // assign values to each row
        holder.adminName.setText(adminEmails.get(position));
    }

    @Override
    public int getItemCount() {
        return adminEmails.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView adminName;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            adminName = itemView.findViewById(R.id.admin_user_row_textView);

            itemView.setOnClickListener(v -> {
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
