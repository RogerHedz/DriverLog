/*
 * MapFragment - tab showing a google map V2
 * 
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 * Much of this code is from the samples
 */
package org.my.hero.driverlog;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.FragmentActivity;
//import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback { // implements LocationListener

    public MapFragment() {
        //empty constructor
    }

    @Override
    public void onResume() {
        gmap = getActivity();
        super.onResume();
    }

    /**
     * Note that this may be null if the Google Play services APK is not
     * available.
     */
    private GoogleMap mMap;
    SupportMapFragment fragment;
    String TAG = "MapFragment";
    View myView;
    static boolean once = true;
    FragmentActivity gmap;
    TextView lblspeed, lblMapLat, lblMapLon, lblMaptrip, lblMapBearing;
    List<LatLng> poslist = new ArrayList<LatLng>();
    int maxNumPos = 100;
    Polyline line;
    static boolean first;

    // -------------------------------------------------------------------------------------------------
    // onDestroy
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestroy: " + "unreg rec");
        try {
            if (gmap != null)
                gmap.unregisterReceiver(broadcastReceived);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onDestroy: " + e.toString());
        }

        super.onDestroy();

    }

    // -------------------------------------------------------------------------------------------------
    // onCreateView
    // -------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (myView == null) {
            myView = inflater.inflate(R.layout.fragment_map, container, false);
            myView.setKeepScreenOn(true);
        }
        return myView;
    }

    // -------------------------------------------------------------------------------------------------
    // onActivityCreated
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onAct: ");
        lblspeed = (TextView) getActivity().findViewById(R.id.txtMapspeed);
        lblspeed.setText("");
        lblMapLat = (TextView) getActivity().findViewById(R.id.txtMapLat);
        lblMapLat.setText("");
        lblMapLon = (TextView) getActivity().findViewById(R.id.txtMapLon);
        lblMapLon.setText("");
        lblMaptrip = (TextView) getActivity().findViewById(R.id.txtMaptrip);
        lblMaptrip.setText("");
        lblMapBearing = (TextView) getActivity().findViewById(R.id.txtmapbearing);
        lblMapBearing.setText("");

        FragmentManager fm = getFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.myGMap);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.myGMap, fragment).commit();
        }
        fragment.getMapAsync(this);
        // gmap = getActivity();
        // Log.d(TAG, "onActivityCreated: " + "reg rec");
        // gmap.registerReceiver(broadcastReceived, new
        // IntentFilter("LOCATION_UPDATED"));
        // setUpMapIfNeeded();

        // first = true;
        //setUpMapIfNeeded();
    }

    // -------------------------------------------------------------------------------------------------
    // onPause
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onPause() {

//		mMap = null;
        once = true;
//
        FragmentManager fmanager = getFragmentManager();
        fmanager.beginTransaction().remove(fragment).commitAllowingStateLoss();
        super.onPause();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "SetupMap");
        mMap = googleMap;
        gmap = getActivity();
        once = true;

        setUpMap();
    }


    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera. In this case, we just add a marker.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
    // -------------------------------------------------------------------------------------------------
    // setUpMap
    // -------------------------------------------------------------------------------------------------
    private void setUpMap() {
        // Enabling MyLocation Layer of Google Map
        // Only if gps used - not network (if you choose network and have
        // this enabled the gps i switched on anyway)
        if (Preferences.getUseGPS(getActivity()))
            mMap.setMyLocationEnabled(true);

        Log.d(TAG, "setupmap: " + "reg rec");
        gmap.registerReceiver(broadcastReceived, new IntentFilter("LOCATION_UPDATED"));

        LatLng latLng = new LatLng(GpsService.getLastLat(), GpsService.getLastLon());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        // remove all track
        poslist.clear();

        // Set the map type from settings
        if (Preferences.getMapType(gmap).startsWith("H"))
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        else if (Preferences.getMapType(gmap).startsWith("T"))
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        else if (Preferences.getMapType(gmap).startsWith("S"))
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        int color;
        if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_colorred)))
            color = Color.RED;
        else if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_colorgreen)))
            color = Color.GREEN;
        else if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_colorblue)))
            color = Color.BLUE;
        else if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_coloryellow)))
            color = Color.YELLOW;
        else if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_colormagenta)))
            color = Color.MAGENTA;
        else if (Preferences.getMapTrackColor(gmap).equals(gmap.getResources().getString(R.string.pref_colorcyan)))
            color = Color.CYAN;
        else
            color = Color.BLACK;
        line = mMap.addPolyline(new PolylineOptions().width(5).color(color));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    // -------------------------------------------------------------------------------------------------
    // class BroadcastReceiver
    // -------------------------------------------------------------------------------------------------
    private BroadcastReceiver broadcastReceived = new BroadcastReceiver() {

        // -------------------------------------------------------------------------------------------------
        // onReceive
        // -------------------------------------------------------------------------------------------------
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: ");

            if (mMap == null)
                return;

            float zoom = mMap.getCameraPosition().zoom;
            Log.d(TAG, "zoom map: " + zoom);

            double lat = intent.getExtras().getDouble("lat");
            double lon = intent.getExtras().getDouble("lon");
            LatLng latLng = new LatLng(lat, lon);
            poslist.add(latLng);

            if (poslist.size() > maxNumPos) {
                poslist.remove(0);
            }

            if (once == true) {
                LatLng pos = new LatLng(intent.getExtras().getDouble("lastlat"), intent.getExtras().getDouble("lastlon"));
                zoom = 16;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
                once = false;
            }

            float bear, bear1 = intent.getExtras().getFloat("bear", 0);

            if (Preferences.getMapNorthUp(gmap))
                bear = bear1;
            else
                bear = 0;
            // 0);

//			Log.d(TAG, "bear map: " + bear);
//			lblMapBearing.setText("Bearing: " + GpsService.decdig.format(bear1));

            CameraPosition cameraPosition;

            if (Preferences.getMap3d(gmap)) {
                // Sets the orientation of the camera
                cameraPosition = new CameraPosition.Builder().target(latLng).bearing(bear).tilt(80)
                        // Sets the tilt of the camera
                        .zoom(zoom).build(); // Creates a CameraPosition from
                // the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
            } else {
                // Creates a CameraPosition from the builder
                cameraPosition = new CameraPosition.Builder().target(latLng).bearing(bear).tilt(0).zoom(zoom).build();
                // Move the camera smoothly (2 sec) to new position
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2000, null);
            }

            line.setPoints(poslist);

            lblMapLat.setText(GpsService.dec3dig.format(lat));
            lblMapLon.setText(GpsService.dec3dig.format(lon));

            float speedval = intent.getExtras().getFloat("speed");
            speedval = (speedval * 60 * 60) / 1000;
            if (speedval > 0)
                lblspeed.setText(GpsService.decdig.format(speedval) + " km/h");

            double flt = intent.getExtras().getFloat("dist");
            if (flt < 1000) {
                lblMaptrip.setText("Trip: " + GpsService.decdig.format(flt) + " m");
            } else {
                lblMaptrip.setText("Trip: " + GpsService.dec1dig.format(flt / 1000) + " km");
            }

        }
    };

}