package jp.nvzk.iotproject.model;

import android.location.Location;

/**
 * Created by user on 15/08/09.
 */
public class GPS {
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setLocation(Location location){
        setLat(location.getLatitude());
        setLng(location.getLongitude());
    }
}
