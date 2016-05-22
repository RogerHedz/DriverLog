/*
 * GpsService - a service for logging and location services
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class GpsService extends Service {

    String TAG = "GpsService";
    public static final String POINTS_TABLE = "POINTS";
    public static final String TRIPS_TABLE = "TRIPS";

    public static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    public static final DecimalFormat decdig = new DecimalFormat("##");
    public static final DecimalFormat dec1dig = new DecimalFormat("#.#");
    public static final DecimalFormat dec3dig = new DecimalFormat("#.###");
    public static final DecimalFormat dec5dig = new DecimalFormat("#.#####");
    public static final DecimalFormat dec7dig = new DecimalFormat("0.#######");
    private static DecimalFormatSymbols point = new DecimalFormatSymbols();

    private LocationManager locMgr;
    private LocationListener locationListener;
    private DatabaseHandler db;

    private static long minTimeMillis = 2000;
    private static long minDistanceMeters = 10;
    private static long onlinetimeupdate = 0;
    private static float minAccuracyMeters = 35;
    private static float mCorr = 0;
    private static float fAccuracy = -999;
    private static double mOldLat = 0;
    private static double mOldLon = 0;
    private static double clat = 0;
    private static double dlon = 0;
    private static String lastpositionupdate = "";
    static float mDist = 0.0f;
    private int tripId;
    private int lastStatus;
    int count = 0;
    private Trips tr;
    Notification notification;
    PendingIntent contentIntent;

    // -------------------------------------------------------------------------------------------------
    // startGPSService
    // Called when the service is created.
    // -------------------------------------------------------------------------------------------------
    private void startGPSService() {
        Log.d(TAG, "startGpsService ");

        db = new DatabaseHandler(this);
        // ---use the LocationManager class to obtain GPS locations---
        locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();

        initAll();
    }

    // -------------------------------------------------------------------------------------------------
    // shutdownService
    // -------------------------------------------------------------------------------------------------
    private void shutdownService() {
        locMgr.removeUpdates(locationListener);
        mDist = 0.0f;
        mOldLat = 0;
        mOldLon = 0;
    }

    // -------------------------------------------------------------------------------------------------
    // resetTrip
    // Set the tripmeter to zero
    // -------------------------------------------------------------------------------------------------
    public void resetTrip() {
        mDist = 0;
    }

    // -------------------------------------------------------------------------------------------------
    // setTrip
    // Set the tripmeter to a number
    // -------------------------------------------------------------------------------------------------
    public static void setTrip(float val) {
        mDist = val;
    }

    // -------------------------------------------------------------------------------------------------
    // initAll
    // Initialize all values
    // -------------------------------------------------------------------------------------------------
    private void initAll() {
        mDist = 1.0f;
        mOldLat = 0.0;
        mOldLon = 0.0;

        try {

            minDistanceMeters = Long.parseLong(Preferences.getMinMove(getApplication()));
            minAccuracyMeters = Long.parseLong(Preferences.getAccuracy(getApplication()));
            mCorr = Float.parseFloat(Preferences.getTripCorrection(getApplicationContext()));
            mCorr = 1.0f + (mCorr / 100.0f);
            if (Preferences.getUseGPS(getApplication()) == true) {
                locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMillis, minDistanceMeters, locationListener);
                Log.d(TAG, "StartGps: Using GPS");
            } else {
                // Network - dont care about millis between updates or distance
                // between updates
                minAccuracyMeters = 10000;
                locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                Log.d(TAG, "StartGps: Using Network");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Log.d(TAG, "StartGps: " + e.getMessage());
        }

        tr = db.getLastTrip();
    }

    // -------------------------------------------------------------------------------------------------
    // class postHttp
    // A class to post a http POST
    // -------------------------------------------------------------------------------------------------
    private class postHttp extends AsyncTask<String, Void, String> {

        @SuppressWarnings("deprecation")
        @Override
        protected String doInBackground(String... params) {
            // at least one minute between updates
            if ((System.currentTimeMillis() - onlinetimeupdate) > 60000) {

                URL url = null;
                try {
                    url = new URL(Preferences.getUrl(getApplication()));
                } catch (MalformedURLException e1) {
                    Log.d(TAG, "Malformed URL: " + e1.toString());
                }

                // No url, or short url - return
                if (url == null || Preferences.getUrl(getApplication()).length() < 10)
                    return null;

                // Prepare connection
                HttpURLConnection con;
                try {
                    con = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    Log.d(TAG, "postLocation: " + e.toString());
                    return null;
                }

                try {
                    con.setRequestMethod("POST");
                } catch (ProtocolException e) {
                    Log.d(TAG, "protocolexception: " + e.toString());
                    return null;
                }

                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setDoInput(true);

                // requestbuilder
                StringBuffer req = new StringBuffer();
                req.append("regnr=");
                req.append(Preferences.getRegnr(getApplication()));

                req.append("&latitude=");
                req.append(clat);

                req.append("&longitude=");
                req.append(dlon);

                req.append("&accuracy=");
                req.append(fAccuracy);

                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", "" + req.length());

                // Connect
                StringBuffer response = new StringBuffer();
                try {
                    con.connect();

                    DataOutputStream wr;
                    wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(req.toString());
                    wr.flush();
                    wr.close();

                    DataInputStream rd;
                    rd = new DataInputStream(con.getInputStream());
                    response.append(rd.readLine());
                    rd.close();
                } catch (IOException e) {
                    Log.d(TAG, "IOexception: " + e.toString());
                    return null;
                }
                con.disconnect();

                Log.d(TAG, "Sent http POST");

                Log.d(TAG, "rec " + response.toString());

                notification.setLatestEventInfo(getApplication(), getText(R.string.service_name), response.toString(), contentIntent);
                notifMgr.notify(R.string.service_started, notification);

                // equalize the timer so we can wait for online update
                onlinetimeupdate = System.currentTimeMillis();

                return response.toString();
            } else {
                return null;
            }
        }
    }

    // -------------------------------------------------------------------------------------------------
    // class MyLocationListener
    // -------------------------------------------------------------------------------------------------
    public class MyLocationListener implements LocationListener {

        Intent intnt = new Intent("LOCATION_UPDATED");

        // -------------------------------------------------------------------------------------------------
        // lastPosition
        // -------------------------------------------------------------------------------------------------
        public Location lastPosition() {
            return locMgr.getLastKnownLocation("gps");
        }

        // -------------------------------------------------------------------------------------------------
        // onLocationChanged
        // -------------------------------------------------------------------------------------------------
        public void onLocationChanged(Location loc) {
            if (loc != null) {

                // // The first locations are not accurate - skip them
                // if (count++ < 3 )
                // return;

                Points pts;

                GregorianCalendar greg = new GregorianCalendar();

                if (loc.hasAccuracy() && loc.getAccuracy() <= minAccuracyMeters) {

                    // Uncomment if you want to save in GMT
                    // TimeZone tz = greg.getTimeZone();
                    // int offset =
                    // tz.getOffset(System.currentTimeMillis());
                    // greg.add(Calendar.SECOND, (offset / 1000) * -1);

                    if (tr == null) {
                        Log.d(TAG, "onLocationChanged: Trips == null");
                        return;
                    }

                    // This because a change in the settings should change mCorr during tracking
                    mCorr = Float.parseFloat(Preferences.getTripCorrection(getApplicationContext()));
                    mCorr = 1.0f + (mCorr / 100.0f);

                    tripId = tr.getId();
                    String tme = timestampFormat.format(greg.getTime());

                    int a = tripId;
                    String b = tme;
                    clat = loc.getLatitude();
                    dlon = loc.getLongitude();

                    double e = loc.hasAltitude() ? loc.getAltitude() : -999;
                    fAccuracy = loc.hasAccuracy() ? loc.getAccuracy() : -999;
                    float g = loc.hasSpeed() ? loc.getSpeed() : -999;
                    float h = loc.hasBearing() ? loc.getBearing() : 0;
                    // TODO
                    float i = 0.0f;

                    // Log.d(TAG, "la/lo: " + clat + "/" + dlon);
                    // Log.d(TAG, "acc: " + fAccuracy);
                    // Save track if set in preferences
                    if (Preferences.getTracks(getApplicationContext())) {
                        try {
                            pts = new Points(a, b, clat, dlon, e, fAccuracy, g, h, i);
                            db.addPoint(pts);
                        } catch (Exception x) {
                            Log.d(TAG, "onLocationChanged:err " + x.getMessage());
                            x.printStackTrace();
                        }
                    }

                    // Store values in intent
                    intnt.putExtra("lat", clat);
                    intnt.putExtra("lon", dlon);
                    intnt.putExtra("acc", fAccuracy);
                    intnt.putExtra("bear", h);

                    // if provider is network g is negative (set above)
                    if (g > 0)
                        intnt.putExtra("speed", g);

                    if (lastPosition() != null) {
                        // last position (used in map view)
                        intnt.putExtra("lastlat", lastPosition().getLatitude());
                        intnt.putExtra("lastlon", lastPosition().getLongitude());
                    }
                    float[] results = new float[1];

                    // Distance for first location is zero
                    if (mOldLat == 0) {
                        mOldLat = clat;
                        mOldLon = dlon;
                    }
                    Location.distanceBetween(clat, dlon, mOldLat, mOldLon, results);

                    // If distance is larger than 1000 meters and using gps -
                    // skip
                    if (results[0] > 1000 && Preferences.getUseGPS(getApplication())) {
                        mOldLat = clat;
                        mOldLon = dlon;
                        Log.d(TAG, "result " + results[0]);

                        return;
                    }

                    // Apply the correction factor from settings
                    results[0] = results[0] * mCorr;
                    mDist = mDist + results[0];
                    intnt.putExtra("dist", mDist);
                    mOldLat = clat;
                    mOldLon = dlon;
                    lastpositionupdate = timeFormat.format(greg.getTime());
                    intnt.putExtra("tim", lastpositionupdate);

                    intnt.putExtra("provider", Preferences.getUseGPS(getApplication()));
                    // Log.d(TAG, "onLocationCh num sats: " +
                    // loc.getExtras().getInt("satellites"));

                    sendBroadcast(intnt);

                    // If we want to send online data
                    if (Preferences.getOnline(getApplication())) {
                        new postHttp() {
                            @Override
                            public void onPostExecute(String result) {
                                // Toast.makeText(getApplication(), result,
                                // Toast.LENGTH_LONG).show();

                            }
                        }.execute("");

                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        // -------------------------------------------------------------------------------------------------
        // onStatusChanged
        // -------------------------------------------------------------------------------------------------
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String showStatus = null;
            if (status == LocationProvider.AVAILABLE)
                showStatus = "Available";
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                showStatus = "Temporarily Unavailable";
            if (status == LocationProvider.OUT_OF_SERVICE)
                showStatus = "Out of Service";
            if (status != lastStatus) {
                Log.d(TAG, "onStatusChanged: " + showStatus);
                // Toast.makeText(getBaseContext(), "new status: " + showStatus,
                // Toast.LENGTH_SHORT).show();
            }
            lastStatus = status;
        }

    }

    private NotificationManager notifMgr;

    // -------------------------------------------------------------------------------------------------
    // onCreate
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Log.d(TAG, "onCreate Service ");

        startGPSService();

        // Display a notification GPS starting
        showNotification();
    }

    // -------------------------------------------------------------------------------------------------
    // onDestroy
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onDestroy() {
        super.onDestroy();

        shutdownService();

        // Cancel the notification.
        notifMgr.cancel(R.string.service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    // -------------------------------------------------------------------------------------------------
    // showNotification
    // -------------------------------------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    private void showNotification() {

        CharSequence text = getText(R.string.service_started);

        // Set the icon, and text
        notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this
        // notification
        contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.service_name), text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number. We use it later to
        // cancel.
        notifMgr.notify(R.string.service_started, notification);
    }

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static void setMinTimeMillis(long _minTimeMillis) {
        minTimeMillis = _minTimeMillis;
    }

    public static long getMinTimeMillis() {
        return minTimeMillis;
    }

    public static String getLastPos() {
        point.setDecimalSeparator('.');
        dec7dig.setDecimalFormatSymbols(point);
        return dec7dig.format(clat) + "," + dec7dig.format(dlon);
    }

    public static double getLastLat() {
        return clat;
    }

    public static double getLastLon() {
        return dlon;
    }

    public static String getTimePos() {
        return lastpositionupdate;
    }

    public static String getPosAccuracy() {
        return decdig.format(fAccuracy);
    }

    public static void setMinDistanceMeters(long _minDistanceMeters) {
        minDistanceMeters = _minDistanceMeters;
    }

    public static long getMinDistanceMeters() {
        return minDistanceMeters;
    }

    public static float getMinAccuracyMeters() {
        return minAccuracyMeters;
    }

    public static void setMinAccuracyMeters(float minAccuracyMeters) {
        GpsService.minAccuracyMeters = minAccuracyMeters;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        GpsService getService() {
            return GpsService.this;
        }
    }

}

// BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
// int scale = -1;
// int level = -1;
// int voltage = -1;
// int temp = -1;
// int ch1, ch2;
//
// @Override
// public void onReceive(Context context, Intent intent) {
// level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
// scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
// temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
// voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
// ch1 = intent.getIntExtra("plugged", 1);
// Log.d("BatteryManager", "level is " + level + "/" + scale +
// ", temp is " + temp + ", voltage is "
// + voltage + " ch: " + ch1);
// }
// };
// IntentFilter filter = new
// IntentFilter(Intent.ACTION_BATTERY_CHANGED);
// registerReceiver(batteryReceiver, filter);
