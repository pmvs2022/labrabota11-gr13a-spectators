package com.spectator.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.counter.MainCounterScreen;
import com.spectator.data.Day;
import com.spectator.utils.DateFormatter;
import com.spectator.utils.JsonIO;
import com.spectator.utils.ObjectWrapperForBinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class Menu extends BaseActivity {

    private TextView addNew;
    private TextView todayDate;
    private TextView electionsDay;
    private int totallyVoters;
    private int totallyBands;
    private JsonIO daysJsonIO;
    private ArrayList<Day> days;
    private LinearLayout scrollList;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        daysJsonIO = new JsonIO(getFilesDir(), Day.DAYS_PATH, Day.ARRAY_KEY, true);

        scrollList = findViewById(R.id.scroll_layout);
        addNew = findViewById(R.id.addNew);

        todayDate = findViewById(R.id.today_date);
        electionsDay = findViewById(R.id.elections_day);

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Dialog.class);

                //Passing daysJsonIO to createCount Activity (Dialog.class)
                final Bundle bundle = new Bundle();
                bundle.putBinder("daysJsonIO", new ObjectWrapperForBinder(daysJsonIO));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Setting locale for current date text
        Locale locale = getResources().getConfiguration().locale;
        String str = getString(R.string.today) + DateFormatter.formatDate(System.currentTimeMillis(), "d MMMM", locale);
        //TODO: make it on date change action
        //Setting current date and election status
        todayDate.setText(str);
        electionsDay.setText(getElectionStage());

        //TODO: maybe rework this, too dumb deleting everything
        //Parsing json file and then building interface
        days = daysJsonIO.parseJsonArray(true, days, true, Day.ARRAY_KEY, Day.class, Day.constructorArgs2, Day.jsonKeys2, Day.defValues);
        //Counting total amount of votes and ribbons
        totallyVoters = 0;
        totallyBands = 0;
        for (Day day: days) {
            totallyVoters += day.getVoters();
            totallyBands += day.getBands();
        }
        //Removing all views (besides "add new" button)
        if (scrollList.getChildCount() > 1)
            scrollList.removeViews(0, scrollList.getChildCount() - 1);

        for (int i = 0; i < days.size(); i++) {
            scrollList.addView(makeNewRow(days.get(i)), i);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Menu", "onStop");
    }

    private LinearLayout makeNewRow(final Day printDay) {
        final LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.day_layout, null);

        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.setMargins(convertDpToPixel(this,10), convertDpToPixel(this, 10), convertDpToPixel(this, 10), 0);
        linearLayout.setLayoutParams(layoutParams);

        TextView newNameView = linearLayout.findViewById(R.id.day_name);
        newNameView.setText(printDay.getName());

        TextView newDateView = linearLayout.findViewById(R.id.day_date);
        newDateView.setText(printDay.getFormattedDate());

        TextView newModeSingleView = linearLayout.findViewById(R.id.mode_single);
        TextView newNumberSingleView = linearLayout.findViewById(R.id.number_single);
        if (printDay.getMode() == Day.PRESENCE) {
            newModeSingleView.setText(getString(R.string.voters));
            newNumberSingleView.setText(String.valueOf(printDay.getVoters()));
        }
        else if (printDay.getMode() == Day.BANDS) {
            newModeSingleView.setText(getString(R.string.bands));
            newNumberSingleView.setText(String.valueOf(printDay.getBands()));
        }
        else if (printDay.getMode() == Day.PRESENCE_BANDS) {
            newModeSingleView.setText(getString(R.string.voters));
            newNumberSingleView.setText(String.valueOf(printDay.getVoters()));
            TextView newModeDualView = linearLayout.findViewById(R.id.mode_dual);
            TextView newNumberDualView = linearLayout.findViewById(R.id.number_dual);
            newModeDualView.setText(getString(R.string.bands));
            newNumberDualView.setText(String.valueOf(printDay.getBands()));
            newModeDualView.setVisibility(View.VISIBLE);
            newNumberDualView.setVisibility(View.VISIBLE);
        }

        final LinearLayout dayLayout = (LinearLayout) linearLayout.findViewById(R.id.day);
        dayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainCounterScreen.class);
                final Bundle bundle = new Bundle();
                bundle.putBinder("daysJsonIO", new ObjectWrapperForBinder(daysJsonIO));
                bundle.putSerializable("day", printDay);
                int totallyVoters = 0;
                int totallyBands = 0;
                for (Day day: days) {
                    if (!printDay.getName().equals(day.getName())) {
                        if (printDay.getYik().equals(day.getYik())) {
                            totallyVoters += day.getVoters();
                            totallyBands += day.getBands();
                        }
                    }
                }
                bundle.putInt("totalVoters", totallyVoters);
                bundle.putInt("totalBands", totallyBands);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        final LinearLayout editor = (LinearLayout) linearLayout.findViewById(R.id.day_editor);
        editor.setVisibility(View.GONE);
        dayLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (editor.getVisibility() == View.GONE) {
                    editor.setVisibility(View.VISIBLE);
                }
                else {
                    editor.setVisibility(View.GONE);
                }
                return true;
            }
        });

        final LinearLayout deleteDay = (LinearLayout) linearLayout.findViewById(R.id.day_delete_button);
        deleteDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int index = scrollList.indexOfChild(linearLayout);
                Day dayToBeDeleted = days.get(index);
                if (dayToBeDeleted.getMode() == Day.PRESENCE) {
                    File fileRecords = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.voters_suffix) + getString(R.string.json_postfix));
                    fileRecords.delete();
                    File fileHourly = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.voters_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix));
                    fileHourly.delete();
                }
                else if (dayToBeDeleted.getMode() == Day.BANDS) {
                    File fileRecords = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.bands_suffix) + getString(R.string.json_postfix));
                    fileRecords.delete();
                    File fileHourly = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.bands_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix));
                    fileHourly.delete();
                }
                else if (dayToBeDeleted.getMode() == Day.PRESENCE_BANDS) {
                    File fileRecords = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.voters_suffix) + getString(R.string.json_postfix));
                    fileRecords.delete();
                    File fileHourly = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.voters_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix));
                    fileHourly.delete();
                    File fileRecords2 = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.bands_suffix) + getString(R.string.json_postfix));
                    fileRecords2.delete();
                    File fileHourly2 = new File(getFilesDir(), dayToBeDeleted.getName() + getString(R.string.bands_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix));
                    fileHourly2.delete();
                }
                scrollList.removeViewAt(index);
                days.remove(index);
                daysJsonIO.deleteAt(index, Day.ARRAY_KEY);
            }
        });

        return linearLayout;
    }

    private static int convertDpToPixel(Context context, float dp) {
        return (int) (dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private String getElectionStage() {
        String currentDate = DateFormatter.formatDateDefaultPattern(System.currentTimeMillis());

        switch (currentDate) {
            case "04.08.2020":
                return getString(R.string.first_day);
            case "05.08.2020" :
                return getString(R.string.second_day);
            case "06.08.2020" :
                return getString(R.string.third_day);
            case "07.08.2020" :
                return getString(R.string.fourth_day);
            case "08.08.2020" :
                return getString(R.string.fifth_day);
            case "09.08.2020" :
                return getString(R.string.primary_day);
            default:
                return getString(R.string.election_days);
        }
    }

}
