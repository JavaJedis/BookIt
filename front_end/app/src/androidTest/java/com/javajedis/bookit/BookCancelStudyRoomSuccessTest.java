package com.javajedis.bookit;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BookCancelStudyRoomSuccessTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void bookCancelStudyRoomSuccessTest() {
        ViewInteraction ic = onView(
                allOf(withText("Sign in"),
                        childAtPosition(
                                allOf(withId(R.id.sign_in_button),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        ic.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.search_button), withText("search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.study_rooms_button), withText("study rooms"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                1),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.building_recyclerView),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.room_names_recyclerview),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView2.perform(actionOnItemAtPosition(3, click()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.book_now_button), withText("book now"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.calendarRecyclerView),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView3.perform(actionOnItemAtPosition(32, click()));

        ViewInteraction recyclerView4 = onView(
                allOf(withId(R.id.timeslots_recycler_view),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        recyclerView4.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.timeslot_status_textView), withText("book now"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView.check(matches(withText("book now")));

        ViewInteraction recyclerView5 = onView(
                allOf(withId(R.id.timeslots_recycler_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView5.perform(actionOnItemAtPosition(47, click()));

        ViewInteraction recyclerView6 = onView(
                allOf(withId(R.id.bookings_recyclerView),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        recyclerView6.check(matches(isDisplayed()));

        ViewInteraction viewGroup = onView(
                allOf(withParent(allOf(withId(R.id.bookings_recyclerView),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.action_bookings_textView), withText("click to cancel"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView2.check(matches(withText("click to cancel")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.timeslot_bookings_textView), withText("2330-2400"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView3.check(matches(withText("2330-2400")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.date_bookings_textView), withText("30-11-2023"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView4.check(matches(withText("30-11-2023")));

//        ViewInteraction textView5 = onView(
//                allOf(withId(com.android.systemui.R.id.clock), withText("2:47"), withContentDescription("2:47 PM"),
//                        withParent(allOf(withId(com.android.systemui.R.id.quick_status_bar_system_icons),
//                                withParent(withId(com.android.systemui.R.id.header)))),
//                        isDisplayed()));
//        textView5.check(matches(withText("2:47")));
//
//        ViewInteraction textView6 = onView(
//                allOf(withId(com.android.systemui.R.id.date), withText("Mon, Nov 20"),
//                        withParent(allOf(withId(com.android.systemui.R.id.quick_qs_status_icons),
//                                withParent(withId(com.android.systemui.R.id.header)))),
//                        isDisplayed()));
//        textView6.check(matches(withText("Mon, Nov 20")));

        ViewInteraction recyclerView7 = onView(
                allOf(withId(R.id.bookings_recyclerView),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView7.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction viewGroup2 = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        viewGroup2.check(doesNotExist());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
