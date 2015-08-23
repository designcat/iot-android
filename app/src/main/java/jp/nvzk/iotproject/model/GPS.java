package jp.nvzk.iotproject.model;

import android.location.Location;

/**
 * Created by user on 15/08/09.
 */
public class GPS {
    private double latitude;
    private double longitude;

    public double getLat() {
        return latitude;
    }

    public void setLat(double lat) {
        this.latitude = lat;
    }

    public double getLng() {
        return longitude;
    }

    public void setLng(double lng) {
        this.longitude = lng;
    }

    public void setLocation(Location location){
        setLat(location.getLatitude());
        setLng(location.getLongitude());
    }
}
