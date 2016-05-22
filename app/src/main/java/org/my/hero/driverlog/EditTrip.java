/*
 * EditTrip - Dialog for editing a trip
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class EditTrip extends Activity {

    Button btnUpdate, btnDelete, btnCancel, btnKml;
    EditText edtripstart, edtripend, edtripmeterstart, edtripmeterend, edtrippurpose, edtripstartadress, edtripendadress;
    CheckBox cbtripwork, cbtriptrack;

    String TAG = "EditTrip";
    int rowId;
    Cursor c;
    DatabaseHandler dbHandler;
    Trips tr;
    Activity activity;

    // -------------------------------------------------------------------------------------------------
    // onCreate
    // Here we create references to all the controls on the dialog
    // We fetch the trip id from the bundle, gets the trip and fill out the
    // controls
    // -------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);
        edtripstart = (EditText) findViewById(R.id.editstarttime);
        edtripend = (EditText) findViewById(R.id.editendtime);
        edtripmeterstart = (EditText) findViewById(R.id.editmetstart);
        edtripmeterend = (EditText) findViewById(R.id.editmetend);
        edtrippurpose = (EditText) findViewById(R.id.editmission);
        edtripstartadress = (EditText) findViewById(R.id.editadrstart);
        edtripendadress = (EditText) findViewById(R.id.editadrend);
        cbtripwork = (CheckBox) findViewById(R.id.editcbwork);
        cbtriptrack = (CheckBox) findViewById(R.id.cbtrack);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        btnDelete = (Button) findViewById(R.id.btn_delete);
        btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnKml = (Button) findViewById(R.id.btnKml);
        activity = this;
        Bundle showData = getIntent().getExtras();
        rowId = showData.getInt("keyid");
        // Toast.makeText(getApplicationContext(), Integer.toString(rowId),
        // 500).show();
        dbHandler = new DatabaseHandler(getApplicationContext());

        tr = dbHandler.getTrip(rowId);

        if (tr != null) {

            edtripstart.setText(tr.getStart());

            edtripend.setText(tr.getStop());
            edtripmeterstart.setText(GpsService.dec1dig.format(tr.getMeterstart())); //xcv
            edtripmeterend.setText(GpsService.dec1dig.format(tr.getMeterstop())); //xcv
            edtrippurpose.setText(tr.getPurpose());
            edtripstartadress.setText(tr.getAdressstart());
            edtripendadress.setText(tr.getAdressstop());
            cbtripwork.setChecked(tr.getWork() > 0);
            if (dbHandler.getPoint(rowId) != null) {
                cbtriptrack.setChecked(true);
                btnKml.setEnabled(true);
            } else {
                cbtriptrack.setChecked(false);
                btnKml.setEnabled(false);
            }

            // This will make the soft keyboard go away until you actually click
            // in an editbox
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }

        // -------------------------------------------------------------------------------------------------
        // Button "Update"
        // onClicklistener
        // We fetch the values and update all values except the trip id (not
        // shown in the dialog)
        // -------------------------------------------------------------------------------------------------
        btnUpdate.setOnClickListener(new OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View arg0) {

                if (tr != null) {
                    tr.setStart(edtripstart.getText().toString());
                    tr.setStop(edtripend.getText().toString());
                    try {
                        tr.setMeterstart(Float.parseFloat(edtripmeterstart.getText().toString()));
                        tr.setMeterstop(Float.parseFloat(edtripmeterend.getText().toString()));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    tr.setPurpose(edtrippurpose.getText().toString());
                    tr.setAdressstart(edtripstartadress.getText().toString());
                    tr.setAdressstop(edtripendadress.getText().toString());
                    tr.setWork(cbtripwork.isChecked() ? 1 : 0);
                    if (cbtriptrack.isChecked() == false)
                        dbHandler.deletePoint(rowId);
                    dbHandler.updateTrip(tr);

                    EditFragment.cursor.requery();
                    EditFragment.adapter.notifyDataSetChanged();
                }
                dbHandler.close();
                finish();
            }
        });

        // -------------------------------------------------------------------------------------------------
        // Button "Delete"
        // onClicklistener
        // Ask one more time if you are certain that you want to delete the trip
        // -------------------------------------------------------------------------------------------------
        btnDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Extra warning when deleting
                new AlertDialog.Builder(activity).setTitle(getString(R.string.edittrip_dlgdeletetitle)).setMessage(getString(R.string.edittrip_dlgdeletemsg))
                        .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @SuppressWarnings("deprecation")
                            public void onClick(DialogInterface dialog, int which) {
                                dbHandler.deleteTrip(rowId);
                                dbHandler.deletePoint(rowId);

                                EditFragment.cursor.requery();
                                EditFragment.adapter.notifyDataSetChanged();

                                dbHandler.close();
                                finish();
                            }
                        }).setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });

        // -------------------------------------------------------------------------------------------------
        // Button "Export to kml"
        // onClicklistener
        // Enabled if we got a track to export.
        // -------------------------------------------------------------------------------------------------
        btnKml.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ExportKml kml = new ExportKml(getApplicationContext(), rowId);
                kml.export();

                if (kml.getFileName().equals(""))
                    return;

                // Start intent with kml-file
                File f = new File(kml.getFileName());
                Uri earthURI = Uri.fromFile(f);
                Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW, earthURI);
                startActivity(earthIntent);
            }
        });

        // -------------------------------------------------------------------------------------------------
        // Button "Cancel"
        // onClicklistener
        // Just cancel
        // -------------------------------------------------------------------------------------------------
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                dbHandler.close();
                finish();
            }
        });
    }

}
