package cgeo.geocaching.unifiedmap;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;

import androidx.annotation.NonNull;

/**
 * Converts between c:geo's WGS84 coordinates and the coordinate system used by a map provider.
 */
public interface MapCoordinateConverter {

    MapCoordinateConverter IDENTITY = new MapCoordinateConverter() {
        @Override
        @NonNull
        public Geopoint toMap(@NonNull final Geopoint geopoint) {
            return geopoint;
        }

        @Override
        @NonNull
        public Geopoint fromMap(@NonNull final Geopoint geopoint) {
            return geopoint;
        }
    };

    @NonNull
    Geopoint toMap(@NonNull Geopoint geopoint);

    @NonNull
    Geopoint fromMap(@NonNull Geopoint geopoint);

    @NonNull
    default Viewport toMap(@NonNull final Viewport viewport) {
        return convertViewport(viewport, this::toMap);
    }

    @NonNull
    default Viewport fromMap(@NonNull final Viewport viewport) {
        return convertViewport(viewport, this::fromMap);
    }

    @NonNull
    static Viewport convertViewport(@NonNull final Viewport viewport, @NonNull final java.util.function.Function<Geopoint, Geopoint> converter) {
        final Geopoint bottomLeft = converter.apply(viewport.bottomLeft);
        final Geopoint bottomRight = converter.apply(new Geopoint(viewport.getLatitudeMin(), viewport.getLongitudeMax()));
        final Geopoint topLeft = converter.apply(new Geopoint(viewport.getLatitudeMax(), viewport.getLongitudeMin()));
        final Geopoint topRight = converter.apply(viewport.topRight);

        final double latitudeMin = Math.min(Math.min(bottomLeft.getLatitude(), bottomRight.getLatitude()), Math.min(topLeft.getLatitude(), topRight.getLatitude()));
        final double latitudeMax = Math.max(Math.max(bottomLeft.getLatitude(), bottomRight.getLatitude()), Math.max(topLeft.getLatitude(), topRight.getLatitude()));
        final double longitudeMin = Math.min(Math.min(bottomLeft.getLongitude(), bottomRight.getLongitude()), Math.min(topLeft.getLongitude(), topRight.getLongitude()));
        final double longitudeMax = Math.max(Math.max(bottomLeft.getLongitude(), bottomRight.getLongitude()), Math.max(topLeft.getLongitude(), topRight.getLongitude()));
        return new Viewport(latitudeMin, longitudeMin, latitudeMax, longitudeMax);
    }
}
