/*
 * ExportKml - For exporting kml-file (often opens in Google Earth)
 * Oruxmap works ok too. 
 * 
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormatSymbols;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

public class ExportKml {

    String OUTDIR = LogFragment.PATH_APPEXP;
    Context ctx;
    DatabaseHandler dbHandler;
    int TripId;
    private String filename;

    // -------------------------------------------------------------------------------------------------
    // ExportKml
    // -------------------------------------------------------------------------------------------------
    public ExportKml(Context ctx, int TripId) {
        this.ctx = ctx;
        this.TripId = TripId;
    }

    // -------------------------------------------------------------------------------------------------
    // getdb
    // The query to get cursor in points for a trip
    // -------------------------------------------------------------------------------------------------
    private Cursor getdb() {
        dbHandler = new DatabaseHandler(ctx);
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM " + GpsService.POINTS_TABLE + " WHERE " + DatabaseHandler.KEY_ID + " =" + TripId + " ORDER BY "
                + DatabaseHandler.KEY_TIMESTAMP + " ASC");
        Cursor cur = dbHandler.getCursorQuery(query.toString());

        return cur;
    }

    // -------------------------------------------------------------------------------------------------
    // export
    // The export function
    // -------------------------------------------------------------------------------------------------
    public void export() {
        Cursor cur = getdb();

        DecimalFormatSymbols point = new DecimalFormatSymbols();
        point.setDecimalSeparator('.');
        GpsService.dec7dig.setDecimalFormatSymbols(point);

        try {
            if (cur.moveToFirst() && cur != null) {
                String beginTimestamp = null;
                String timestamp = null;

                // Create a filename by taking the first point
                // and read the timestamp
                timestamp = cur.getString(1).replace(":", "");
                filename = OUTDIR + timestamp + ".kml";
                BufferedWriter bufWr = new BufferedWriter(new FileWriter(filename));

                // Write the first part of file
                createHeader(bufWr);

                do {
                    timestamp = cur.getString(1);
                    if (beginTimestamp == null) {
                        beginTimestamp = timestamp;
                    }

                    bufWr.write(GpsService.dec7dig.format(cur.getDouble(3)));
                    bufWr.write(",");
                    bufWr.write(GpsService.dec7dig.format(cur.getDouble(2)));
                    bufWr.write(",");
                    bufWr.write(GpsService.dec7dig.format(cur.getFloat(4)));
                    bufWr.write("\n");
                } while (cur.moveToNext());

                closeFileBuf(bufWr);

                bufWr.flush();
                bufWr.close();

                Toast.makeText(ctx, ctx.getResources().getString(R.string.export_ok), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, ctx.getResources().getString(R.string.export_none), Toast.LENGTH_LONG).show();
                filename = "";
            }
        } catch (FileNotFoundException fnfe) {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.export_err1), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ctx, ctx.getResources().getString(R.string.export_err2) + e.getMessage(), Toast.LENGTH_LONG).show();
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
            buf.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            buf.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
            buf.write("  <Document>\n");
            buf.write("    <name>" + getShortFileName() + "</name>\n");
            buf.write("    <description>KML export</description>\n");
            buf.write("    <Style id=\"myStyle\">\n");
            buf.write("      <LineStyle>\n");
            buf.write("        <color>ffff0000</color>\n");
            buf.write("        <width>3</width>\n");
            buf.write("      </LineStyle>\n");
            buf.write("      <PolyStyle>\n");
            buf.write("        <color>7fff0000</color>\n"); // Alpha,Blue,Green,Red
            // AABBGGRR
            buf.write("      </PolyStyle>\n");
            buf.write("    </Style>\n");
            buf.write("    <Placemark>\n");
            buf.write("      <name>Absolute Extruded</name>\n");
            buf.write("      <description>Transparent blue lines</description>\n");
            buf.write("      <styleUrl>#myStyle</styleUrl>\n");
            buf.write("      <LineString>\n");
            buf.write("        <extrude>0</extrude>\n");
            buf.write("        <tessellate>1</tessellate>\n");
            buf.write("        <altitudeMode>clampToGround</altitudeMode>\n");
            buf.write("        <coordinates>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // -------------------------------------------------------------------------------------------------
    // closeFileBuf
    // -------------------------------------------------------------------------------------------------
    private void closeFileBuf(BufferedWriter buf) {
        try {
            buf.write("        </coordinates>\n");
            buf.write("     </LineString>\n");
            buf.write("    </Placemark>\n");
            buf.write("  </Document>\n");
            buf.write("</kml>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------------------------------
    // getFileName
    // return path and filename
    // -------------------------------------------------------------------------------------------------
    public String getFileName() {
        return filename;
    }

    // -------------------------------------------------------------------------------------------------
    // getShortFilename
    // returns filename only
    // -------------------------------------------------------------------------------------------------
    public String getShortFileName() {
        return filename.substring(filename.length() - 19);
    }

}
