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

import java.util.ArrayList;

public class AdditionalInfoFragment extends Fragment {

    private int mode;

    private TextView grandTotal;
    private TextView[] lastHour = new TextView[2];
    private final ArrayList<Numbers> bindedNumbers = new ArrayList<>();

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
                lastHour[0] = view.findViewById(R.id.hourly_amount);
                TextView grandTotalLabel = view.findViewById(R.id.total_label);
                TextView lastHourLabel = view.findViewById(R.id.hourly_label);

                lastHourLabel.setText(labels[1]);
                grandTotalLabel.setText(labels[2]);

                bindedNumbers.get(0).setHourlyTextView(lastHour[0]);
                bindedNumbers.get(0).setTotalTextView(grandTotal);
            } else if (mode == Day.PRESENCE_BANDS) {
                //Labels and totally will be null
                view = inflater.inflate(R.layout.joint_additional_info_fragment, container, false);

                lastHour[0] = view.findViewById(R.id.votes_last_hour_amount);
                lastHour[1] = view.findViewById(R.id.ribbons_last_hour_amount);

                for (int i = 0; i < Integer.min(bindedNumbers.size(), lastHour.length); i++) {
                    bindedNumbers.get(i).setHourlyTextView(lastHour[i]);
                }
            }
        }
        return view;
    }

    void bindNumbers(Numbers numbers) {
        bindedNumbers.add(numbers);
    }
}
