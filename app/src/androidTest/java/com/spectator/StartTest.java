package com.spectator;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.lang.Thread.sleep;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.spectator.menu.Start;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class StartTest {

    @Rule public ActivityScenarioRule<Start> activityScenarioRule
            = new ActivityScenarioRule<>(Start.class);

    @Test
    public void navigateToAboutUs() {
        //Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        onView(withId(R.id.about_us)).perform(click());
        onView(withText(R.string.about_us)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void navigateToSettings() {
        onView(withId(R.id.settings)).perform(click());
        onView(withText(R.string.settings)).check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void navigateToMainMenu() {
        onView(withId(R.id.start)).perform(click());
        onView(withId(R.id.menu_header)).check(matches(isCompletelyDisplayed()));
    }
}
