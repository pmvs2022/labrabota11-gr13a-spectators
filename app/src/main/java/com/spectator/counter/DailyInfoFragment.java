package com.spectator.counter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.spectator.R;
import com.spectator.data.Day;
import com.spectator.utils.PreferencesIO;


public class DailyInfoFragment extends Fragment {

    private int mode;
    private TextView thisDay;
    private TextView thisDay2;
    private VerticalViewPager viewPager;
    private boolean isFirstTime;
    private PreferencesIO preferencesIO;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daily_info_fragment, container, false);
        viewPager = (VerticalViewPager) ((MainCounterScreen)this.getActivity()).getPager();

        Bundle extras = getArguments();
        if (extras == null) {
            Log.e("DailyInfoExtras", "null");
        }
        else {
            Log.i("DailyInfoExtras", "not null");

            if (extras.containsKey("mode"))
                mode = extras.getInt("mode");
            else {
                Log.e("DailyInfoExtras", "No mode key");
            }

            if (mode == Day.BANDS || mode == Day.PRESENCE) {
                String[] labels = extras.getStringArray("labels");
                view = inflater.inflate(R.layout.daily_info_fragment, container, false);

                thisDay = view.findViewById(R.id.daily_amount);
                TextView thisDayLabel = view.findViewById(R.id.daily_label);

                thisDayLabel.setText(labels[0]);
                thisDay.setText(String.valueOf(extras.getInt("daily")));
            } else if (mode == Day.PRESENCE_BANDS) {
                //Labels will be null
                view = inflater.inflate(R.layout.joint_daily_info_fragment, container, false);

                thisDay = view.findViewById(R.id.votes_counter_amount);
                thisDay2 = view.findViewById(R.id.ribbons_counter);

                thisDay.setText(String.valueOf(extras.getInt("daily")));
                thisDay2.setText(String.valueOf(extras.getInt("daily2")));
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesIO = new PreferencesIO(view.getContext());
        isFirstTime = preferencesIO.getBoolean(PreferencesIO.IS_FIRST_TIME, true);
        final TextView swipeHint = view.findViewById(R.id.swipe_hint);

        if (isFirstTime) {
            swipeHint.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new VerticalViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (isFirstTime) {
                        preferencesIO.putBoolean(PreferencesIO.IS_FIRST_TIME, false);
                        swipeHint.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
        else {
            swipeHint.setVisibility(View.GONE);
        }

    }

    void setDaily(int daily, @Day.Position int position) {
        if (mode != Day.PRESENCE_BANDS) {
            if (thisDay != null)
                this.thisDay.setText(String.valueOf(daily));
        }
        else {
            if (thisDay != null && thisDay2 != null) {
                if (position == 0)
                    this.thisDay.setText(String.valueOf(daily));
                else if (position == 1)
                    this.thisDay2.setText(String.valueOf(daily));
            }
        }
    }

}

