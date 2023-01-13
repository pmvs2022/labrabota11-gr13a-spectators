package com.spectator;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import androidx.test.espresso.matcher.ViewMatchers.*;

import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.spectator.menu.Settings;
import com.spectator.utils.PreferencesIO;

import java.util.Arrays;
import java.util.Locale;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    @Rule public ActivityScenarioRule<Settings> activityScenarioRule
            = new ActivityScenarioRule<>(Settings.class);

    private static Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    private static Locale getLocale() {
        return getContext().getResources().getConfiguration().getLocales().get(0);
    }

    @Test
    public void changeTheme() {
        SharedPreferences sp = getContext().getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        boolean nightWas = sp.getBoolean(PreferencesIO.IS_NIGHT_MODE, true);
        onView(withId(R.id.theme_switch)).perform(click());
        boolean nightNew = sp.getBoolean(PreferencesIO.IS_NIGHT_MODE, true);

        assert (nightWas != nightNew);
    }

    @Test
    public void changeLang() {
        SharedPreferences sp = getContext().getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        onView(withId(R.id.lang_en)).perform(click());

        onView(withId(R.id.settings)).check(matches(withText("Settings")));
        int langIndexWas = sp.getInt(PreferencesIO.LANG_RADIOBUTTON_INDEX, 0);

        onView(withId(R.id.lang_by)).perform(click());

        int langIndexNew = sp.getInt(PreferencesIO.LANG_RADIOBUTTON_INDEX, 0);
        onView(withId(R.id.settings)).check(matches(withText("Налады")));

        onView(withId(R.id.lang_en))
                .check(matches(isNotChecked()));
        onView(withId(R.id.lang_ru))
                .check(matches(isNotChecked()));
        onView(withId(R.id.lang_by))
                .check(matches(isChecked()));

        assert (langIndexWas != langIndexNew);
    }

}
