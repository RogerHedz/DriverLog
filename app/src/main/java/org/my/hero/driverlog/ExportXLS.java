/*
 * ExportXLS - For exporting excel file (actually tab delimited, but by setting 
 * extension to xls excel imports to columns directly
 * 
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class ExportXLS {

    private String startdate, enddate, filename;
    private int wp; // Work or private
    String TAG = "ExportXls";
    String OUTDIR = LogFragment.PATH_APPEXP;
    Fragment frag;
    DatabaseHandler dbHandler;

    // -------------------------------------------------------------------------------------------------
    // ExportXLS
    // -------------------------------------------------------------------------------------------------
    public ExportXLS(Fragment frag, String from, String to, int wp) {
        startdate = from;
        enddate = to;
        this.wp = wp;
        this.frag = frag;
        Log.d(TAG, "ExportXLS: create");
    }

    // -------------------------------------------------------------------------------------------------
    // getdb
    // -------------------------------------------------------------------------------------------------
    private Cursor getdb() {
        dbHandler = new DatabaseHandler(frag.getActivity());
        StringBuilder query = new StringBuilder();

        query.append("SELECT " + DatabaseHandler.KEY_ID + " FROM " + GpsService.TRIPS_TABLE + " WHERE "
                + DatabaseHandler.KEY_WORK + " >= " + wp + " AND ( "
                + DatabaseHandler.KEY_START + " BETWEEN '" + startdate + " 00:00:00' AND '");
        query.append(enddate + " 23:59:59') ORDER BY " + DatabaseHandler.KEY_START + " ASC");


        Cursor cur = dbHandler.getCursorQuery(query.toString());
        return cur;
    }

    // -------------------------------------------------------------------------------------------------
    // export
    // -------------------------------------------------------------------------------------------------
    void export() {
        Cursor cur = getdb();
        filename = OUTDIR + startdate.substring(0, 10) + " till " + enddate.substring(0, 10) + ".xls";

        try {
            if (cur.moveToFirst() && cur != null) {
                BufferedWriter bufWr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "ISO-8859-1"));
                createHeader(bufWr);

                String str1, str11 = "", str2, str22 = "", str3, str33 = "";

                do {
                    Trips tr = dbHandler.getTrip(cur.getInt(0));
                    Log.d(TAG, "exporttr: " + tr.getId());
                    bufWr.write(tr.getWork() > 0 ? frag.getResources().getString(R.string.export_xls_wrk) : frag.getResources().getString(R.string.export_xls_priv));
                    bufWr.append("\t");
                    str1 = tr.getPurpose();

                    // Replace all newlines to ;
                    if (str1 != null && str1.length() > 0) {
                        StringBuilder result = new StringBuilder();
                        StringTokenizer t = new StringTokenizer(str1, "\n");
                        while (t.hasMoreTokens()) {
                            result.append(t.nextToken().trim()).append("; ");
                        }
                        str11 = result.toString();
                    }

                    str2 = tr.getAdressstart();
                    if (str2 != null && str2.length() > 0) {
                        StringBuilder result = new StringBuilder();
                        StringTokenizer t = new StringTokenizer(str2, "\n");
                        while (t.hasMoreTokens()) {
                            result.append(t.nextToken().trim()).append("; ");
                        }
                        str22 = result.toString();
                    }

                    str3 = tr.getAdressstop();
                    if (str3 != null && str3.length() > 0) {
                        StringBuilder result = new StringBuilder();
                        StringTokenizer t = new StringTokenizer(str3, "\n");
                        while (t.hasMoreTokens()) {
                            result.append(t.nextToken().trim()).append("; ");
                        }
                        str33 = result.toString();
                    }

                    bufWr.write(str11 + str22 + str33 + "\t");

                    bufWr.write(tr.getStart() + "\t");
                    bufWr.write(tr.getStop() + "\t");
                    bufWr.write(GpsService.dec1dig.format(tr.getMeterstart()) + "\t"); //xcv
                    bufWr.write(GpsService.dec1dig.format(tr.getMeterstop()) + "\t"); //xcv
                    bufWr.write(GpsService.dec1dig.format(tr.getTrip())); //xcv
                    bufWr.newLine();
                } while (cur.moveToNext());

                bufWr.flush();
                bufWr.close();
                Toast.makeText(frag.getActivity(), frag.getResources().getString(R.string.export_ok), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(frag.getActivity(), frag.getResources().getString(R.string.export_none), Toast.LENGTH_LONG).show();
                filename = "";
            }
        } catch (FileNotFoundException fnfe) {
            Toast.makeText(frag.getActivity(), frag.getResources().getString(R.string.export_err1), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(frag.getActivity(), frag.getResources().getString(R.string.export_err2) + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            if (dbHandler != null) {
                dbHandler.close();
            }
        }

    }

    // -------------------------------------------------------------------------------------------------
    // createHeader
    // -------------------------------------------------------------------------------------------------
    private void createHeader(BufferedWriter buf) {
        try {
            buf.write(frag.getResources().getString(R.string.export_xls_header));
            buf.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------------------------------
    // getFilename
    // returns filename and path
    // -------------------------------------------------------------------------------------------------
    public String getFileName() {
        return filename;
    }
}
