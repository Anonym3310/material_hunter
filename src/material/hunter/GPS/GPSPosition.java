package material.hunter.GPS;

import java.util.Locale;

/**
 * Created by Danial on 2/23/2015.
 * https://github.com/danialgoodwin/android-app-samples/blob/master/gps-satellite-nmea-info/app/src/main/java/net/simplyadvanced/gpsandsatelliteinfo/GpsPosition.java
 */

public class GPSPosition {

    private final int quality = 0;
    public float time = 0.0f;

    public void updateIsfixed() {
        boolean isFixed = quality > 0;
    }

    @Override
    public String toString() {
        float latitude = 0.0f;
        float longitude = 0.0f;
        float direction = 0.0f;
        float altitude = 0.0f;
        float velocity = 0.0f;
        return String.format(Locale.getDefault(), "GpsPosition: latitude: %f, longitude: %f, time: %f, quality: %d, " +
                        "direction: %f, altitude: %f, velocity: %f", latitude, longitude, time, quality,
                direction, altitude, velocity);
    }

}