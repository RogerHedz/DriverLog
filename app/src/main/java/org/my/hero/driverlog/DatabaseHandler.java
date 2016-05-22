/*
 * DatabasHandler - a class for handling sqlite database
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 * Handles two tables - 
 * One for storing the trip with start and end time, meter reading, 
 * adress for the start and end, your mission etc
 * The other is for position data - also with altitude, accuracy, speed, bearing 
 * (dist not implemented yet)
 */
package org.my.hero.driverlog;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    String TAG = "DatabaseHandler";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    public static final String POINTS_TABLE = "POINTS";
    public static final String TRIPS_TABLE = "TRIPS";

    // All Static variables

    // Points Table Columns names
    public static final String KEY_ID = "_id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";
    public static final String KEY_ALT = "alt";
    public static final String KEY_ACC = "acc";
    public static final String KEY_SPEED = "speed";
    public static final String KEY_BEARING = "bearing";
    public static final String KEY_DIST = "dist";

    // Trips table Column names
    // public static final String KEY_id = "_id"; // same as above
    public static final String KEY_CAR = "car";
    public static final String KEY_START = "timestart";
    public static final String KEY_STOP = "timestop";
    public static final String KEY_METERSTART = "meterstart";
    public static final String KEY_METERSTOP = "meterstop";
    public static final String KEY_TRIP = "trip";
    public static final String KEY_LATSTART = "latstart";
    public static final String KEY_LONSTART = "lonstart";
    public static final String KEY_LATSTOP = "latstop";
    public static final String KEY_LONSTOP = "lonstop";
    public static final String KEY_ADRSTART = "adrstart";
    public static final String KEY_ADRSTOP = "adrstop";
    public static final String KEY_PURPOSE = "purpose";
    public static final String KEY_WORK = "work";

    public DatabaseHandler(Context context) {
        super(context, LogFragment.PATH_APPDB + LogFragment.DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POINTS_TABLE = "CREATE TABLE IF NOT EXISTS " + POINTS_TABLE + " (" + KEY_ID + " INTEGER  ,"
                + KEY_TIMESTAMP + " TEXT," + KEY_LAT + " REAL," + KEY_LON + " REAL," + KEY_ALT + " REAL," + KEY_ACC
                + " REAL," + KEY_SPEED + " REAL," + KEY_BEARING + " REAL," + KEY_DIST + " REAL" + ");";
        db.execSQL(CREATE_POINTS_TABLE);

        String CREATE_TRIPS_TABLE = "CREATE TABLE IF NOT EXISTS " + TRIPS_TABLE + " (" + KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_CAR + " TEXT," + KEY_START + " TEXT," + KEY_STOP
                + " TEXT," + KEY_METERSTART + " REAL," + KEY_METERSTOP + " REAL," + KEY_TRIP + " REAL," + KEY_LATSTART
                + " REAL," + KEY_LONSTART + " REAL," + KEY_LATSTOP + " REAL," + KEY_LONSTOP + " REAL," + KEY_ADRSTART
                + " TEXT," + KEY_ADRSTOP + " TEXT," + KEY_PURPOSE + " TEXT," + KEY_WORK + " INTEGER" + ");";
        db.execSQL(CREATE_TRIPS_TABLE);

    }

    // -------------------------------------------------------------------------------------------------
    // Upgrading database
    // Would be more creative to save the old, create the new and import.
    // Now we just delete and create new
    // -------------------------------------------------------------------------------------------------
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + POINTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TRIPS_TABLE);

        // Create tables again
        onCreate(db);
    }

    // -------------------------------------------------------------------------------------------------
    // Dealing with the points table
    // Adding new point - Argument is the Points to be updated
    // -------------------------------------------------------------------------------------------------
    void addPoint(Points pnt) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues val = new ContentValues();

        val.put(KEY_ID, pnt.getId());
        val.put(KEY_TIMESTAMP, pnt.getTimestamp());
        val.put(KEY_LAT, pnt.getLat());
        val.put(KEY_LON, pnt.getLon());
        val.put(KEY_ALT, pnt.getAlt());
        val.put(KEY_ACC, pnt.getAcc());
        val.put(KEY_SPEED, pnt.getSpeed());
        val.put(KEY_BEARING, pnt.getBearing());
        val.put(KEY_DIST, pnt.getDist());

        // Inserting Row
        db.insert(POINTS_TABLE, null, val);
        db.close(); // Closing database connection
    }

    // -------------------------------------------------------------------------------------------------
    // Getting single point by id
    // Returns Points if found, null if not - Argument is the _id (trip id)
    // -------------------------------------------------------------------------------------------------
    Points getPoint(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        cursor = db.query(POINTS_TABLE, new String[]{KEY_ID, KEY_TIMESTAMP, KEY_LAT, KEY_LON, KEY_ALT, KEY_ACC,
                        KEY_SPEED, KEY_BEARING, KEY_DIST}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null,
                null, null);
        if (cursor.moveToFirst()) {

            Points point = new Points(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                    Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3)),
                    Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                    Float.parseFloat(cursor.getString(6)), Float.parseFloat(cursor.getString(7)),
                    Float.parseFloat(cursor.getString(8)));
            cursor.close();
            return point;
        } else {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Getting all points
    // Returns a list with all Points - careful - could be huge
    // -------------------------------------------------------------------------------------------------
    public List<Points> getAllPoints() {
        List<Points> pointList = new ArrayList<Points>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + POINTS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Points point = new Points();
                point.setId(Integer.parseInt(cursor.getString(0)));
                point.setTimestamp(cursor.getString(1));
                point.setLat(Double.parseDouble(cursor.getString(2)));
                point.setLon(Double.parseDouble(cursor.getString(3)));
                point.setAlt(Float.parseFloat(cursor.getString(4)));
                point.setAcc(Float.parseFloat(cursor.getString(5)));
                point.setSpeed(Float.parseFloat(cursor.getString(6)));
                point.setBearing(Float.parseFloat(cursor.getString(7)));
                point.setDist(Float.parseFloat(cursor.getString(8)));

                // Adding point to list
                pointList.add(point);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // return list
        return pointList;
    }

    // -------------------------------------------------------------------------------------------------
    // Updating a single point
    // Returns the TripId for the Points in the argument
    // -------------------------------------------------------------------------------------------------
    public int updatePoint(Points point) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TIMESTAMP, point.getTimestamp());
        values.put(KEY_LAT, point.getLat());
        values.put(KEY_LON, point.getLon());
        values.put(KEY_ALT, point.getAlt());
        values.put(KEY_ACC, point.getAcc());
        values.put(KEY_SPEED, point.getSpeed());
        values.put(KEY_BEARING, point.getBearing());
        values.put(KEY_DIST, point.getDist());

        // updating row
        return db.update(POINTS_TABLE, values, KEY_ID + " = ?", new String[]{String.valueOf(point.getId())});
    }

    // -------------------------------------------------------------------------------------------------
    // Deleting a single point
    // Argument = Point to be deleted
    // -------------------------------------------------------------------------------------------------
    public void deletePoint(Points point) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(POINTS_TABLE, KEY_ID + " = ?", new String[]{String.valueOf(point.getId())});
        db.close();
    }

    // -------------------------------------------------------------------------------------------------
    // Deleting a single point
    // Argument = Id of Point to be deleted
    // -------------------------------------------------------------------------------------------------
    public void deletePoint(int _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(POINTS_TABLE, KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        db.close();
    }

    // -------------------------------------------------------------------------------------------------
    // Get the number of points
    // -------------------------------------------------------------------------------------------------
    public int getPointCount() {
        String countQuery = "SELECT  * FROM " + POINTS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        // return count
        return cnt;
    }

    // -------------------------------------------------------------------------------------------------
    // Dealing with the trip table
    // Adding new trip - Argument is the Trips to be updated
    // -------------------------------------------------------------------------------------------------
    void addTrip(Trips trip) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // KEY_CAR,KEY_START,KEY_STOP,KEY_METERSTART,KEY_METERSTOP,KEY_TRIP,KEY_LATSTART,KEY_LONSTART,KEY_LATSTOP,KEY_LONSTOP,KEY_ADRSTART,KEY_ADRSTOP,KEY_PURPOSE,KEY_WORK
        values.put(KEY_CAR, trip.getCar());
        values.put(KEY_START, trip.getStart());
        values.put(KEY_STOP, trip.getStop());
        values.put(KEY_METERSTART, trip.getMeterstart());
        values.put(KEY_METERSTOP, trip.getMeterstop());
        values.put(KEY_TRIP, trip.getTrip());
        values.put(KEY_LATSTART, trip.getLatstart());
        values.put(KEY_LONSTART, trip.getLonstart());
        values.put(KEY_LATSTOP, trip.getLatstop());
        values.put(KEY_LONSTOP, trip.getLonstop());
        values.put(KEY_ADRSTART, trip.getAdressstart());
        values.put(KEY_ADRSTOP, trip.getAdressstop());
        values.put(KEY_PURPOSE, trip.getPurpose());
        values.put(KEY_WORK, trip.getWork());

        // Inserting Row
        db.insert(TRIPS_TABLE, null, values);
        db.close(); // Closing database connection
    }

    // -------------------------------------------------------------------------------------------------
    // Getting single trip by id
    // Returns Trips if found, null if not - Argument is the _id (trip id)
    // -------------------------------------------------------------------------------------------------
    Trips getTrip(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (id < 0)
            return null;

        Cursor cursor = db.query(TRIPS_TABLE, new String[]{KEY_ID, KEY_CAR, KEY_START, KEY_STOP, KEY_METERSTART,
                        KEY_METERSTOP, KEY_TRIP, KEY_LATSTART, KEY_LONSTART, KEY_LATSTOP, KEY_LONSTOP, KEY_ADRSTART,
                        KEY_ADRSTOP, KEY_PURPOSE, KEY_WORK}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null,
                null, null);
        Trips trip = null;

        if (cursor != null) {
            cursor.moveToFirst();

            trip = new Trips(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                    Float.parseFloat(cursor.getString(6)), Double.parseDouble(cursor.getString(7)),
                    Double.parseDouble(cursor.getString(8)), Double.parseDouble(cursor.getString(9)),
                    Double.parseDouble(cursor.getString(10)), cursor.getString(11), cursor.getString(12),
                    cursor.getString(13), Integer.parseInt(cursor.getString(14)));
            cursor.close();
        }
        return trip;
    }

    // -------------------------------------------------------------------------------------------------
    // Getting the last trip saved (last by timestamp)
    // Returns Trips if found, null if not
    // -------------------------------------------------------------------------------------------------
    Trips getLastTrip() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT " + KEY_ID + "," + KEY_CAR + "," + KEY_START + "," + KEY_STOP + ","
                + KEY_METERSTART + "," + KEY_METERSTOP + "," + KEY_TRIP + "," + KEY_LATSTART + "," + KEY_LONSTART + ","
                + KEY_LATSTOP + "," + KEY_LONSTOP + "," + KEY_ADRSTART + "," + KEY_ADRSTOP + "," + KEY_PURPOSE + ","
                + KEY_WORK + " FROM " + TRIPS_TABLE + " ORDER BY " + KEY_ID + " ASC", null);
        if (cursor != null) {
            if (cursor.getCount() == 0)
                return null;

            cursor.moveToLast();

            try {
                Trips trip = new Trips(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor
                        .getString(5)), Float.parseFloat(cursor.getString(6)), Double.parseDouble(cursor
                        .getString(7)), Double.parseDouble(cursor.getString(8)), Double.parseDouble(cursor
                        .getString(9)), Double.parseDouble(cursor.getString(10)), cursor.getString(11),
                        cursor.getString(12), cursor.getString(13), Integer.parseInt(cursor.getString(14)));

                // return
                cursor.close();
                return trip;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------
    // Getting single trip by timestamp
    // Returns Trips if found, null if not - Argument is the timestamp (YYYY-MM-DD HH:MM:SS) as string
    // -------------------------------------------------------------------------------------------------
    Trips getTrip(String ts) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TRIPS_TABLE, new String[]{KEY_ID, KEY_CAR, KEY_START, KEY_STOP, KEY_METERSTART,
                KEY_METERSTOP, KEY_TRIP, KEY_LATSTART, KEY_LONSTART, KEY_LATSTOP, KEY_LONSTOP, KEY_ADRSTART,
                KEY_ADRSTOP, KEY_PURPOSE, KEY_WORK}, KEY_START + "=?", new String[]{ts}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Trips trip = new Trips(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2),
                cursor.getString(3), Float.parseFloat(cursor.getString(4)), Float.parseFloat(cursor.getString(5)),
                Float.parseFloat(cursor.getString(6)), Double.parseDouble(cursor.getString(7)),
                Double.parseDouble(cursor.getString(8)), Double.parseDouble(cursor.getString(9)),
                Double.parseDouble(cursor.getString(10)), cursor.getString(11), cursor.getString(12),
                cursor.getString(13), Integer.parseInt(cursor.getString(14)));

        cursor.close();
        return trip;
    }

    // -------------------------------------------------------------------------------------------------
    // Get the cursor by query
    // Returns a cursor for the selectQuery
    // Remeber to close the cursor when finished
    // Don't add a ';' at the end of the query
    // -------------------------------------------------------------------------------------------------
    public Cursor getCursorQuery(String selectQuery) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // -------------------------------------------------------------------------------------------------
    // Getting all trips
    // Returns a list with all trips
    // -------------------------------------------------------------------------------------------------
    public List<Trips> getAllTrips() {
        List<Trips> tripList = new ArrayList<Trips>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TRIPS_TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Trips trip = new Trips();
                trip.setId(Integer.parseInt(cursor.getString(0)));
                trip.setCar(cursor.getString(1));
                trip.setStart(cursor.getString(2));
                trip.setStop(cursor.getString(3));
                trip.setMeterstart(Float.parseFloat(cursor.getString(4)));
                trip.setMeterstop(Float.parseFloat(cursor.getString(5)));
                trip.setTrip(Float.parseFloat(cursor.getString(6)));
                trip.setLatstart(Double.parseDouble(cursor.getString(7)));
                trip.setLonstart(Double.parseDouble(cursor.getString(8)));
                trip.setLatstop(Double.parseDouble(cursor.getString(9)));
                trip.setLonstop(Double.parseDouble(cursor.getString(10)));
                trip.setAdressstart(cursor.getString(11));
                trip.setAdressstop(cursor.getString(12));
                trip.setPurpose(cursor.getString(13));
                trip.setWork(Integer.parseInt(cursor.getString(04)));

                // Adding to list
                tripList.add(trip);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // return list
        return tripList;
    }

    // -------------------------------------------------------------------------------------------------
    // Updating a single trip
    // Returns the TripId for the trip updated
    // -------------------------------------------------------------------------------------------------
    public int updateTrip(Trips trip) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CAR, trip.getCar());
        values.put(KEY_START, trip.getStart());
        values.put(KEY_STOP, trip.getStop());
        values.put(KEY_METERSTART, trip.getMeterstart());
        values.put(KEY_METERSTOP, trip.getMeterstop());
        values.put(KEY_TRIP, trip.getTrip());
        values.put(KEY_LATSTART, trip.getLatstart());
        values.put(KEY_LONSTART, trip.getLonstart());
        values.put(KEY_LATSTOP, trip.getLatstop());
        values.put(KEY_LONSTOP, trip.getLonstop());
        values.put(KEY_ADRSTART, trip.getAdressstart());
        values.put(KEY_ADRSTOP, trip.getAdressstop());
        values.put(KEY_PURPOSE, trip.getPurpose());
        values.put(KEY_WORK, trip.getWork());

        // updating row
        return db.update(TRIPS_TABLE, values, KEY_ID + " = ?", new String[]{String.valueOf(trip.getId())});
    }

    // -------------------------------------------------------------------------------------------------
    // Deleting a single trip
    // Argument = Trip to be deleted
    // -------------------------------------------------------------------------------------------------
    public void deleteTrip(Trips trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRIPS_TABLE, KEY_ID + " = ?", new String[]{String.valueOf(trip.getId())});
        db.close();
    }

    // -------------------------------------------------------------------------------------------------
    // Deleting a single trip
    // Argument = TripId to be deleted
    // -------------------------------------------------------------------------------------------------
    public void deleteTrip(int _id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TRIPS_TABLE, KEY_ID + " = ?", new String[]{String.valueOf(_id)});
        db.close();
    }

    // -------------------------------------------------------------------------------------------------
    // Get the number of trips
    // -------------------------------------------------------------------------------------------------
    public int getTripsCount() {
        String countQuery = "SELECT  * FROM " + TRIPS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        // return count
        return cnt;
    }

}
