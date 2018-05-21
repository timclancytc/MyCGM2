package projects.tmc.mycgm2;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button glucoseButton = findViewById(R.id.glucose_button);
        glucoseButton.setOnClickListener(v -> {
            Intent startGlucoseActivity = new Intent(MainActivity.this, GlucoseActivity.class);
            MainActivity.this.startActivity(startGlucoseActivity);
        });


        Button eventsButton = findViewById(R.id.events_button);
        eventsButton.setOnClickListener(v -> {
            Intent startEventActivity = new Intent(MainActivity.this, EventActivity.class);
            MainActivity.this.startActivity(startEventActivity);
        });


        //Display a welcome message
        //Fetch the datasets and store them (in a db? as a singleton?)
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_goto_events:
                //intent to start activity_event
                Intent startEventActivity = new Intent(MainActivity.this, EventActivity.class);
                MainActivity.this.startActivity(startEventActivity);
                return true;
            case R.id.action_goto_glucose:
                //intent to start activity_glucose
                Intent startGlucoseActivity = new Intent(MainActivity.this, GlucoseActivity.class);
                MainActivity.this.startActivity(startGlucoseActivity);
                return true;
            case R.id.action_refresh:
                MyCGMLab.get().refreshItems(
                        (MyCGMLab.OnRefreshItemsListener) mFragment
                );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void updateFragment(Fragment fragment) {
        mFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
