package cgeo.geocaching.unifiedmap;

import cgeo.geocaching.location.Geopoint;

import androidx.annotation.NonNull;

/**
 * Converts WGS84 coordinates to and from GCJ-02, as used by mainland Chinese online maps.
 */
public final class Gcj02CoordinateConverter implements MapCoordinateConverter {

    public static final Gcj02CoordinateConverter INSTANCE = new Gcj02CoordinateConverter();

    private static final double EARTH_SEMIMAJOR_AXIS = 6378245.0;
    private static final double ECCENTRICITY_SQUARED = 0.00669342162296594323;
    private static final int INVERSE_ITERATIONS = 4;

    private Gcj02CoordinateConverter() {
        // singleton
    }

    @Override
    @NonNull
    public Geopoint toMap(@NonNull final Geopoint wgs84) {
        final double latitude = wgs84.getLatitude();
        final double longitude = wgs84.getLongitude();
        if (isOutsideChina(latitude, longitude)) {
            return wgs84;
        }

        double latitudeDelta = transformLatitude(longitude - 105.0, latitude - 35.0);
        double longitudeDelta = transformLongitude(longitude - 105.0, latitude - 35.0);
        final double latitudeRadians = Math.toRadians(latitude);
        final double sinLatitude = Math.sin(latitudeRadians);
        final double magic = 1 - ECCENTRICITY_SQUARED * sinLatitude * sinLatitude;
        final double sqrtMagic = Math.sqrt(magic);
        latitudeDelta = Math.toDegrees(latitudeDelta * sqrtMagic * magic / (EARTH_SEMIMAJOR_AXIS * (1 - ECCENTRICITY_SQUARED)));
        longitudeDelta = Math.toDegrees(longitudeDelta * sqrtMagic / (EARTH_SEMIMAJOR_AXIS * Math.cos(latitudeRadians)));
        return new Geopoint(latitude + latitudeDelta, longitude + longitudeDelta);
    }

    @Override
    @NonNull
    public Geopoint fromMap(@NonNull final Geopoint gcj02) {
        if (isOutsideChina(gcj02.getLatitude(), gcj02.getLongitude())) {
            return gcj02;
        }

        Geopoint estimate = gcj02;
        for (int i = 0; i < INVERSE_ITERATIONS; i++) {
            final Geopoint convertedEstimate = toMap(estimate);
            estimate = new Geopoint(
                    estimate.getLatitude() + gcj02.getLatitude() - convertedEstimate.getLatitude(),
                    estimate.getLongitude() + gcj02.getLongitude() - convertedEstimate.getLongitude());
        }
        return estimate;
    }

    private static boolean isOutsideChina(final double latitude, final double longitude) {
        return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271;
    }

    private static double transformLatitude(final double x, final double y) {
        double result = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        result += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        result += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return result;
    }

    private static double transformLongitude(final double x, final double y) {
        double result = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        result += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        result += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        result += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return result;
    }
}
