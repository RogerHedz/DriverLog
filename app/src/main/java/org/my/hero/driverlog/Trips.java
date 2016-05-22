/*
 * Trips - for storing trips
 * Roger Hedin rohe9600 - Miun DT031G - Applikationsutveckling för Android - SO13
 * 2013-08-31
 * 
 */
package org.my.hero.driverlog;

public class Trips {

    int _id;
    String car, start, stop;
    float meterstart, meterstop, trip;
    double latstart, lonstart, latstop, lonstop;
    String adressstart, adressstop, purpose;
    int work;

    public Trips() {

    }

    public Trips(int _id, String car, String start, String stop, float meterstart, float meterstop, float trip,
                 double latstart, double lonstart, double latstop, double lonstop, String adressstart, String adressstop,
                 String purpose, int work) {
        this._id = _id;
        this.car = car;
        this.start = start;
        this.stop = stop;
        this.meterstart = meterstart;
        this.meterstop = meterstop;
        this.trip = trip;
        this.latstart = latstart;
        this.lonstart = lonstart;
        this.latstop = latstop;
        this.lonstop = lonstop;
        this.adressstart = adressstart;
        this.adressstop = adressstop;
        this.purpose = purpose;
        this.work = work;
    }

    public Trips(String car, String start, String stop, float meterstart, float meterstop, float trip,
                 double latstart, double lonstart, double latstop, double lonstop, String adressstart, String adressstop,
                 String purpose, int work) {
        this.car = car;
        this.start = start;
        this.stop = stop;
        this.meterstart = meterstart;
        this.meterstop = meterstop;
        this.trip = trip;
        this.latstart = latstart;
        this.lonstart = lonstart;
        this.latstop = latstop;
        this.lonstop = lonstop;
        this.adressstart = adressstart;
        this.adressstop = adressstop;
        this.purpose = purpose;
        this.work = work;
    }

    public int getId() {
        return _id;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public float getMeterstart() {
        return meterstart;
    }

    public void setMeterstart(float meterstart) {
        this.meterstart = meterstart;
    }

    public float getMeterstop() {
        return meterstop;
    }

    public void setMeterstop(float meterstop) {
        this.meterstop = meterstop;
    }

    public float getTrip() {
        return trip;
    }

    public void setTrip(float trip) {
        this.trip = trip;
    }

    public double getLatstart() {
        return latstart;
    }

    public void setLatstart(double latstart) {
        this.latstart = latstart;
    }

    public double getLonstart() {
        return lonstart;
    }

    public void setLonstart(double lonstart) {
        this.lonstart = lonstart;
    }

    public double getLatstop() {
        return latstop;
    }

    public void setLatstop(double latstop) {
        this.latstop = latstop;
    }

    public double getLonstop() {
        return lonstop;
    }

    public void setLonstop(double lonstop) {
        this.lonstop = lonstop;
    }

    public String getAdressstart() {
        return adressstart;
    }

    public void setAdressstart(String adressstart) {
        this.adressstart = adressstart;
    }

    public String getAdressstop() {
        return adressstop;
    }

    public void setAdressstop(String adressstop) {
        this.adressstop = adressstop;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

}
