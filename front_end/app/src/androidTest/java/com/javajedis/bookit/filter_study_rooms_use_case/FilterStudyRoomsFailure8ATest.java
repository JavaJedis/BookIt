package com.javajedis.bookit.filter_study_rooms_use_case;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
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
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

import com.javajedis.bookit.MainActivity;
import com.javajedis.bookit.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FilterStudyRoomsFailure8ATest {

    @Rule
    public ActivityScenarioRule<MainActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void filterStudyRoomsFailure8ATest() {
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
                allOf(withId(R.id.filter_button), withText("filter study rooms"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                8),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.filter_bottom_button), withText("filter"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton2.perform(click());

        UiObject2 denyLocations = uiDevice.findObject(By.textContains("Deny"));

        // Click on the Google account
        if (denyLocations != null) {
            denyLocations.click();
        }
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
