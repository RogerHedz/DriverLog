/*
 * Preferences . for settings
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    static String TAG = "PreferenceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    private void setSummaryEd(String key, String def, String pre, String seq) {
        getPreferenceScreen().findPreference(key).setSummary(pre + PreferenceManager.getDefaultSharedPreferences(this).getString(key, def) + seq);
    }

    private void setSummaryCb(String key, boolean def, String ifTrue, String ifFalse) {
        getPreferenceScreen().findPreference(key).setSummary(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(key, def) ? ifTrue : ifFalse);
    }

    private void setSummaryLb(String key, String def) {
        getPreferenceScreen().findPreference(key).setSummary(PreferenceManager.getDefaultSharedPreferences(this).getString(key, def));
    }

    private void setupPref() {
        Context ctx = getApplicationContext();

        setSummaryEd("key_pref_regnr", ctx.getResources().getString(R.string.pref_regnrvalue), ctx.getResources().getString(R.string.pref_regnr), "");
        setSummaryEd("key_pref_email", ctx.getResources().getString(R.string.pref_emailvalue), ctx.getResources().getString(R.string.pref_email), "");
        setSummaryEd("key_pref_minmove", ctx.getResources().getString(R.string.pref_minmove), ctx.getResources().getString(R.string.pref_gpsminmovesum1), ctx
                .getResources().getString(R.string.pref_gpsminmovesum2));
        setSummaryEd("key_pref_sms", "", ctx.getResources().getString(R.string.pref_smssum), "");
        setSummaryEd("key_pref_acc", ctx.getResources().getString(R.string.pref_acc), ctx.getResources().getString(R.string.pref_gpsaccsum1), ctx
                .getResources().getString(R.string.pref_gpsaccsum2));
        setSummaryEd("key_pref_corr", "0", ctx.getResources().getString(R.string.pref_tripcorrsum1), ctx.getResources().getString(R.string.pref_tripcorrsum2));
        setSummaryCb("key_pref_gps", true, ctx.getResources().getString(R.string.pref_gpssumyes), ctx.getResources().getString(R.string.pref_gpssumno));
        setSummaryLb("key_pref_maptype", ctx.getResources().getString(R.string.pref_maptype));
        setSummaryLb("key_pref_maptrackcolor", ctx.getResources().getString(R.string.pref_maptrackcolor));

        setSummaryEd("key_pref_url", ctx.getResources().getString(R.string.pref_url), ctx.getResources().getString(R.string.pref_urlsum), "");
        setSummaryCb("key_pref_online", false, ctx.getResources().getString(R.string.pref_onlineyes), ctx.getResources().getString(R.string.pref_onlineno));
        setSummaryCb("key_pref_tracks", false, ctx.getResources().getString(R.string.pref_tracksyes), ctx.getResources().getString(R.string.pref_tracksno));
        setSummaryCb("key_pref_extern", true, ctx.getResources().getString(R.string.pref_externyes), ctx.getResources().getString(R.string.pref_externno));
        setSummaryCb("key_pref_googleadr", true, ctx.getResources().getString(R.string.pref_geolocyes), ctx.getResources().getString(R.string.pref_geolocno));
        setSummaryCb("key_pref_map3d", true, ctx.getResources().getString(R.string.pref_map3dyes), ctx.getResources().getString(R.string.pref_map3dno));
        setSummaryCb("key_pref_mapnorthup", true, ctx.getResources().getString(R.string.pref_mapnorthupyes),
                ctx.getResources().getString(R.string.pref_mapnorthupno));

        //findPreference("key_pref_online").setEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        setupPref();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setupPref();
    }

    // Return the source url
    public static String getUrl(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_url", context.getResources().getString(R.string.pref_url));
        return str;
    }

    public static String getRegnr(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_regnr", context.getResources().getString(R.string.pref_regnrvalue));
        return str;
    }

    public static String getEmail(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_email", context.getResources().getString(R.string.pref_email));
        return str;
    }

    public static String getMinMove(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_minmove",
                context.getResources().getString(R.string.pref_minmove));
        return str;
    }

    public static String getAccuracy(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_acc", context.getResources().getString(R.string.pref_acc));
        return str;
    }

    public static String getTripCorrection(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_corr", "0");
        return str;
    }

    // public static String getProvider(Context context) {
    // String str =
    // PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_provider",
    // context.getResources().getString(R.string.pref_provider));
    // return str;
    // }

    public static String getMapType(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_maptype",
                "Normal"); //context.getResources().getString(R.string.pref_maptype));
        return str;
    }

    public static String getMapTrackColor(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context).getString("key_pref_maptrackcolor",
                context.getResources().getString(R.string.pref_maptrackcolor));
        return str;
    }

    public static boolean getOnline(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_online", false);
        return res;
    }

    public static boolean getTracks(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_tracks", false);
        return res;
    }

    public static boolean getExtern(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_extern", true);
        return res;
    }

    public static boolean getGoogleAdr(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_googleadr", true);
        return res;
    }

    public static boolean getMap3d(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_map3d", true);
        return res;
    }

    public static boolean getUseGPS(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_gps", true);
        return res;
    }

    public static boolean getMapNorthUp(Context context) {
        boolean res = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("key_pref_mapnorthup", true);
        return res;
    }

    public static String getSmsNumber(Context context) {
        String str = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("key_pref_sms", context.getResources().getString(R.string.pref_smsnumber));
        return str;
    }

}
