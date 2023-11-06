package com.javajedis.bookit.recyclerview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javajedis.bookit.R;

import java.util.ArrayList;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> commentsList;

    public CommentsRecyclerViewAdapter(Context context, ArrayList<String> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }
    @NonNull
    @Override
    public CommentsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // responsible for appearance
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.comment_recycler_view_row, parent, false);
        return new CommentsRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsRecyclerViewAdapter.MyViewHolder holder, int position) {
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
