/*
 * JournalFragment - Main screen for the drivers journal
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

// -------------------------------------------------------------------------------------------------
// Journal - This is the main tabbed window for the drivers journal program
// -------------------------------------------------------------------------------------------------
@SuppressLint("SimpleDateFormat")
public class LogFragment extends Fragment implements OnClickListener {

	String TAG = "LogFragment", strMission;
	public static String PATH_NAME;
	public static String PATH_APPDB;
	public static String PATH_APPEXP;
	public static final String DATABASE_NAME = "journal.db";

	private Button buttonStartGps, buttonStartMan;
	public static final DateFormat readabledate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DecimalFormatSymbols point = new DecimalFormatSymbols();
	TextView lat, lon, time, acc, trip, meterstart, meterstop, mission, speed, prov, startadr, endadr;
	CheckBox type;
	private int tripId;
	float valmeterstart, valmeterstop;
	private static boolean tripstarted = false;
	private static boolean firstloc = true;
	private static boolean continuetrip = false;
	private static double mLat, mLon;
	Activity activity;
	View view;
	private DatabaseHandler dbhandler = null;
	private TabHost host;
	private TextView tv;

	// -------------------------------------------------------------------------------------------------
	// onCreate
	// Here we set the save path for db and exports
	// -------------------------------------------------------------------------------------------------
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		activity = getActivity();
		activity.registerReceiver(broadcastReceived, new IntentFilter("LOCATION_UPDATED"));

		point.setDecimalSeparator('.');
		GpsService.dec1dig.setDecimalFormatSymbols(point);
		GpsService.dec5dig.setDecimalFormatSymbols(point);
		GpsService.dec3dig.setDecimalFormatSymbols(point);

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) && Preferences.getExtern(activity) == true) {
			PATH_NAME = Environment.getExternalStorageDirectory().getAbsolutePath();
			PATH_APPDB = PATH_NAME + activity.getResources().getString(R.string.journal_db_path);
			PATH_APPEXP = PATH_NAME + activity.getResources().getString(R.string.journal_exp_path);
			File dbDirectory = new File(PATH_APPDB);
			dbDirectory.mkdirs();
			File expDirectory = new File(PATH_APPEXP);
			expDirectory.mkdirs();
		} else {
			PATH_APPDB = "";
			PATH_APPEXP = "";
		}
	}

	// -------------------------------------------------------------------------------------------------
	// onDestroy - the application is really closing
	// -------------------------------------------------------------------------------------------------
	@Override
	public void onDestroy() {
		super.onDestroy();
		activity.unregisterReceiver(broadcastReceived);
	}

	// -------------------------------------------------------------------------------------------------
	// onPause - the application is resting and not visible
	// -------------------------------------------------------------------------------------------------
	@Override
	public void onPause() {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("key", buttonStartGps.getText().toString());
		editor.putString("btngpstxt", buttonStartGps.getText().toString());
		editor.putString("btnmantxt", buttonStartMan.getText().toString());
		editor.putString("lattxt", lat.getText().toString());
		editor.putString("lontxt", lon.getText().toString());
		editor.putString("acctxt", acc.getText().toString());
		editor.putString("triptxt", trip.getText().toString());
		editor.putString("timetxt", time.getText().toString());
		editor.putString("meterstarttxt", meterstart.getText().toString());
		editor.putString("meterstoptxt", meterstop.getText().toString());
		editor.putString("speed", speed.getText().toString());
		editor.putBoolean("btngpsenabled", buttonStartGps.isEnabled());
		editor.putBoolean("btnmanenabled", buttonStartMan.isEnabled());
		editor.putString("mission", mission.getText().toString());
		editor.putString("startadr", startadr.getText().toString());
		editor.putString("endadr", endadr.getText().toString());
		editor.putBoolean("checktype", type.isChecked());
		editor.putBoolean("typeenabled", type.isEnabled());

		editor.commit();

		if (dbhandler != null)
			dbhandler.close();

		super.onPause();
	}

	// -------------------------------------------------------------------------------------------------
	// onCreateView - here we try to set the screen always on and we inflate the
	// layout
	// -------------------------------------------------------------------------------------------------
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_log, container, false);

		// No sleep for screen on this view
		view.setKeepScreenOn(true);
		return view;
	}

	// -------------------------------------------------------------------------------------------------
	// isGpsServiceRunning()
	// Check if gpsService is running
	// -------------------------------------------------------------------------------------------------
	private boolean isGpsServiceRunning() {
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (GpsService.class.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	// -------------------------------------------------------------------------------------------------
	// onActivityCreated
	// Here we get references to all controls and sets them to a proper state
	// If saveInstanceState is not null we have a restart
	// -------------------------------------------------------------------------------------------------
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		buttonStartGps = (Button) activity.findViewById(R.id.btnStartGps);
		buttonStartMan = (Button) activity.findViewById(R.id.btnStartMan);
		lat = (TextView) activity.findViewById(R.id.txtLat);
		lon = (TextView) activity.findViewById(R.id.txtLon);
		time = (TextView) activity.findViewById(R.id.txtLastPosTime);
		acc = (TextView) activity.findViewById(R.id.txtAcc);
		trip = (TextView) activity.findViewById(R.id.txtVTrip);
		meterstart = (TextView) activity.findViewById(R.id.txtMeterStart);
		meterstop = (TextView) activity.findViewById(R.id.txtMeterStop);
		mission = (TextView) activity.findViewById(R.id.txtVMission);
		mission.setMovementMethod(new ScrollingMovementMethod());

		speed = (TextView) activity.findViewById(R.id.txtSpeed);
		type = (CheckBox) activity.findViewById(R.id.CbWork);
		prov = (TextView) activity.findViewById(R.id.txtProv);
		startadr = (TextView) activity.findViewById(R.id.txtAdrStart);
		startadr.setMovementMethod(new ScrollingMovementMethod());
		endadr = (TextView) activity.findViewById(R.id.txtAdrEnd);
		endadr.setMovementMethod(new ScrollingMovementMethod());
		host = (TabHost) activity.findViewById(android.R.id.tabhost);
		tv = (TextView) host.getTabWidget().getChildAt(1).findViewById(android.R.id.title);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		buttonStartGps.setText(prefs.getString("btngpstxt", getString(R.string.journal_btn_gps_start)));
		buttonStartMan.setText(prefs.getString("btnmantxt", getString(R.string.journal_btn_man_start)));
		lat.setText(prefs.getString("lattxt", "lat"));
		lon.setText(prefs.getString("lontxt", "lon"));
		acc.setText(prefs.getString("acctxt", "acc"));
		trip.setText(prefs.getString("triptxt", "0 m"));
		speed.setText(prefs.getString("speed", "0"));
		time.setText(prefs.getString("timetxt", "12:00:00"));
		meterstart.setText(prefs.getString("meterstarttxt", "0"));
		meterstop.setText(prefs.getString("meterstoptxt", "0"));
		buttonStartGps.setEnabled(prefs.getBoolean("btngpsenabled", true));
		buttonStartMan.setEnabled(prefs.getBoolean("btnmanenabled", true));
		mission.setText(prefs.getString("mission", ""));

		startadr.setText(prefs.getString("startadr", ""));
		endadr.setText(prefs.getString("endadr", ""));
		type.setChecked(prefs.getBoolean("checktype", true));
		type.setEnabled(prefs.getBoolean("typeenabled", true));

		// add buttons listener
		buttonStartGps.setOnClickListener(this);
		buttonStartMan.setOnClickListener(this);

		dbhandler = new DatabaseHandler(activity);
		Trips tr = dbhandler.getLastTrip();
		dbhandler.close();
		if (tr != null) {
			tripId = tr.getId();
			if (savedInstanceState == null)
				populateJournal(tr);
		}
		Log.d(TAG, "onActivityCreated: " + tripId);

		// Gps-service active and we want tracking
		if (isGpsServiceRunning()) {
//			Need to ensure that all buttons and enable/disable thinsgs work
//			tv.setTextColor(Color.DKGRAY);
//			host.getTabWidget().getChildAt(1).setEnabled(false);
//			buttonStartMan.setEnabled(false);
//			buttonStartGps.setText(getResources().getString(R.string.journal_btn_gps_end));
//			Toast.makeText(activity, "GpsService is running", Toast.LENGTH_SHORT).show();
		} else if (!isGpsServiceRunning() && tripstarted && !buttonStartMan.isEnabled()) {
//			Toast.makeText(activity, "GpsService NOT running", Toast.LENGTH_SHORT).show();
			activity.startService(new Intent(activity, GpsService.class));
		}

		super.onActivityCreated(savedInstanceState);
	}

	// -------------------------------------------------------------------------------------------------
	// populateJournal
	// Fill widgets with info from a trip (most often the latest trip)
	// -------------------------------------------------------------------------------------------------
	private void populateJournal(Trips tr) {

		try {
			trip.setText(GpsService.dec1dig.format(tr.getTrip())); //xcv
			meterstart.setText(GpsService.dec1dig.format(tr.getMeterstart())); //xcv
			meterstop.setText(GpsService.dec1dig.format(tr.getMeterstop()));
			lat.setText("--.---");
			lon.setText("--.---");
			acc.setText("--.---");
			String str = tr.getStop();
			if (str != null && str.length() > 15)
				time.setText(str.substring(0, 10) + "\n" + str.substring(11, 19));
			else
				time.setText("--.--.--");
			speed.setText("---");
			mission.setText(tr.getPurpose());
			startadr.setText(tr.getAdressstart());
			endadr.setText(tr.getAdressstop());
			type.setChecked(tr.getWork() == 1);
			prov.setText("---");
		} catch (Exception e) {
			Log.d(TAG, "populateJournal: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------------------------------
	// onClick
	// Listener for clicks on buttons
	// -------------------------------------------------------------------------------------------------
	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.btnStartGps:
			if (tripstarted == false) {
				startTrip(getResources().getString(R.string.journal_start_trip), true);
			} else {
				endTrip(getResources().getString(R.string.journal_end_trip), true);
			}

			break;

		case R.id.btnStartMan:
			if (tripstarted == false) {
				startTrip(getResources().getString(R.string.journal_start_trip), false);
			} else {
				endTrip(getResources().getString(R.string.journal_end_trip), false);
			}

			break;
		}
	}

	// -------------------------------------------------------------------------------------------------
	// resizeTextview
	// Resize the textview to numberoflines in TextView
	// -------------------------------------------------------------------------------------------------
	public void resizeTextview(TextView tv) {
		if (tv.getLineCount() < 3)
			tv.setLines(2);
		else
			tv.setLines(tv.getLineCount());

	}

	// -------------------------------------------------------------------------------------------------
	// updateAdr
	// Get and store address of start and end position.
	// If no address reported store lat/lon instead
	// -------------------------------------------------------------------------------------------------
	public void updateAdr(String result, boolean last) {
		dbhandler = new DatabaseHandler(activity);
		Trips tr = dbhandler.getTrip(tripId);
		StringBuilder str = new StringBuilder();
		if (tr != null) {
			if (last) {
				if (tr.getAdressstop() == null || tr.getAdressstop().length() < 5) {
					if (result.length() < 5) {
						str.append(GpsService.dec5dig.format(mLat));
						str.append("\n");
						str.append(GpsService.dec5dig.format(mLon));
						endadr.setText(str);
						tr.setAdressstop(str.toString());
					} else {
						endadr.setText(result);
						tr.setAdressstop(result);

						resizeTextview(endadr);
					}
				}
			} else {
				if (tr.getAdressstart() == null) {
					if (result.length() < 5) {
						str.append(GpsService.dec5dig.format(mLat));
						str.append("\n");
						str.append(GpsService.dec5dig.format(mLon));
						startadr.setText(str);
						tr.setAdressstart(str.toString());
					} else {
						startadr.setText(result);
						tr.setAdressstart(result);

						resizeTextview(startadr);
					}
				}
			}
			dbhandler.updateTrip(tr);
			dbhandler.close();
		}

	}

	// -------------------------------------------------------------------------------------------------
	// continueTrip
	// Function to continue a previous trip
	// -------------------------------------------------------------------------------------------------
	private void continueTrip() {
		firstloc = false;
		if (Preferences.getUseGPS(activity) == true) {
			prov.setText(getResources().getString(R.string.journal_provider_gps));
		} else {
			prov.setText(getResources().getString(R.string.journal_provider_net));
		}

		// no focus on edit tab while gps service is running
		tv.setTextColor(Color.DKGRAY);
		host.getTabWidget().getChildAt(1).setEnabled(false);

		buttonStartMan.setEnabled(false);
		buttonStartGps.setText(getResources().getString(R.string.journal_btn_gps_end));
		type.setEnabled(false);
		activity.startService(new Intent(activity, GpsService.class));

		tripstarted = true;
		continuetrip = true;

	}

	// -------------------------------------------------------------------------------------------------
	// newTrip
	// function to start a new trip
	// -------------------------------------------------------------------------------------------------
	private void newTrip(boolean usegps) {

		if (usegps) {
			// no focus on edit tab while gps
			// service is running
			tv.setTextColor(Color.DKGRAY);
			host.getTabWidget().getChildAt(1).setEnabled(false);
			buttonStartMan.setEnabled(false);
			buttonStartGps.setText(getResources().getString(R.string.journal_btn_gps_end));
			type.setEnabled(false);
			activity.startService(new Intent(activity, GpsService.class));

		} else {
			buttonStartGps.setEnabled(false);
			buttonStartMan.setText(getResources().getString(R.string.journal_btn_man_end));
			type.setEnabled(false);

		}

		Trips tr = new Trips();
		dbhandler = new DatabaseHandler(activity);

		try {
			GregorianCalendar greg = new GregorianCalendar();
			String date = GpsService.timestampFormat.format(greg.getTime());
			tr.setStart(date);

			int typ = type.isChecked() ? 1 : 0;
			tr.setWork(typ);
			tr.setCar(Preferences.getRegnr(activity));
			tr.setMeterstart(valmeterstart);

			tr.setPurpose(strMission);
			dbhandler.addTrip(tr);

			tr = dbhandler.getTrip(date);

			tripId = tr.getId();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Starttrip: " + "Error" + e.getMessage());
		}

		dbhandler.close();
		tripstarted = true;
	}

	// -------------------------------------------------------------------------------------------------
	// startTrip
	// start a trip by gps or manually
	// -------------------------------------------------------------------------------------------------
	private void startTrip(String title, final boolean usegps) {
		LayoutInflater inflater = LayoutInflater.from(activity);
		final View addView = inflater.inflate(R.layout.dlg_start_trip, null);

		// Editboxes within the DIALOG. Start metervalue (number) and mission
		// (multiline text)
		final EditText met = (EditText) addView.findViewById(R.id.value);
		final EditText dlgmsn = (EditText) addView.findViewById(R.id.mission);

		// These dont work on all terminals - kept for reference
		// met.setRawInputType(Configuration.KEYBOARD_12KEY);
		// upp.setRawInputType(Configuration.KEYBOARD_QWERTY);

		met.setText(meterstop.getText().toString());
		int length = met.getText().length();
		met.setSelection(length, length);

		dlgmsn.setText(mission.getText());

		new AlertDialog.Builder(activity).setTitle(title)
				.setPositiveButton(getResources().getString(R.string.journal_continue_trip), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dbhandler = new DatabaseHandler(activity);
						Trips tr = dbhandler.getLastTrip();
						if (tr != null) {
							tripId = tr.getId();

							// Move the end address and timestamp to mission
							// text
							// (this will be an intermidiate point)
							StringBuilder sb = new StringBuilder();
							// First fetch the original mission text
							sb.append(mission.getText());
							// Get the end time - append it
							if (tr.getStop() != null) {
								sb.append("\n");
								sb.append(tr.getStop());
							}
							// Get the end address - append it
							if (tr.getAdressstop() != null && tr.getAdressstop().length() > 5) {
								sb.append("\n");
								sb.append(tr.getAdressstop());
							}
							// Write back the complete mission text
							mission.setText(sb);

							resizeTextview(mission);

							// Save in database
							tr.setPurpose(sb.toString());
							tr.setStop("");
							tr.setAdressstop("");
							endadr.setText("");

							dbhandler.updateTrip(tr);
						} else {
							Toast.makeText(activity, getResources().getString(R.string.journal_toast_info_notrips), Toast.LENGTH_LONG).show();
							return;
						}
						dbhandler.close();
						continueTrip();

					}
				}).setNegativeButton(getResources().getString(R.string.journal_new_trip), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						new AlertDialog.Builder(activity).setTitle(getResources().getString(R.string.journal_new_trip)).setView(addView)
								.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										trip.setText("0 m");
										lat.setText("");
										lon.setText("");
										acc.setText("");
										time.setText("");
										speed.setText("");
										startadr.setText("");
										endadr.setText("");
										firstloc = true;

										if (Preferences.getUseGPS(activity) == true) {
											prov.setText(getResources().getString(R.string.journal_provider_gps));
										} else {
											prov.setText(getResources().getString(R.string.journal_provider_net));

										}
										strMission = dlgmsn.getText().toString();
										try {
											valmeterstart = Float.parseFloat(met.getText().toString());
											meterstart.setText(GpsService.decdig.format(valmeterstart));
											meterstop.setText(GpsService.decdig.format(valmeterstart));
											mission.setText(strMission);
										} catch (NumberFormatException e1) {
											e1.printStackTrace();
											valmeterstart = 0;
											Log.d(TAG, "startTrip: NumError" + e1.getMessage());
										}

										resizeTextview(mission);

										newTrip(usegps);

									}
								}).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										// ignore, just dismiss
										valmeterstart = 0;
										strMission = "";
									}
								}).show();

					}
				}).show();

	}

	// -------------------------------------------------------------------------------------------------
	// endTrip
	// end of trip - store and clean up
	// -------------------------------------------------------------------------------------------------
	private void endTrip(String title, final boolean gps) {
		LayoutInflater inflater = LayoutInflater.from(activity);
		final View addView = inflater.inflate(R.layout.dlg_end_trip, null);
		final EditText met = (EditText) addView.findViewById(R.id.mtrstop);

		met.setText(meterstop.getText().toString());
		int length = met.getText().length();
		met.setSelection(length, length);

		new AlertDialog.Builder(activity).setTitle(title).setView(addView)
				.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (Preferences.getGoogleAdr(activity)) {
							new getGoogleAddress() {
								@Override
								public void onPostExecute(String result) {
									// Toast.makeText(activity, result,
									// Toast.LENGTH_LONG).show();
									updateAdr(result, true);

								}
							}.execute("");
						}

						try {
							valmeterstop = Float.parseFloat(met.getText().toString());
						} catch (NumberFormatException e1) {
							e1.printStackTrace();
							Log.d(TAG, "endTrip: Error" + e1.getMessage());
							valmeterstop = 0;
						}
						meterstop.setText(GpsService.dec1dig.format(valmeterstop)); //xcv
						dbhandler = new DatabaseHandler(activity);

						try {
							GregorianCalendar greg = new GregorianCalendar();
							String date = GpsService.timestampFormat.format(greg.getTime());

							float msta, msto;

							try {
								msto = Float.parseFloat(meterstop.getText().toString());
							} catch (NumberFormatException e) {
								msto = 0;
							}

							try {
								msta = Float.parseFloat(meterstart.getText().toString());
							} catch (NumberFormatException e) {
								msta = 0;
							}

							Trips tr = dbhandler.getTrip(tripId);

							if (msto > 0 && msta > 0) {
								trip.setText(GpsService.dec1dig.format(msto - msta));
								tr.setTrip(msto - msta);
							}

							tr.setStop(date);
							tr.setMeterstop(msto);

							dbhandler.updateTrip(tr);
							dbhandler.close();

							if (gps) {
								// now we can enable focus on edit tab
								// again
								tv.setTextColor(Color.WHITE);
								host.getTabWidget().getChildAt(1).setEnabled(true);
								buttonStartMan.setEnabled(true);
								buttonStartGps.setText(getResources().getString(R.string.journal_btn_gps_start));
								type.setEnabled(true);
								activity.stopService(new Intent(activity, GpsService.class));
							} else {
								buttonStartGps.setEnabled(true);
								buttonStartMan.setText(getResources().getString(R.string.journal_btn_man_start));
								type.setEnabled(true);
							}
							tripstarted = false;
						} catch (Exception e) {
							e.printStackTrace();
							Log.d(TAG, "endTrip: Error" + e.getMessage());
						}
					}
				}).setNegativeButton(getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						valmeterstop = 0;
					}
				}).show();

	}

	// -------------------------------------------------------------------------------------------------
	// BroadcastReceiver
	// receiver to get info from Gps-service
	// -------------------------------------------------------------------------------------------------
	private BroadcastReceiver broadcastReceived = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			try {

				// a check that view is created
				if (lat == null) {
					Log.d(TAG, "onReceive: Error lat == null");
					return;
				}

				if (continuetrip) {
					Float tripmeter = (Float.parseFloat(meterstop.getText().toString()) - Float.parseFloat(meterstart.getText().toString())) * 1000.0f;
					GpsService.setTrip(tripmeter);
					continuetrip = false;
				}

				mLat = intent.getExtras().getDouble("lat");
				lat.setText(GpsService.dec5dig.format(mLat));

				mLon = intent.getExtras().getDouble("lon");
				lon.setText(GpsService.dec5dig.format(mLon));

				if (Preferences.getGoogleAdr(activity)) {
					// First time we got a location --> fetch address
					if (firstloc) {
						Toast.makeText(activity, getResources().getString(R.string.journal_toast_info_getadr), Toast.LENGTH_LONG).show();
						firstloc = false;
						new getGoogleAddress() {
							@Override
							public void onPostExecute(String result) {
								updateAdr(result, false);
								Log.d(TAG, "googledadr length: " + result.length());
								Log.d(TAG, "googleadr: " + result);

							}
						}.execute("");

					}
				}

				String str = intent.getExtras().getString("tim");
				time.setText(str);

				float speedval = intent.getExtras().getFloat("speed");
				speedval = (speedval * 60 * 60) / 1000;
				if (speedval > 0)
					speed.setText(GpsService.decdig.format(speedval) + " km/h");
				else
					speed.setText("0 km/h");

				float flt = intent.getExtras().getFloat("acc");
				acc.setText(GpsService.decdig.format(flt) + " m");

				flt = intent.getExtras().getFloat("dist");
				if (flt < 1000) {
					trip.setText(GpsService.decdig.format(flt) + " m");
					meterstop.setText(GpsService.dec1dig.format(Float.parseFloat(meterstart.getText().toString()) + (flt / 1000)));
				} else {
					trip.setText(GpsService.dec1dig.format(flt / 1000) + " km");
					meterstop.setText(GpsService.dec1dig.format(Float.parseFloat(meterstart.getText().toString()) + (flt / 1000)));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				Log.d(TAG, "onReceive: Error" + e.getMessage());
			}

			if (intent.getExtras().getBoolean("provider")) {
				prov.setText(getResources().getString(R.string.journal_provider_gps));
			} else {
				prov.setText(getResources().getString(R.string.journal_provider_net));
			}
		}
	};

	// -------------------------------------------------------------------------------------------------
	// getGoogleAdress
	// Async class to fetch address from google
	// -------------------------------------------------------------------------------------------------
	private class getGoogleAddress extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			Geocoder geoCoder = new Geocoder(activity, Locale.getDefault());
			String add = "";
			try { // fetch one address
				List<Address> addresses = geoCoder.getFromLocation(mLat, mLon, 1);

				if (addresses.size() > 0) {
					for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
						add += addresses.get(0).getAddressLine(i) + "\n";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return add;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

}
