package jp.nvzk.iotprojectandroid.model;

import java.io.Serializable;

/**
 * Created by user on 15/08/10.
 */
public class Member implements Serializable {
    private String name;
    private String id;
    private int point;
    private int status;
    private GPS gps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public GPS getGps() {
        return gps;
    }

    public void setGps(GPS gps) {
        this.gps = gps;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
