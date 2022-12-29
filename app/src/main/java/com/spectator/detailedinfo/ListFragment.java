package com.spectator.detailedinfo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.spectator.R;
import com.spectator.data.Voter;

import java.util.ArrayList;

public class ListFragment extends Fragment {

    private int totally = 0;
    private int daily = 0;
    private int hourly = 0;
    private TextView total;
    private TextView lastHour;
    private TextView thisDay;
    private ArrayList<Voter> records;
    private ScrollView scrollView;
    private LinearLayout scrollList;
    private boolean isPrevWhite = false;
    private LayoutInflater rowInflater;
    private Context context;

    public ListFragment(ArrayList<Voter> records, int totally) {
        this.totally = totally;
        if (records != null)
            this.records = records;
        else
            Log.e("ListFragment", "records array is null");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        context = getContext();
        total = (TextView) view.findViewById(R.id.total);
        thisDay = (TextView) view.findViewById(R.id.daily);
        lastHour = (TextView) view.findViewById(R.id.hourly);

        scrollView = (ScrollView) view.findViewById(R.id.votes);
        scrollList = (LinearLayout) view.findViewById(R.id.scroll_list);

        rowInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Initializing interface from voters array
        daily = 0;
        for (int j = 0; j < records.size(); j++) {
            LinearLayout newRow = makeNewRow(records.get(j));
            scrollList.addView(newRow);
            daily++;
        }
        thisDay.setText(String.valueOf(daily));
        total.setText(String.valueOf(totally));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Turns off vote button if current date doesn't equals session date
        //TODO: make it date change indifferent
        checkVotesHourly();
        lastHour.setText(String.valueOf(hourly));
    }

    //Making new Linear layout for new vote
    private LinearLayout makeNewRow(Voter printVoter) {
        LinearLayout linearLayout = (LinearLayout) rowInflater.inflate(R.layout.rows, null);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        linearLayout.setLayoutParams(layoutParams);

        //Making color contrast for neighbouring layouts
        if (isPrevWhite) {
            linearLayout.setBackgroundColor(context.getResources().getColor(R.color.list));
            isPrevWhite = false;
        }
        else {
            linearLayout.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundDark));
            isPrevWhite = true;
        }

        TextView newTimeView = linearLayout.findViewById(R.id.time);
        newTimeView.setText(printVoter.getFormattedTime());

        TextView newCountView = linearLayout.findViewById(R.id.count);
        newCountView.setText(String.valueOf(printVoter.getCount()));

        return linearLayout;
    }

    private void checkVotesHourly() {
        final long HOUR = 1000 * 60 * 60;
        hourly = 0;
        Log.e("check", String.valueOf(records.size()));
        for (int i = records.size() - 1; i >= 0; i--) {
            long currentTime = System.currentTimeMillis();
            long difference =  currentTime - records.get(i).getTimestamp();
            if (difference > 0 && difference < HOUR) {
                hourly++;
            }
            else {
                Log.e("hourly", String.valueOf(hourly));
                break;
            }
        }
    }

}
