package com.duckma.androidibeaconwear;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;


public class ShowBeacon extends ActionBarActivity {
    public static final String TAG = ShowBeacon.class.getSimpleName();

    private Beacon beacon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beacon = getIntent().getParcelableExtra(MainActivity.BEACON_EXTRA_KEY);
        if(beacon == null){
            finish();
        }
        setContentView(R.layout.activity_show_beacon);
        ((TextView) findViewById(R.id.tv1)).setText("Beacon Found!");
        ((TextView) findViewById(R.id.tv2)).setText(beacon.getId1().toString());
        ((TextView) findViewById(R.id.tv3)).setText("d: " + String.valueOf(beacon.getDistance()) + " m");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
