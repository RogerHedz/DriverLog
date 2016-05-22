/*
 * Points - for storing points in track
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

public class Points {

    int _id;
    String timestamp;
    double alt, lat, lon;
    float acc, speed, bearing, dist;

    public Points() {

    }

    public Points(int _id, String timestamp, double lat, double lon, double alt, float acc, float speed, float bearing, float dist) {
        this._id = _id;
        this.timestamp = timestamp;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.acc = acc;
        this.speed = speed;
        this.bearing = bearing;
        this.dist = dist;

    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getAlt() {
        return alt;
    }

    public void setAlt(double alt) {
        this.alt = alt;
    }

    public float getAcc() {
        return acc;
    }

    public void setAcc(float acc) {
        this.acc = acc;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getDist() {
        return dist;
    }

    public void setDist(float dist) {
        this.dist = dist;
    }

}
