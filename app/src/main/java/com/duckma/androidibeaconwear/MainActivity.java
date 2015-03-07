package com.duckma.androidibeaconwear;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

public class MainActivity extends ActionBarActivity implements BeaconConsumer {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String BEACON_EXTRA_KEY = "beacon_key";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String UNIQUE_ID = "com.example.myapp.boostrapRegion";
    private static final String PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private int notificationId = 101;
    private boolean notified;
    private BeaconManager beaconManager;
    private Region mAllBeaconsRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAllBeaconsRegion = new Region(UNIQUE_ID, Identifier.parse(PROXIMITY_UUID), null, null);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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


    @Override
    public void onBeaconServiceConnect() {

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "I just saw a beacon named " + region.getUniqueId() + " for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(TAG, "I no longer see a beacon named " + region.getUniqueId());

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d(TAG, "I have just switched from seeing/not seeing beacons: ");

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, final Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (final Beacon beacon : beacons) {
                            Log.d(TAG, "Ibeacon found");
                            if (!notified) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendNotificationWear(beacon);
                                    }
                                }, 1000 * 10);
                                notified = true;
                            }
                        }
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(mAllBeaconsRegion);
        } catch (RemoteException e) {
            Log.e(TAG, "No start Monitoring iBeacon");
        }
    }

    public void sendNotificationWear(Beacon beacon) {
        Log.d(TAG, "Rise Notification");

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender();

        // Build intent for notification content
        Intent viewIntent = new Intent(this, ShowBeacon.class);
        viewIntent.putExtra(BEACON_EXTRA_KEY, beacon);

        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.ibeacon_found))
                        .setContentText(getString(R.string.ibeacon_found_text, beacon.getId1(), beacon.getDistance()))
                        .setContentIntent(viewPendingIntent)
                        .setSound(alarmSound)
                        .extend(wearableExtender);


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void isBluetoothEnabled() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, REQUEST_ENABLE_BT);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isBluetoothEnabled();
    }
}
