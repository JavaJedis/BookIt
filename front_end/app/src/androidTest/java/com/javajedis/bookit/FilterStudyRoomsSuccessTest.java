package com.javajedis.bookit;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FilterStudyRoomsSuccessTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void filterStudyRoomsSuccessTest() {
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
                allOf(withId(R.id.filter_button), withText("filter study rooms"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction viewGroup = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        ViewInteraction button = onView(
                allOf(withId(R.id.day_button), withText("day"),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.start_time_button), withText("start time"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));

        ViewInteraction button3 = onView(
                allOf(withId(R.id.hours_button), withText("duration"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        button3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.day_button), withText("day"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        linearLayout.check(matches(isDisplayed()));

        ViewInteraction linearLayout2 = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        linearLayout2.check(matches(isDisplayed()));

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.calendarRecyclerView),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(32, click()));

        ViewInteraction viewGroup2 = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        viewGroup2.check(matches(isDisplayed()));

        ViewInteraction button4 = onView(
                allOf(withId(R.id.day_button), withText("30 November 2023"),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        button4.check(matches(isDisplayed()));

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.start_time_button), withText("start time"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction linearLayout3 = onView(
                allOf(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                        withParent(allOf(withId(android.R.id.content),
                                withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class)))),
                        isDisplayed()));
        linearLayout3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction button5 = onView(
                allOf(withId(R.id.start_time_button), withText("04:00 PM"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        button5.check(matches(isDisplayed()));

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.start_time_button), withText("04:00 PM"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        appCompatButton5.perform(click());

        ViewInteraction appCompatButton6 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatButton6.perform(click());

        ViewInteraction button6 = onView(
                allOf(withId(R.id.start_time_button), withText("07:30 PM"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        button6.check(matches(isDisplayed()));

        ViewInteraction appCompatButton7 = onView(
                allOf(withId(R.id.hours_button), withText("duration"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                1)),
                                1),
                        isDisplayed()));
        appCompatButton7.perform(click());

        ViewInteraction frameLayout = onView(
                allOf(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class), isDisplayed()));
        frameLayout.check(matches(isDisplayed()));

        DataInteraction appCompatTextView = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(3);
        appCompatTextView.perform(click());

        ViewInteraction button7 = onView(
                allOf(withId(R.id.hours_button), withText("2 hours"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        button7.check(matches(isDisplayed()));

        ViewInteraction appCompatButton8 = onView(
                allOf(withId(R.id.filter_bottom_button), withText("filter"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton8.perform(click());

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.study_rooms_filter_recycler_view),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        recyclerView2.check(matches(isDisplayed()));

        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.study_rooms_filter_recycler_view),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView3.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction viewGroup3 = onView(
                allOf(withParent(allOf(withId(android.R.id.content),
                                withParent(withId(androidx.appcompat.R.id.action_bar_root)))),
                        isDisplayed()));
        viewGroup3.check(matches(isDisplayed()));

        ViewInteraction appCompatButton9 = onView(
                allOf(withId(R.id.book_now_button), withText("book now"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                7),
                        isDisplayed()));
        appCompatButton9.perform(click());

        ViewInteraction recyclerView4 = onView(
                allOf(withId(R.id.calendarRecyclerView),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                2)));
        recyclerView4.perform(actionOnItemAtPosition(32, click()));

        ViewInteraction recyclerView5 = onView(
                allOf(withId(R.id.timeslots_recycler_view),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        recyclerView5.check(matches(isDisplayed()));

        ViewInteraction viewGroup4 = onView(
                allOf(withParent(allOf(withId(R.id.timeslots_recycler_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup4.check(matches(isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.timeslot_name_textView), withText("1930-2000"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView.check(matches(withText("1930-2000")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.timeslot_status_textView), withText("book now"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView2.check(matches(withText("book now")));

        ViewInteraction viewGroup5 = onView(
                allOf(withParent(allOf(withId(R.id.timeslots_recycler_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup5.check(matches(isDisplayed()));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.timeslot_name_textView), withText("2000-2030"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView3.check(matches(withText("2000-2030")));

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.timeslot_status_textView), withText("book now"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView4.check(matches(withText("book now")));

        ViewInteraction viewGroup6 = onView(
                allOf(withParent(allOf(withId(R.id.timeslots_recycler_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup6.check(matches(isDisplayed()));

        ViewInteraction textView5 = onView(
                allOf(withId(R.id.timeslot_name_textView), withText("2030-2100"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView5.check(matches(withText("2030-2100")));

        ViewInteraction textView6 = onView(
                allOf(withId(R.id.timeslot_status_textView), withText("book now"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView6.check(matches(withText("book now")));

        ViewInteraction viewGroup7 = onView(
                allOf(withParent(allOf(withId(R.id.timeslots_recycler_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup7.check(matches(isDisplayed()));

        ViewInteraction textView7 = onView(
                allOf(withId(R.id.timeslot_name_textView), withText("2100-2130"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView7.check(matches(withText("2100-2130")));

        ViewInteraction textView8 = onView(
                allOf(withId(R.id.timeslot_status_textView), withText("book now"),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class))),
                        isDisplayed()));
        textView8.check(matches(withText("book now")));
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
