package com.spectator.menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.counter.MainCounterScreen;
import com.spectator.data.Day;
import com.spectator.utils.DateFormatter;
import com.spectator.utils.JsonIO;
import com.spectator.utils.ObjectWrapperForBinder;

import java.util.ArrayList;

public class Dialog extends BaseActivity {

    private TextView yesButton;
    private EditText editName;
    private EditText editYikNumber;
    private CheckBox checkPresence;
    private CheckBox checkBands;
    private JsonIO daysJsonIO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e("DialogExtras", "null");
        }
        else {
            Log.i("DialogExtras", "not null");
            daysJsonIO =  (JsonIO) ((ObjectWrapperForBinder)extras.getBinder("daysJsonIO")).getData();
        }

        yesButton = (TextView) findViewById(R.id.confirm);
        editName = (EditText) findViewById(R.id.edit_name);
        editYikNumber = (EditText) findViewById(R.id.edit_yik_number);
        checkPresence = (CheckBox) findViewById(R.id.count_people);
        checkBands = (CheckBox) findViewById(R.id.count_bands);

        final String date = DateFormatter.formatDateDefaultPattern(System.currentTimeMillis());

        //TODO: Now if special character appears deletes everything in editText.Make it delete only the special character
        editName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains("/") || str.contains("\\") || str.contains("&") || str.contains("#") || str.contains(":") || str.contains("|") || str.contains("<") || str.contains(">") || str.contains("*") || str.contains("\"") || str.contains("\'") || str.contains("?")) {
                    s.clear();
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.special_characters_denial, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean canPass = true;
                if (editName.getText().toString().trim().equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.enter_name, Toast.LENGTH_SHORT);
                    toast.show();
                    canPass = false;
                }
                else if (editYikNumber.getText().toString().trim().equals("")) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.enter_pec, Toast.LENGTH_SHORT);
                    toast.show();
                    canPass = false;
                }
                else if (!checkPresence.isChecked() && !checkBands.isChecked()) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.choose_mode, Toast.LENGTH_SHORT);
                    toast.show();
                    canPass = false;
                }

                String[] fileNames = fileList();
                String str, suffix = getString(R.string.voters_suffix);
                if (checkPresence.isChecked() && checkBands.isChecked())
                    suffix = getString(R.string.voters_suffix);
                else if (checkPresence.isChecked())
                    suffix = getString(R.string.voters_suffix);
                else if (checkBands.isChecked())
                    suffix = getString(R.string.bands_suffix);
                for (String fileName : fileNames) {
                    str = editName.getText().toString().trim() + suffix +  getString(R.string.json_postfix);
                    if (str.equals(fileName)) {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.day_exists, Toast.LENGTH_SHORT);
                        toast.show();
                        canPass = false;
                        break;
                    }
                }

                if (canPass) {
                    Intent intent = new Intent(getApplicationContext(), MainCounterScreen.class);

                    //Creating new Day record and writing it to the file
                    int mode = Day.PRESENCE;
                    if (checkPresence.isChecked() && checkBands.isChecked())
                        mode = Day.PRESENCE_BANDS;
                    else if (checkPresence.isChecked())
                        mode = Day.PRESENCE;
                    else if (checkBands.isChecked())
                        mode = Day.BANDS;

                    Day newDay = new Day(editName.getText().toString().trim(), editYikNumber.getText().toString().trim(), date, 0, 0, mode);

                    ArrayList<Day> days = daysJsonIO.parseJsonArray(true, new ArrayList<Day>(), true, Day.ARRAY_KEY, Day.class, Day.constructorArgs2, Day.jsonKeys2, Day.defValues);
                    int totallyVoters = 0;
                    int totallyBands = 0;
                    for (Day day: days) {
                        if (newDay.getYik().equals(day.getYik())) {
                            totallyVoters += day.getVoters();
                            totallyBands += day.getBands();
                        }
                    }

                    daysJsonIO.writeToEndOfFile(newDay.toJSONObject());

                    //Passing date, total votes and daysJsonIO to Voting Activity
                    final Bundle bundle = new Bundle();
                    bundle.putBinder("daysJsonIO", new ObjectWrapperForBinder(daysJsonIO));
                    bundle.putSerializable("day", newDay);
                    bundle.putInt("totalVoters", totallyVoters);
                    bundle.putInt("totalBands", totallyBands);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
