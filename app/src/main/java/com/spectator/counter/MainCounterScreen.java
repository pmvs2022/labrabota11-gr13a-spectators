package com.spectator.counter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.data.Comment;
import com.spectator.data.Day;
import com.spectator.data.Hour;
import com.spectator.data.Voter;
import com.spectator.detailedinfo.Details;
import com.spectator.realm.Realm;
import com.spectator.utils.DateFormatter;
import com.spectator.utils.JsonIO;
import com.spectator.utils.ObjectWrapperForBinder;
import com.spectator.utils.PreferencesIO;
import com.spectator.utils.UniversalPagerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainCounterScreen extends BaseActivity {

    private static final int EDIT_YIK_REQUEST = 11;
    private static final int CREATE_COMMENT_REQUEST = 12;

    private TextView voteButtonMain;
    private TextView voteButtonSecond;
    private LinearLayout deleteLastButtonMain;
    private LinearLayout deleteLastButtonSecond;
    private LinearLayout commentButton;
    private TextView yikNumber;
    private VerticalViewPager viewPager;
    private DailyInfoFragment dailyInfoFragment;
    private AdditionalInfoFragment additionalInfoFragment;

    private Numbers votersNumbers = new Numbers();
    private Numbers bandsNumbers = new Numbers();
    private Day day;

    private PreferencesIO preferencesIO;
    private JsonIO votersJsonIO;
    private JsonIO bandsJsonIO;
    private JsonIO hourlyVotersJsonIO;
    private JsonIO hourlyBandsJsonIO;
    private JsonIO daysJsonIO;
    private JsonIO commentsJsonIO;

    private ArrayList<Voter> voters;
    private ArrayList<Voter> bands;

    private Handler hourlyCheckHandler;
    private boolean isHourlyCheckRunning;
    private boolean isBandsAndVotersConnected;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e("MainExtras", "null");
            extras = new Bundle();
            day = new Day((String) Day.defValues[0], (String) Day.defValues[1], (String) Day.defValues[2], (int) Day.defValues[3], (int) Day.defValues[4], (int) Day.defValues[5]);
            daysJsonIO = new JsonIO(this.getFilesDir(), Day.DAYS_PATH, Day.ARRAY_KEY, true);
        }
        else {
            Log.i("MainExtras", "not null");
            day = (Day) extras.getSerializable("day");
            daysJsonIO = (JsonIO) ((ObjectWrapperForBinder)extras.getBinder("daysJsonIO")).getData();
        }

        //Must not trigger
        if (day == null) {
            day = new Day((String) Day.defValues[0], (String) Day.defValues[1], (String) Day.defValues[2], (int) Day.defValues[3], (int) Day.defValues[4], (int) Day.defValues[5]);
            Log.e("Counter", "No day provided");
        }
        Log.i("Counter", day.toString());

        //Init voters and bands variables, setting view
        if (day.getMode() == Day.PRESENCE) {
            setContentView(R.layout.main_counter2);
            initVoters(votersNumbers, extras.getInt("totalVoters", 0));
        }
        else if (day.getMode() == Day.BANDS) {
            setContentView(R.layout.main_counter2);
            initBands(bandsNumbers, extras.getInt("totalBands", 0));
        }
        else if (day.getMode() == Day.PRESENCE_BANDS) {
            setContentView(R.layout.joint_main_counter);
            initVoters(votersNumbers, extras.getInt("totalVoters", 0));
            initBands(bandsNumbers, extras.getInt("totalBands", 0));

            voteButtonSecond = (TextView) findViewById(R.id.count_ribbons);
            deleteLastButtonSecond = (LinearLayout) findViewById(R.id.delete_ribbon_button);
        }

        preferencesIO = new PreferencesIO(this);
        isBandsAndVotersConnected = preferencesIO.getBoolean(PreferencesIO.IS_BANDS_AND_VOTERS_CONNECTED, true);

        voteButtonMain = (TextView) findViewById(R.id.count);
        deleteLastButtonMain = (LinearLayout) findViewById(R.id.delete_button);
        commentButton = (LinearLayout) findViewById(R.id.mark_button);

        //Creating fragments for voters numbers and view pager
        viewPager = findViewById(R.id.pager);
        initPager();

        //Yik number initialization
        yikNumber = (TextView) findViewById(R.id.precinct_id);
        if (day.getYik() != null)
            yikNumber.setText(String.format("%s%s", getString(R.string.yik_prefix), day.getYik()));
        else
            yikNumber.setText(R.string.yik_placeholder);

        //Day's date initialization
        TextView electionStatus = (TextView) findViewById(R.id.election_status);
        if (day.getFormattedDate() != null)
            electionStatus.setText(day.getFormattedDate());
        else
            electionStatus.setText("01.01.1970");

        //Creating JsonIO for comments writing
        commentsJsonIO = new JsonIO(getFilesDir(), Comment.COMMENTS_PATH, Comment.ARRAY_KEY, JsonIO.MODE.WRITE_ONLY_EOF, false);

        //Timer for checking votes those are one hour old
        hourlyCheckHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (isHourlyCheckRunning) {
                    if ((day.getMode() & Day.PRESENCE) > 0) {
                        checkVotesHourly(voters, votersNumbers);
                    }
                    if ((day.getMode() & Day.BANDS) > 0) {
                        checkVotesHourly(bands, bandsNumbers);
                    }
                    hourlyCheckHandler.sendEmptyMessageDelayed(0, 60000);
                }
            }
        };

        //Setting onClick for main add button (for ribbons if count only ribbons; for voters if count only voters or both)
        voteButtonMain.setOnClickListener(view -> {
            doVibration();
            if (day.getMode() == Day.PRESENCE || day.getMode() == Day.PRESENCE_BANDS) {
                onAddRecord(voters, votersJsonIO, hourlyVotersJsonIO, votersNumbers, Day.PRESENCE);
            }
            else if (day.getMode() == Day.BANDS) {
                onAddRecord(bands, bandsJsonIO, hourlyBandsJsonIO, bandsNumbers, Day.BANDS);
            }
        });

        //Setting onClick for main delete button (for ribbons if count only ribbons; for voters if count only voters or both)
        deleteLastButtonMain.setOnClickListener(view -> {
            doDeleteVibration();
            if (day.getMode() == Day.PRESENCE || day.getMode() == Day.PRESENCE_BANDS) {
                onDeleteLastRecord(voters, votersJsonIO, hourlyVotersJsonIO, votersNumbers, Day.PRESENCE);
            }
            else if (day.getMode() == Day.BANDS) {
                onDeleteLastRecord(bands, bandsJsonIO, hourlyBandsJsonIO, bandsNumbers, Day.BANDS);
            }
        });

        //Setting onClick for secondary buttons (for ribbons) if count both
        if (day.getMode() == Day.PRESENCE_BANDS) {
            voteButtonSecond.setOnClickListener(view -> {
                doVibration();
                onAddRecord(bands, bandsJsonIO, hourlyBandsJsonIO, bandsNumbers, Day.BANDS);
                if (isBandsAndVotersConnected) {
                    onAddRecord(voters, votersJsonIO, hourlyVotersJsonIO, votersNumbers, Day.PRESENCE);
                }
            });

            deleteLastButtonSecond.setOnClickListener(view -> {
                doDeleteVibration();
                if (voters.size() > 0 && bands.size() > 0 && isBandsAndVotersConnected) {
                    Voter bandToBeDeleted = bands.get(bands.size() - 1);
                    onDeleteConnectedRecord(bandToBeDeleted, voters, votersJsonIO, hourlyVotersJsonIO, votersNumbers, Day.PRESENCE);
                }
                onDeleteLastRecord(bands, bandsJsonIO, hourlyBandsJsonIO, bandsNumbers, Day.BANDS);
            });
        }

        // Add dialogs for editing yik number and adding comments
        setUpCommentAndYikEditDialogs();

        LinearLayout info = (LinearLayout) findViewById(R.id.info_button);
        LinearLayout done = (LinearLayout) findViewById(R.id.finish_button);
        ImageView doneIcon = (ImageView) findViewById(R.id.finish_icon);
        TextView doneLabel = (TextView) findViewById(R.id.finish_label);
        ImageView infoIcon = (ImageView) findViewById(R.id.info_icon);
        TextView infoLabel = (TextView) findViewById(R.id.info_label);

        //On info click goes to Details (Graphs and list)
        View.OnClickListener infoListener = view -> onInfoClick();
        info.setOnClickListener(infoListener);
        infoIcon.setOnClickListener(infoListener);
        infoLabel.setOnClickListener(infoListener);

        //On done click goes to menu
        View.OnClickListener doneListener = view -> onDoneClick();
        doneIcon.setOnClickListener(doneListener);
        doneLabel.setOnClickListener(doneListener);
        done.setOnClickListener(doneListener);
    }

    private void setUpCommentAndYikEditDialogs() {
        //Write comment function (see OnActivityResult)
        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditTextDialog.class);
                final Bundle bundle = new Bundle();
                bundle.putString(EditTextDialog.textHintExtras, getString(R.string.comment_hint));
                bundle.putInt(EditTextDialog.textInputTypeExtras, InputType.TYPE_CLASS_TEXT);
                bundle.putInt(EditTextDialog.textMaxLengthExtras, 500);
                intent.putExtras(bundle);
                startActivityForResult(intent, CREATE_COMMENT_REQUEST);
            }
        });

        //Change yik number function (see OnActivityResult)
        yikNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditTextDialog.class);
                final Bundle bundle = new Bundle();
                bundle.putString(EditTextDialog.textHintExtras, getString(R.string.yik_hint));
                bundle.putInt(EditTextDialog.textInputTypeExtras, InputType.TYPE_CLASS_NUMBER);
                bundle.putInt(EditTextDialog.textMaxLengthExtras, 4);
                intent.putExtras(bundle);
                startActivityForResult(intent, EDIT_YIK_REQUEST);
            }
        });
    }

    private void initPager() {
        dailyInfoFragment = new DailyInfoFragment();
        additionalInfoFragment = new AdditionalInfoFragment();

        Bundle fragmentBundle = new Bundle();

        if ((day.getMode() & Day.PRESENCE) > 0) {
            dailyInfoFragment.bindNumbers(votersNumbers);
            additionalInfoFragment.bindNumbers(votersNumbers);
            fragmentBundle.putStringArray("labels", new String[] {getString(R.string.voted_today), getString(R.string.last_hour), getString(R.string.grand_total)});
            //checking last hour votes. It's not really good because on startup it is performed twice (see onResume). But it's needed for fragments initialization
            checkVotesHourly(voters, votersNumbers);
        }
        if ((day.getMode() & Day.BANDS) > 0) {
            dailyInfoFragment.bindNumbers(bandsNumbers);
            additionalInfoFragment.bindNumbers(bandsNumbers);
            fragmentBundle.putStringArray("labels", new String[] {getString(R.string.bands_today), getString(R.string.bands_last_hour), getString(R.string.bands_total)});
            checkVotesHourly(bands, bandsNumbers);
        }
        fragmentBundle.putInt("mode", day.getMode());

        UniversalPagerAdapter universalPagerAdapter = new UniversalPagerAdapter(this, getSupportFragmentManager(),
                new Fragment[] {dailyInfoFragment, additionalInfoFragment},
                new String[] {getString(R.string.this_day), getString(R.string.other)},
                fragmentBundle);
        viewPager.setAdapter(universalPagerAdapter);
    }

    private void onDeleteConnectedRecord(Voter connectedVoter, ArrayList<Voter> records, JsonIO recordsJsonIO, JsonIO hourlyRecordsJsonIO, Numbers numbers, @Day.Position int position) {
        try {
            int index = recordsJsonIO.getIndexOfObject(Voter.timeKey, connectedVoter.getFormattedTime(), Voter.ARRAY_KEY);
            records.remove(index);
            recordsJsonIO.deleteAt(index, Voter.ARRAY_KEY);
            delete(connectedVoter.getFormattedTime(), records, recordsJsonIO, hourlyRecordsJsonIO, numbers, position);
        } catch (JsonIO.ObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onDeleteLastRecord(ArrayList<Voter> records, JsonIO recordsJsonIO, JsonIO hourlyRecordsJsonIO, Numbers numbers, @Day.Position int position) {
        if (records.size() > 0) {
            recordsJsonIO.deleteLast(Voter.ARRAY_KEY);
            Voter deletedVoter = records.remove(records.size() - 1);

            delete(deletedVoter.getFormattedTime(), records, recordsJsonIO, hourlyRecordsJsonIO, numbers, position);
        }
    }

    private void delete(String formattedTime, ArrayList<Voter> records, JsonIO recordsJsonIO, JsonIO hourlyRecordsJsonIO, Numbers numbers, @Day.Position int position) {

        //Update number of votes in TextViews
        numbers.changeAll(-1);

        //Updating this day voters number in file
        try {
            day = day.getDayWithChanged(numbers.getDaily(), position);
            daysJsonIO.replaceObject(day.toJSONObject(), Day.nameKey, day.getName(), Day.ARRAY_KEY);
        } catch (JsonIO.ObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Updating list with voters separated on hour basis (for graphs)
        String hourString = Hour.extractHourFromTime(formattedTime);
        Log.i("HourlyFinal", hourString);
        try {

            int index = hourlyRecordsJsonIO.getIndexOfObject(Hour.hourKey, hourString, Hour.ARRAY_KEY);
            JSONObject object = hourlyRecordsJsonIO.searchObjectAtIndex(index, Hour.ARRAY_KEY);
            if (object.getInt(Hour.countKey) - 1 > 0) {
                object.put(Hour.countKey, object.getInt(Hour.countKey) - 1);
                hourlyRecordsJsonIO.replaceObjectAtIndex(object, index, Hour.ARRAY_KEY);
            }
            else  {
                hourlyRecordsJsonIO.deleteAt(index, Hour.ARRAY_KEY);
            }

        } catch (JSONException | JsonIO.ObjectNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onAddRecord(ArrayList<Voter> records, JsonIO recordsJsonIO, JsonIO hourlyRecordsJsonIO, Numbers numbers, @Day.Position int position) {
        //Changing number of votes in TextViews
        numbers.changeAll(1);

        //Creating new voter
        Voter newVoter = new Voter(System.currentTimeMillis(), numbers.getDaily());
        //Adding it to the list
        records.add(newVoter);
        //Writing new voter to the end of json file
        recordsJsonIO.writeToEndOfFile(newVoter.toJSONObject());

        Realm.insertVoter(newVoter);

        //Updating this day voters number in file
        try {
            day = day.getDayWithChanged(numbers.getDaily(), position);
            daysJsonIO.replaceObject(day.getDayWithChanged(numbers.getDaily(), position).toJSONObject(), Day.nameKey, day.getName(), Day.ARRAY_KEY);
        } catch (JsonIO.ObjectNotFoundException e) {
            e.printStackTrace();
        }

        //Updating list with voters separated on hour basis (for graphs)
        String hourString = Hour.extractHourFromTime(newVoter.getFormattedTime());
        //Log.i("HourlyFinal", hourString);
        try {
            int index = hourlyRecordsJsonIO.getIndexOfObject(Hour.hourKey, hourString, Hour.ARRAY_KEY);
            JSONObject object = hourlyRecordsJsonIO.searchObjectAtIndex(index, Hour.ARRAY_KEY);
            object.put(Hour.countKey, object.getInt(Hour.countKey) + 1);
            hourlyRecordsJsonIO.replaceObjectAtIndex(object, index, Hour.ARRAY_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JsonIO.ObjectNotFoundException e) {
            hourlyRecordsJsonIO.writeToEndOfFile(new Hour(hourString, 1).toJSONObject());
        }
    }

    private void initVoters(Numbers numbers, int totalCount) {
        String votersJsonPath = day.getName() + getString(R.string.voters_suffix) + getString(R.string.json_postfix);
        String hourlyVotersJsonPath = day.getName() + getString(R.string.voters_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix);
        //Reading from file on startup, init daily votes number
        votersJsonIO = new JsonIO(this.getFilesDir(), votersJsonPath, Voter.ARRAY_KEY, true);
        voters = votersJsonIO.parseJsonArray(false, new ArrayList<Voter>(), true, Voter.ARRAY_KEY, Voter.class, Voter.constructorArgs, Voter.jsonKeys, null);

        numbers.setTotal(totalCount);
        numbers.setDaily(voters.size());

        //Creating jsonIO for further distributing voters by their hours
        hourlyVotersJsonIO = new JsonIO(this.getFilesDir(), hourlyVotersJsonPath, Hour.ARRAY_KEY, true);
    }

    private void initBands(Numbers numbers, int totalCount) {
        String bandsJsonPath = day.getName() + getString(R.string.bands_suffix) + getString(R.string.json_postfix);
        String hourlyBandsJsonPath = day.getName() + getString(R.string.bands_suffix) + getString(R.string.hourly_suffix) + getString(R.string.json_postfix);
        //Reading from file on startup, init daily bands number
        bandsJsonIO = new JsonIO(this.getFilesDir(), bandsJsonPath, Voter.ARRAY_KEY, true);
        bands = bandsJsonIO.parseJsonArray(false, new ArrayList<Voter>(), true, Voter.ARRAY_KEY, Voter.class, Voter.constructorArgs, Voter.jsonKeys, null);

        numbers.setTotal(totalCount);
        numbers.setDaily(bands.size());

        //Creating jsonIO for further distributing voters by their hours
        hourlyBandsJsonIO = new JsonIO(this.getFilesDir(), hourlyBandsJsonPath, Hour.ARRAY_KEY, true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case EDIT_YIK_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("textResult")) {
                        if (!data.getStringExtra("textResult").equals("")) {
                            day.setYik(data.getStringExtra("textResult"));
                            yikNumber.setText(String.format("%s%s", getString(R.string.yik_prefix), day.getYik()));
                            try {
                                daysJsonIO.replaceObject(day.toJSONObject(), Day.nameKey, day.getName(), Day.ARRAY_KEY);
                            } catch (JsonIO.ObjectNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            yikNumber.setText(getString(R.string.yik_placeholder));
                    }
                }
                break;
            }
            case CREATE_COMMENT_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("textResult")) {
                        if (!data.getStringExtra("textResult").equals("")) {
                            commentsJsonIO.writeToEndOfFile(new Comment(System.currentTimeMillis(), data.getStringExtra("textResult")).toJSONObject());
                        }
                    }
                }
            }
        }
    }

    private void onDoneClick() {
        finish();
    }

    private void onInfoClick() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("day", day);
        if ((day.getMode() & Day.PRESENCE) > 0) {
            bundle.putBinder("voters", new ObjectWrapperForBinder(voters));
            bundle.putInt("totalVoters", votersNumbers.getTotally());
        }
        if ((day.getMode() & Day.BANDS) > 0) {
            bundle.putBinder("bands", new ObjectWrapperForBinder(bands));
            bundle.putInt("totalBands", bandsNumbers.getTotally());
        }

        Intent intent = new Intent(getApplicationContext(), Details.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Turns off buttons if current date doesn't equals session date and stops hourly check
        //TODO: make it date change indifferent
        if (!day.getFormattedDate().equals(DateFormatter.formatDateDefaultPattern(System.currentTimeMillis()))) {
            voteButtonMain.setClickable(false);
            voteButtonMain.setText(R.string.end_voting);
            deleteLastButtonMain.setClickable(false);

            if ((day.getMode() & Day.PRESENCE) > 0) {
                votersNumbers.setHourly(0);
            }
            if ((day.getMode() & Day.BANDS) > 0) {
                bandsNumbers.setHourly(0);
            }
            if ((day.getMode() & Day.PRESENCE_BANDS) > 0) {
                voteButtonSecond.setClickable(false);
                voteButtonSecond.setText(R.string.end_voting);
                deleteLastButtonSecond.setClickable(false);
            }

            isHourlyCheckRunning = false;
            hourlyCheckHandler.removeMessages(0);
        }
        //Else checks hourly votes
        else {
            isHourlyCheckRunning = true;
            hourlyCheckHandler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Stops hourly check
        isHourlyCheckRunning = false;
        hourlyCheckHandler.removeMessages(0);
    }

    // Checking how much votes made last hour
    private void checkVotesHourly(ArrayList<Voter> records, Numbers numbers) {
        final long HOUR = 1000 * 60 * 60;
        int lastHourCount = 0;
        Log.i("check", String.valueOf(records.size()));

        long currentTime = System.currentTimeMillis();
        for (int i = records.size() - 1; i >= 0; i--) {
            long difference = currentTime - records.get(i).getTimestamp();
            if (difference > 0 && difference < HOUR) {
                lastHourCount++;
            } else {
                break;
            }
        }

        numbers.setHourly(lastHourCount);
    }

    private void doVibration() {
        final long[] durations = {500, 250, 100, 50, 0};

        int vibeId = preferencesIO.getInt(PreferencesIO.VIBE_RADIOBUTTON_INDEX, 2);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibe == null) {
            Log.e("Vibrator", "Can't get vibrator service.");
            return;
        } else if (vibeId < 0 || vibeId >= durations.length) {
            Log.e("Vibrator", "Vibe id is out of bounds.");
            return;
        }

        if (durations[vibeId] > 0) {
            vibe.vibrate(durations[vibeId]);
        }
    }

    private void doDeleteVibration(){
        final long[][] patterns = {{0, 300, 150, 150},
                {0, 200, 100, 100},
                {0, 100, 75, 75},
                {0, 50, 50, 50},
                {0, 0, 0, 0}};

        int vibeId = preferencesIO.getInt(PreferencesIO.VIBE_RADIOBUTTON_INDEX, 2);
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibe == null) {
            Log.e("Vibrator", "Can't get vibrator service.");
            return;
        } else if (vibeId < 0 || vibeId >= patterns.length) {
            Log.e("Vibrator", "Vibe id is out of bounds.");
            return;
        }

        if (patterns[vibeId][1] > 0) {
            vibe.vibrate(patterns[vibeId], 1);
        }
    }

    public ViewPager getPager(){
        return viewPager;
    }

}