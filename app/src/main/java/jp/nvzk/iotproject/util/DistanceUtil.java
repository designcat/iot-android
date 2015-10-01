package jp.nvzk.iotproject.util;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by user on 15/09/19.
 */
public class DistanceUtil {

    public static double getDistance(double latO, double lngO, double latS, double lngS) {
        double r = 6378.137; // 赤道半径[km]

        double lat1 = latO * PI / 180;
        double lng1 = lngO * PI / 180;

        double lat2 = latS * PI / 180;
        double lng2 = lngS * PI / 180;

        // 2点間の距離[km]
        double distanceKm = r * acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(lng2 - lng1));
        double distanceM = distanceKm * 1000;

        return distanceM;
    }
}
