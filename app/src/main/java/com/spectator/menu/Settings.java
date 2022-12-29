package com.spectator.menu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.utils.PreferencesIO;

public class Settings extends BaseActivity {

    private RadioGroup langRadioGroup;
    private Switch themeSwitch;
    private RadioGroup textRadioGroup;
    private PreferencesIO preferencesIO;
    private RadioGroup vibeSelection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        preferencesIO = new PreferencesIO(this);
        themeSwitch = (Switch) findViewById(R.id.theme_switch);
        langRadioGroup = findViewById(R.id.lang_selection);
        vibeSelection = findViewById(R.id.vibe_selection);

        LoadPreferences();

        final Context context = this;
        themeSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferencesIO.putBoolean(PreferencesIO.IS_NIGHT_MODE, b);
                Log.e("Night", String.valueOf(b));
                if (b) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                recreate();
            }
        });

        langRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                Log.e("SettingsCheckedChange", "language changed");
                RadioButton checkedRadioButton = (RadioButton) langRadioGroup.findViewById(checkedId);
                int checkedIndex = langRadioGroup.indexOfChild(checkedRadioButton);

                preferencesIO.putInt(PreferencesIO.LANG_RADIOBUTTON_INDEX, checkedIndex);
                recreate();
            }
        });

        vibeSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId2) {
                RadioButton checkedRadioButton = (RadioButton) vibeSelection.findViewById(checkedId2);
                int checkedIndex2 = vibeSelection.indexOfChild(checkedRadioButton);

                preferencesIO.putInt(PreferencesIO.VIBE_RADIOBUTTON_INDEX, checkedIndex2);

                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibe != null) {
                    switch (checkedId2) {
                        case R.id.vibe_max:
                            vibe.vibrate(500);
                            break;
                        case R.id.vibe_high:
                            vibe.vibrate(250);
                            break;
                        case R.id.vibe_normal:
                            vibe.vibrate(100);
                            break;
                        case R.id.vibe_low:
                            vibe.vibrate(50);
                            break;
                        case R.id.vibe_none:
                            break;
                        default:
                            vibe.vibrate(100);
                            break;
                    }
                }
            }
        });


        /*textRadioGroup = findViewById(R.id.text_selection);
        textRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                RadioButton checkedRadioButton = (RadioButton) textRadioGroup.findViewById(checkedId);
                int checkedIndex = textRadioGroup.indexOfChild(checkedRadioButton);

                SavePreferences(TEXT_RADIOBUTTON_INDEX, checkedIndex);
            }
        });*/

    }

    private void LoadPreferences() {
        int savedLangIndex = preferencesIO.getInt(PreferencesIO.LANG_RADIOBUTTON_INDEX, 1);
        RadioButton langCheckedRadioButton = (RadioButton) langRadioGroup.getChildAt(savedLangIndex);
        langCheckedRadioButton.setChecked(true);

        boolean isNightMode = preferencesIO.getBoolean(PreferencesIO.IS_NIGHT_MODE, true);
        themeSwitch.setChecked(isNightMode);

        int savedVibeIndex = preferencesIO.getInt(PreferencesIO.VIBE_RADIOBUTTON_INDEX, 2);
        RadioButton vibeCheckedRadioButton = (RadioButton) vibeSelection.getChildAt(savedVibeIndex);
        vibeCheckedRadioButton.setChecked(true);
        /*int savedTextIndex = sp.getInt(TEXT_RADIOBUTTON_INDEX, 0);
        RadioButton textCheckedRadioButton = (RadioButton) textRadioGroup.getChildAt(savedTextIndex);
        textCheckedRadioButton.setChecked(true);*/
    }

}
