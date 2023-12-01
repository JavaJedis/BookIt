package com.javajedis.bookit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.javajedis.bookit.model.TimeSlotsModel;
import com.javajedis.bookit.recyclerview.adapter.TimeSlotsRecyclerViewAdapter;
// ChatGPT usage: Yes
public class TimeSlotsRecyclerViewStatusBookNowAssertion implements ViewAssertion {
    private final int position;

    public TimeSlotsRecyclerViewStatusBookNowAssertion(int position) {
        this.position = position;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();

            // Assuming your adapter is YourAdapter
            assert adapter != null;
            TimeSlotsModel item = ((TimeSlotsRecyclerViewAdapter) adapter).getItemAtPosition(position);

            // Now you can perform assertions or actions based on the data in the item
            // For example, you can check a specific property of the item
            assertThat(item.getStatus(), is("book now"));
        } else {
            throw noViewFoundException;
        }
    }
}