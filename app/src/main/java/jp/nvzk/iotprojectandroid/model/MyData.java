package jp.nvzk.iotprojectandroid.model;

/**
 * Created by user on 15/08/09.
 */
public class MyData {
    private String id;
    private String name;
    private Sensor sensor;
    private GPS gps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
