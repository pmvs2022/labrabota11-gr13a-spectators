package com.spectator.detailedinfo;

import android.app.Notification;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.data.Day;
import com.spectator.data.Voter;
import com.spectator.notifications.Notifications;
import com.spectator.utils.ObjectWrapperForBinder;
import com.spectator.utils.UniversalPagerAdapter;

import java.util.ArrayList;

public class Details extends BaseActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Day day = null;
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e("DetailsExtras", "null");
        } else {
            Log.i("DetailsExtras", "not null");
            if (extras.containsKey("day")) {
                day = (Day) extras.getSerializable("day");
            } else Log.e("DetailsExtras", "Day key not found");
        }

        UniversalPagerAdapter universalPagerAdapter = createAdapter(day, extras);

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(universalPagerAdapter);
        tabs.setupWithViewPager(viewPager);

        Notification notification = Notifications.getNotification(this);
        Notifications.showNotification(this, notification);
    }

    private UniversalPagerAdapter createAdapter(Day day, Bundle extras) {
        Bundle fragmentExtras = new Bundle();
        fragmentExtras.putSerializable("day", day);

        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();

        if ((day.getMode() & Day.PRESENCE) > 0) {
            if (extras.containsKey("voters") && extras.containsKey("totalVoters")) {
                fragments.add(new GraphsFragment(day.getName() + getString(R.string.voters_suffix)));
                fragments.add(new ListFragment(
                        (ArrayList<Voter>) ((ObjectWrapperForBinder) extras.getBinder("voters")).getData(),
                        extras.getInt("totalVoters")));
                strings.add(getString(R.string.graphs) + "\n" + getString(R.string.voters));
                strings.add(getString(R.string.list) + "\n" + getString(R.string.voters));
            } else Log.e("Details", "No voters key in extras, Mode: " + day.getMode());

        }
        if ((day.getMode() & Day.BANDS) > 0) {
            if (extras.containsKey("bands") && extras.containsKey("totalBands")) {
                fragments.add(new GraphsFragment(day.getName() + getString(R.string.bands_suffix)));
                fragments.add(new ListFragment(
                        (ArrayList<Voter>) ((ObjectWrapperForBinder) extras.getBinder("bands")).getData(),
                        extras.getInt("totalBands")));
                strings.add(getString(R.string.graphs) + "\n" + getString(R.string.bands));
                strings.add(getString(R.string.list) + "\n" + getString(R.string.bands));
            } else Log.e("Details", "No bands key in extras, Mode: " + day.getMode());
        }

        return new UniversalPagerAdapter(this, getSupportFragmentManager(),
                fragments.toArray(new Fragment[0]),
                strings.toArray(new String[0]),
                fragmentExtras);
    }

}
