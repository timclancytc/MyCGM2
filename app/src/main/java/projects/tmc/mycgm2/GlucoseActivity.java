package projects.tmc.mycgm2;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;

public class GlucoseActivity extends MainActivity {

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_glucose_calibrations:
                        updateFragment(CalibrationsFragment.newInstance());
                        return true;
                    case R.id.navigation_glucose_egvs:
                        updateFragment(EGVSFragment.newInstance());
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucose);

        BottomNavigationView navigation = findViewById(R.id.navigation_exercise);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_glucose_calibrations);
    }


}
