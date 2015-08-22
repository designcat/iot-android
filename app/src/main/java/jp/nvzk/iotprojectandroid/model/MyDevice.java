package jp.nvzk.iotprojectandroid.model;

/**
 * Created by user on 15/08/09.
 */
public class MyDevice {
    private int side;
    private Sensor sensor;
    private GPS gps;

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public GPS getGps() {
        return gps;
    }

    public void setGps(GPS gps) {
        this.gps = gps;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
