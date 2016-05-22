/*
 * EditFragment - For editing and exporting trips
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 * 2014-01-02 Change the text on the "from" button to change to the year before
 * (bug: in january it became december current year and no data showed)
 */
package org.my.hero.driverlog;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

// -------------------------------------------------------------------------------------------------
// EditFragment
// -------------------------------------------------------------------------------------------------
@SuppressLint("SimpleDateFormat")
public class EditFragment extends Fragment implements OnClickListener, OnDateSetListener {


    String TAG = "EditFragment";
    Activity activity;
    Fragment frag;
    public static ListView listViewdb;
    private static DatabaseHandler dbHandler = null;
    public static SimpleCursorAdapter adapter;
    public static final DateFormat dateonly = new SimpleDateFormat("yyyy-MM-dd");
    DatePickerDialog.OnDateSetListener from_dateListener, to_dateListener;
    final int DATE_PICKER_TO = 0;
    final int DATE_PICKER_FROM = 1;
    public static Cursor cursor;
    Button btnfromdate, btntodate, btnsend;
    ExportXLS xls = null;

    // -------------------------------------------------------------------------------------------------
    // onCreate
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        frag = this;
    }

    // -------------------------------------------------------------------------------------------------
    // onActivityCreated
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnfromdate = (Button) activity.findViewById(R.id.btnfrom);
        btntodate = (Button) activity.findViewById(R.id.btnto);
        btnsend = (Button) activity.findViewById(R.id.btnSend);
        btnfromdate.setOnClickListener(this);
        btntodate.setOnClickListener(this);
        btnsend.setOnClickListener(this);

        if (savedInstanceState == null) {
            // Set to date to this month last date
            Calendar cal = Calendar.getInstance();
            cal.roll(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.roll(Calendar.DAY_OF_YEAR, -1);
            String strsto = dateonly.format(cal.getTime());
            btntodate.setText(strsto);

            // Set start to previous month first date
            if (cal.get(Calendar.MONTH) == 0) { //January
                cal.roll(Calendar.YEAR, -1); //Previous year
            }

            //Set start to previous month first date
            cal.roll(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String strsta = dateonly.format(cal.getTime());
            btnfromdate.setText(strsta);
        } else {
            btnfromdate.setText(savedInstanceState.getCharSequence("btnfromtxt"));
            btntodate.setText(savedInstanceState.getCharSequence("btntotxt"));
        }

        from_dateListener = new OnDateSetListener() {
            // -------------------------------------------------------------------------------------------------
            // onDateSet
            // We got two onDateSet - one for each button - set a nice
            // year-date-day as button text
            // -------------------------------------------------------------------------------------------------
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {

                arg2 += 1;
                String m, d;
                if (arg2 < 10)
                    m = "0" + arg2;
                else
                    m = arg2 + "";
                if (arg3 < 10)
                    d = "0" + arg3;
                else
                    d = arg3 + "";
                btnfromdate.setText(arg1 + "-" + m + "-" + d);

                refreshList();

            }
        };

        to_dateListener = new OnDateSetListener() {
            // -------------------------------------------------------------------------------------------------
            // onDateSet
            // We got two onDateSet - one for each button - set a nice
            // year-date-day as button text
            // -------------------------------------------------------------------------------------------------
            public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {

                arg2 += 1;
                String m, d;
                if (arg2 < 10)
                    m = "0" + arg2;
                else
                    m = arg2 + "";
                if (arg3 < 10)
                    d = "0" + arg3;
                else
                    d = arg3 + "";
                btntodate.setText(arg1 + "-" + m + "-" + d);

                refreshList();

            }
        };

        dbHandler = new DatabaseHandler(activity);
        listViewdb = (ListView) activity.findViewById(R.id.listViewdb);

        String[] arrayColumns = new String[]{DatabaseHandler.KEY_START, DatabaseHandler.KEY_PURPOSE,
                DatabaseHandler.KEY_ID};
        int[] arrayViewIDs = new int[]{R.id.txtHeader, R.id.txtBody};

        String start = btnfromdate.getText().toString();
        String stop = btntodate.getText().toString();

        cursor = dbHandler.getCursorQuery("select timestart,purpose,_id from trips where timestart between '" + start
                + " 00:00:00' and '" + stop + " 23:59:59'");

        adapter = new SimpleCursorAdapter(activity.getApplicationContext(), R.layout.row, cursor, arrayColumns,
                arrayViewIDs, 0);
        listViewdb.setAdapter(adapter);

        listViewdb.setOnItemClickListener(new OnItemClickListener() {

            // -------------------------------------------------------------------------------------------------
            // onItemClick
            // When you click on an item in the list we open an edit dialog
            // -------------------------------------------------------------------------------------------------
            @SuppressWarnings("rawtypes")
            @Override
            public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {

                Bundle passdata = new Bundle();
                Cursor listCursor = (Cursor) arg0.getItemAtPosition(arg2);
                int nameId = listCursor.getInt(listCursor.getColumnIndex(DatabaseHandler.KEY_ID));
                // We pass on the tripId to the dialog
                passdata.putInt("keyid", nameId);
                Intent editIntent = new Intent(activity, EditTrip.class);
                editIntent.putExtras(passdata);
                startActivity(editIntent);
            }
        });

    }

    // -------------------------------------------------------------------------------------------------
    // onSaveInstanceState
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("btnfromtxt", btnfromdate.getText());
        outState.putCharSequence("btntotxt", btntodate.getText());
        super.onSaveInstanceState(outState);
    }

    // -------------------------------------------------------------------------------------------------
    // refreshList
    // Here we update the listview
    // -------------------------------------------------------------------------------------------------
    public void refreshList() {
        cursor = dbHandler
                .getCursorQuery("select timestart,purpose,_id from trips where timestart between '"
                        + btnfromdate.getText().toString() + " 00:00:00' and '" + btntodate.getText().toString()
                        + " 23:59:59'");

        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();

    }

    // -------------------------------------------------------------------------------------------------
    // onCreateView
    // Inflate the window
    // -------------------------------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit, container, false);

        return v;
    }

    // -------------------------------------------------------------------------------------------------
    // onClick
    // Click listener for the buttons
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onClick(View arg0) {

        Dialog dlg;
        int yy, mm, dd;
        String frdate = "2001-01-01", todate = "2020-12-31";

        switch (arg0.getId()) {

            case R.id.btnfrom:
                frdate = btnfromdate.getText().toString();

                yy = Integer.parseInt(frdate.substring(0, 4));
                mm = Integer.parseInt(frdate.substring(5, 7)) - 1;
                dd = Integer.parseInt(frdate.substring(8, 10));
                dlg = new DatePickerDialog(activity, from_dateListener, yy, mm, dd);
                dlg.show();
                refreshList();
                break;

            case R.id.btnto:
                todate = btntodate.getText().toString();
                yy = Integer.parseInt(todate.substring(0, 4));
                mm = Integer.parseInt(todate.substring(5, 7)) - 1;
                dd = Integer.parseInt(todate.substring(8, 10));
                dlg = new DatePickerDialog(activity, to_dateListener, yy, mm, dd);
                dlg.show();
                refreshList();

                break;

            case R.id.btnSend:
                // Dialog for choosing work trips only or "everything"
                new AlertDialog.Builder(activity)
                        .setTitle(getResources().getString(R.string.edit_dlg_exptitle))
                        .setPositiveButton(getResources().getString(R.string.edit_btn_expwork),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        xls = new ExportXLS(frag, btnfromdate.getText().toString(), btntodate.getText()
                                                .toString(), 1);
                                        xls.export();
                                        if (xls.getFileName().length() > 0) {
                                            startEmailIntent();
                                        }

                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.edit_btn_expall),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        xls = new ExportXLS(frag, btnfromdate.getText().toString(), btntodate.getText()
                                                .toString(), 0);
                                        xls.export();
                                        if (xls.getFileName().length() > 0) {
                                            startEmailIntent();
                                        }
                                    }
                                }).show();

                break;
        }

    }

    // -------------------------------------------------------------------------------------------------
    // startEmailIntent
    // Intent for sending email
    // -------------------------------------------------------------------------------------------------
    private void startEmailIntent() {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{Preferences.getEmail(activity)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.edit_email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.edit_email_message));
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(xls.getFileName())));
        emailIntent.setType("text/plain");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));

    }

    // -------------------------------------------------------------------------------------------------
    // onDateSet - unimplemented...
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }

}
