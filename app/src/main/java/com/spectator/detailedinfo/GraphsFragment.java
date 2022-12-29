package com.spectator.detailedinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.spectator.R;
import com.spectator.data.Day;
import com.spectator.data.Hour;
import com.spectator.utils.JsonIO;

import java.io.File;
import java.util.ArrayList;

public class GraphsFragment extends Fragment {

    Day day;
    String pathPrefix;

    public GraphsFragment(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graphs_fragment, container, false);

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle extras = getArguments();
        if (extras == null) {
            Log.e("GraphsExtras", "null");
        }
        else {
            Log.i("GraphsExtras", "not null");
            if (extras.containsKey("day"))
                day = (Day) extras.getSerializable("day");
        }

        TextView exportData = (TextView) view.findViewById(R.id.export_data);
        exportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportData();
            }
        });

        TextView showComments = (TextView) view.findViewById(R.id.show_comments);
        showComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext().getApplicationContext(), ViewComments.class);
                startActivity(intent);
            }
        });

        ArrayList<Hour> hours = new ArrayList<>();
        ArrayList<BarEntry> voters = new ArrayList<>();

        JsonIO hourlyJsonIO = new JsonIO(getContext().getFilesDir(), pathPrefix + getString(R.string.hourly_suffix) + getString(R.string.json_postfix), Hour.ARRAY_KEY, false);
        hours = hourlyJsonIO.parseJsonArray(true, hours, true, Hour.ARRAY_KEY, Hour.class, Hour.constructorArgs, Hour.jsonKeys, null);

        BarChart barChart = (BarChart) view.findViewById(R.id.chart);

        //Disabling interaction with the graph
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
        barChart.setScaleEnabled(false);

        //To avoid clipping xAxis labels
        barChart.setExtraOffsets(0, 0, 0, 10);

        //Turning off description and legend
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        //Setting xAxis. Text size 13; label color white; min step between labels is 1; draw labels at center of grid lines; turn off grid lines drawing.
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setCenterAxisLabels(true);
        xAxis.setTextSize(14);
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(24);
        xAxis.setAvoidFirstLastClipping(true);

        //Setting yAxis. Start at 0; min step between labels is 1; label color white.
        YAxis yAxisLeft = barChart.getAxisLeft();
        yAxisLeft.setTextColor(getResources().getColor(R.color.white));
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setTextSize(12);
        YAxis yAxisRight = barChart.getAxisRight();
        yAxisRight.setTextColor(getResources().getColor(R.color.white));
        yAxisRight.setAxisMinimum(0f);
        yAxisRight.setGranularity(1f);
        yAxisRight.setEnabled(false);

        settingValues(hours, voters);
        BarDataSet bardataset = new BarDataSet(voters, "voters");
        //Setting color of bars
        bardataset.setColors(getResources().getColor(R.color.colorAccentDark));
        BarData data = new BarData(bardataset);
        //Animation (bars rising on start) duration
        barChart.animateY(2000);
        //Turning on values above bars and setting value formatter
        data.setDrawValues(true);
        data.setValueTextColor(getResources().getColor(R.color.white));
        data.setValueTextSize(18);
        data.setValueFormatter(new CustomValueFormatter());

        barChart.setData(data);

    }

    private void exportData() {
        File file = new File(getContext().getFilesDir(), pathPrefix + getString(R.string.json_postfix));
        Uri uri = FileProvider.getUriForFile(getContext(), "com.spectator.fileProvider", file);

        Intent exportingIntent = new Intent(android.content.Intent.ACTION_SEND);
        exportingIntent.setType("application/json");
        exportingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        exportingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, day.getName() + "(" + day.getFormattedDate() + ") " + getString(R.string.spectator_list));
        exportingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(exportingIntent, getString(R.string.export_via)));
    }

    private void settingValues(ArrayList<Hour> hours, ArrayList<BarEntry> voters) {
        //Offset for xAxis to make bars between hour x and x+1, and not between x-1 and x
        int xOffset = 1;
        float minValue = 0.15f;

        //Finding min and max value of Hour String [0;23]
        int min = 25; int max = -1; int curValue;
        for (int i = 0; i < hours.size(); i++) {
            curValue = Integer.parseInt(hours.get(i).getHour());
            if (curValue > max) {
                max = curValue;
            }
            if (curValue < min) {
                min = curValue;
            }
        }
        //Filling gaps between min and max
        if (min != 25 && max != -1) {
            for (int i = min + 1; i < max; i++) {
                voters.add(new BarEntry(i + xOffset, minValue));
            }
        }
        //Setting data from hours array
        for (int i = 0; i < hours.size(); i++) {
            voters.add(new BarEntry(Integer.parseInt(hours.get(i).getHour()) + xOffset, hours.get(i).getCount()));
        }
    }

    private static class CustomValueFormatter extends ValueFormatter {
        @Override
        public String getBarLabel(BarEntry barEntry) {
            if (barEntry.getY() >= 1)
                return String.valueOf((int)barEntry.getY());
            else
                return "";
        }
    }
}