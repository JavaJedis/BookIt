package com.javajedis.bookit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.javajedis.bookit.model.BookingsModel;
import com.javajedis.bookit.recyclerview.adapter.BookingsRecyclerViewAdapter;

// ChatGPT usage: Yes
public class BookingsRecyclerViewCheckConfirmedAssertion implements ViewAssertion {
    private final int position;

    public BookingsRecyclerViewCheckConfirmedAssertion(int position) {
        this.position = position;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            // Assuming your adapter is YourAdapter
            assert adapter != null;
            BookingsModel item = ((BookingsRecyclerViewAdapter) adapter).getItemAtPosition(position);

            // Now you can perform assertions or actions based on the data in the item
            // For example, you can check a specific property of the item
            assertThat(item.getAction(), is("confirmed"));
        } else {
            throw noViewFoundException;
        }
    }
}
