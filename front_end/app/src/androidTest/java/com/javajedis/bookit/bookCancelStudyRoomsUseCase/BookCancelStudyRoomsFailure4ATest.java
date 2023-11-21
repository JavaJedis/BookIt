package com.javajedis.bookit.bookCancelStudyRoomsUseCase;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.javajedis.bookit.ListTimeSlotsActivity.getCurrentTime;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.javajedis.bookit.MainActivity;
import com.javajedis.bookit.R;
import com.javajedis.bookit.util.ToastMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class BookCancelStudyRoomsFailure4ATest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void bookCancelFailure4ATest() {
        ViewInteraction ic = onView(
                allOf(withText("Sign in"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.sign_in_button),
                                        childAtPosition(
                                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        ic.perform(click());

        // ChatGPT usage: Yes --> from here
        // Initialize UiDevice instance
        UiDevice uiDevice = UiDevice.getInstance(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation());
        // Wait for the Google Sign-In screen to appear
        uiDevice.waitForIdle();

        // Click on the first Google account in the account picker
        UiObject2 googleAccount = uiDevice.findObject(By.textContains("@")); // Modify the selector as needed

        // Click on the Google account
        if (googleAccount != null) {
            googleAccount.click();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.bookings_button), withText("bookings"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.bookings_recyclerView),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()));
        recyclerView.check(matches(isDisplayed()));


        // ChatGPT usage: Yes --> from here
        // get current date
        LocalDate currentDate = LocalDate.now();
        LocalDate targetDate = LocalDate.of(2023, 11, 30);

        if (currentDate.isAfter(targetDate)) {
            // Assert that the current date is after 30-11-2023
            assertTrue(currentDate.isAfter(targetDate));
        } else if (currentDate.isEqual(targetDate)){
            // Same dateAssert that the current time is after 20:00
            String currentTime = getCurrentTime();
            int currentHour = Integer.parseInt(currentTime.substring(0, 2));
            int currentMinute = Integer.parseInt(currentTime.substring(2));

            int targetHour = 22; // TODO: confirm this later
            int targetMinute = 30; // TODO: confirm this later

            // Convert both current time and target time to minutes for easy comparison
            int currentTimeInMinutes = currentHour * 60 + currentMinute;
            int targetTimeInMinutes = targetHour * 60 + targetMinute;

            assertTrue(currentTimeInMinutes > targetTimeInMinutes);
        } else {
            // Test fails
            fail("current time is before booking starting time for newly booked session");
        }

        // click a expired booking
        uiDevice.waitForIdle();

        UiObject2 expiredBooking = uiDevice.findObject(By.textContains("expired")); // Modify the selector as needed

        if (expiredBooking != null) {
            expiredBooking.click();
        }

        ViewInteraction viewGroup = onView(
                allOf(withParent(withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));


        ViewInteraction viewGroup2 = onView(
                allOf(withParent(allOf(withId(R.id.bookings_recyclerView),
                                withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup2.check(matches(isDisplayed()));
        // From: https://www.qaautomated.com/2016/01/how-to-test-toast-message-using-espresso.html
        onView(withText("Your booking has expired!")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
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
