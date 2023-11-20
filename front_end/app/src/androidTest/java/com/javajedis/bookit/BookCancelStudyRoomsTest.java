package com.javajedis.bookit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withResourceName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BookCancelStudyRoomsTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.javajedis.bookit", appContext.getPackageName());
    }
//
//    public void openMainActivity() {
//        onView(withId(R.id.sign_in_button)).perform(click());
//        // TODO: perform signing in here
//    }
//
//    public void openListTimeSlotsActivity() {
//        openMainActivity();
//        onView(withId(R.id.search_button)).perform(click());
//        onView(withId(R.id.study_rooms_button)).perform(click());
//        // Click the first building
//        onView(withResourceName()).perform(click());
//        // Click the first study room
//        onView(withResourceName()).perform(click());
//        // Click the first room
//        onView(withResourceName()).perform(click());
//        // Click "book now" button
//        onView(withId(R.id.book_now_button)).perform(click());
//    }
//
//    public void openBookingsActivity() {
//        openMainActivity();
//        onView(withId(R.id.bookings_button)).perform(click());
//    }

//    @Test
//    public void bookStudyRoomSuccessTest() {
//        openListTimeSlotsActivity();
//        // Check there exits study rooms with text showing "book now"
//        onView(withText("book now")).check(matches(isDisplayed()));
//        // Click the first time slot that shows as "book now"
//        onView(withResourceName()).perform(click());
//        // Check navigate to bookings activity
//        // From : https://stackoverflow.com/questions/25998659/espresso-how-can-i-check-if-an-activity-is-launched-after-performing-a-certain
//        Intents.init();
//        intended(hasComponent(BookingsActivity.class.getName()));
//        // Check the selected time slot is displayed
//        onView(withResourceName()).check(matches(isDisplayed()));
//        // Check if the time slot says: "click to cancel"
//        onView(withText("book now")).check(matches(isDisplayed()));
//    }

//    @Test
//    public void bookStudyRoomFailure2ATest() {
//        openListTimeSlotsActivity();
//        // Check there exits study rooms with text showing "book now"
//        onView(withText("get on waitlist")).check(matches(isDisplayed()));
//        // Click the first time slot that shows as "get on waitlist"
//        onView(withResourceName()).perform(click());
//        // Check dialogue is opened with the text: "You have been added to the wait-list!"
//        onView(withText("You have been added to the wait-list!")).check(matches(isDisplayed()));
////        // Click the same time slot again
////        onView(withResourceName()).perform(click());
////        // Check dialogue is opened with the text: "Successfully added to the waitlist"
////        onView(withText("Successfully added to the waitlist")).check(matches(isDisplayed()));
//
//    }

//    @Test
//    public void cancelStudyRoomSuccessTest() {
//        openBookingsActivity();
//        // Check there exits study rooms with text showing "click to cancel"
//        onView(withText("click to cancel")).check(matches(isDisplayed()));
//        // TODO: Check the current time must be ahead of the starting time of that time slot
//
//        // Click that time slot
//        onView(withResourceName()).perform(click());
//        // Check the time slot is removed from the list of bookings
//        onView(withResourceName()).check(matches());
//    }
//
//    @Test
//    public void cancelStudyRoomFailure4ATest() {
//        openListTimeSlotsActivity();
//        // check there exits study rooms with text showing "book now"
//        onView(withText("book now")).check(matches(isDisplayed()));
//        // click the first time slot that shows as “book now”
//        onView(withResourceName()).perform(click());
//        // check navigate to bookings activity
//        // From : https://stackoverflow.com/questions/25998659/espresso-how-can-i-check-if-an-activity-is-launched-after-performing-a-certain
//        Intents.init();
//        intended(hasComponent(BookingsActivity.class.getName()));
//        // check the selected time slot is displayed
//        onView(withResourceName()).check(matches(isDisplayed()));
//        // check if the time slot says: “click to cancel”
//        onView(withText("book now")).check(matches(isDisplayed()));
//    }
}
