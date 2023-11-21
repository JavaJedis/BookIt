package com.javajedis.bookit.confirmAttendanceUseCase;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;
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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ConfirmAttendanceFailure3ATest {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void confirmAttendanceFailure3ATest() {
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

        //  Click on the confirm attendance button
        UiObject2 confirmAttendance = uiDevice.findObject(By.textContains("click to confirm")); // Modify the selector as needed

        if (confirmAttendance != null) {
            confirmAttendance.click();
        }

        // From: https://www.qaautomated.com/2016/01/how-to-test-toast-message-using-espresso.html
        // check if the message appears with the text: “You are too far away from your booking!”
        onView(withText("You are too far away from your booking!")).inRoot(new ToastMatcher())
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
