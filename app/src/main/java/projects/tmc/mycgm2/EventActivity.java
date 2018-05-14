package projects.tmc.mycgm2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import java.util.List;

public class EventActivity extends MainActivity {

    private List<EventItem> mEventItems;
    private Fragment mExerciseFragment;
    private String HEALTH_STRING = "HEALTH";
    private String EXERCISE_STRING = "EXERCISE";


    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_exercise:
                    updateFragment(ExerciseFragment.newInstance());
                    return true;
                case R.id.navigation_carbs:
                    updateFragment(CarbFragment.newInstance());
                    return true;
                case R.id.navigation_insulin:
                    updateFragment(InsulinFragment.newInstance());
                    return true;
                case R.id.navigation_health:
                    updateFragment(HealthFragment.newInstance());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void updateFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
