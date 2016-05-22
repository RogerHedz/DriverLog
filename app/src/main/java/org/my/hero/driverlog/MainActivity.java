/*
 * Main class for Drivers Journal (K�rjournal)
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    private FragmentTabHost mTabHost;
    static long cnt = 0, oldcnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator(getString(R.string.main_tab1)), LogFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator(getString(R.string.main_tab2)), EditFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("tab3").setIndicator(getString(R.string.main_tab3)), MapFragment.class, null);

        // this is to ensure that the tabs fill screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int tabWidth = metrics.widthPixels / 3;
        for (int i = 0; i < 3; i++)
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().width = tabWidth;
    }

    // ---------------------------------------------------------------------------------------
    // Function to prevent closing the app by mistake
    // If you press "back" twice within three seconds the app exits
    // but if you are on another tab than the first you navigate to tab 0 first
    // ---------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        Calendar time = Calendar.getInstance();
        cnt = time.getTimeInMillis();

        if (cnt - oldcnt < 3000)
            finish();
        else {
            TabHost host = (TabHost) findViewById(android.R.id.tabhost);
            if (host.getCurrentTab() == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.main_exitwarning), Toast.LENGTH_SHORT).show();
                oldcnt = cnt;
            } else {
                host.setCurrentTab(0);
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Send position in a SMS
    // ---------------------------------------------------------------------------------------
    private void sendSMS(boolean nonumber, String phoneNumber, String message) {

        if (nonumber == true) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }

    // ---------------------------------------------------------------------------------------
    // onCreateOptionsMenu
    // Inflate the menu; this adds items to the action bar if it is present.
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ---------------------------------------------------------------------------------------
    // onOptionsItemSelected
    // Handles menu choices
    // ---------------------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Preferences.class));
                TabHost host = (TabHost) findViewById(android.R.id.tabhost);
                host.setCurrentTab(0);
                return true;
            case R.id.action_optsendsms:
                if (GpsService.getLastPos().equals("0,0")) { // No position yet
                    Toast.makeText(getApplicationContext(), getString(R.string.main_nopos), Toast.LENGTH_SHORT).show();
                    return true;
                } else if (Preferences.getSmsNumber(this).length() < 8) {
                    // no phone number in preferences - use the normal "send sms" application
                    Toast.makeText(getApplicationContext(), getString(R.string.main_nosmsnumber), Toast.LENGTH_SHORT).show();
                    sendSMS(true, Preferences.getSmsNumber(this), getString(R.string.main_googlemap1) + GpsService.getLastPos()
                            + getString(R.string.main_googlemap2) + GpsService.getTimePos() + getString(R.string.main_googlemap3)
                            + GpsService.getPosAccuracy()
                            + getString(R.string.main_googlemap4));
                    return true;

                } else { // Go ahead and send - no questions asked
                    sendSMS(false, Preferences.getSmsNumber(this), getString(R.string.main_googlemap1) + GpsService.getLastPos()
                            + getString(R.string.main_googlemap2) + GpsService.getTimePos() + getString(R.string.main_googlemap3)
                            + GpsService.getPosAccuracy()
                            + getString(R.string.main_googlemap4));
                    return true;
                }
            case R.id.action_optabout:
                // do something for About click
                AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
                aboutDialog.setTitle(R.string.action_about_title);
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.action_about_text));
                sb.append(LogFragment.PATH_APPEXP);
                aboutDialog.setMessage(sb);
                aboutDialog.setPositiveButton(android.R.string.ok, null);
                aboutDialog.create();
                aboutDialog.show();
                break;
        }
        return false;
    }

}