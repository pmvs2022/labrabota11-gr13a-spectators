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

public class AdditionalInfoFragment extends Fragment {

    private int mode;
    private TextView grandTotal;
    private TextView lastHour;
    private TextView lastHour2;
    private VerticalViewPager viewPager;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        viewPager = (VerticalViewPager) ((MainCounterScreen) this.getActivity()).getPager();

        Bundle extras = getArguments();
        if (extras == null) {
            Log.e("DetailedInfoExtras", "null");
        }
        else {
            Log.i("DetailedInfoExtras", "not null");

            if (extras.containsKey("mode"))
                mode = extras.getInt("mode");
            else {
                Log.e("DetailedInfoExtras", "No mode key");
            }

            if (mode == Day.BANDS || mode == Day.PRESENCE) {
                String[] labels = extras.getStringArray("labels");
                view = inflater.inflate(R.layout.additional_info_fragment, container, false);

                grandTotal = view.findViewById(R.id.total_amount);
                lastHour = view.findViewById(R.id.hourly_amount);
                TextView grandTotalLabel = view.findViewById(R.id.total_label);
                TextView lastHourLabel = view.findViewById(R.id.hourly_label);

                lastHourLabel.setText(labels[1]);
                grandTotalLabel.setText(labels[2]);
                grandTotal.setText(String.valueOf(extras.getInt("totally")));
                lastHour.setText(String.valueOf(extras.getInt("hourly")));

            } else if (mode == Day.PRESENCE_BANDS) {
                //Labels and totally will be null
                view = inflater.inflate(R.layout.joint_additional_info_fragment, container, false);

                lastHour = view.findViewById(R.id.votes_last_hour_amount);
                lastHour2 = view.findViewById(R.id.ribbons_last_hour_amount);

                lastHour.setText(String.valueOf(extras.getInt("hourly")));
                lastHour2.setText(String.valueOf(extras.getInt("hourly2")));
            }
        }

        return view;
    }

    void setTotally(int totally) {
        if (mode != Day.PRESENCE_BANDS)
            if (grandTotal != null)
                this.grandTotal.setText(String.valueOf(totally));
    }

    void setHourly(int hourly, @Day.Position int position) {
        if (mode != Day.PRESENCE_BANDS) {
            if (lastHour != null)
                this.lastHour.setText(String.valueOf(hourly));
        }
        else {
            if (lastHour != null && lastHour2 != null) {
                if (position == 0)
                    this.lastHour.setText(String.valueOf(hourly));
                else if (position == 1)
                    this.lastHour2.setText(String.valueOf(hourly));
            }
        }
    }
}
