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
import com.javajedis.bookit.model.RoomModel;
import com.javajedis.bookit.recyclerView.RecyclerViewInterface;

import java.util.ArrayList;

public class Comments_RecyclerViewAdapter extends RecyclerView.Adapter<Comments_RecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> commentsList;

    public Comments_RecyclerViewAdapter(Context context, ArrayList<String> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }
    @NonNull
    @Override
    public Comments_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_recycler_view_row, parent, false);
        return new Comments_RecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Comments_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // assign values to each row
        holder.comment.setText(commentsList.get(position));
    }

    @Override
    public int getItemCount() {
        // returns how many items
        return commentsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView comment;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            comment = itemView.findViewById(R.id.name_textView);
        }
    }
}
