package cgeo.geocaching.unifiedmap.mapsforge;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;

import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.LatLongUtils;
import org.oscim.core.BoundingBox;

/** A WGS84 viewport converted for one bounds update on the current Mapsforge map source. */
final class MapsforgeBounds {

    private final BoundingBox mapBounds;
    private final Geopoint wgs84Center;
    private final boolean point;

    MapsforgeBounds(final Viewport wgs84Bounds, final MapCoordinateConverter converter) {
        final Viewport convertedBounds = converter.toMap(wgs84Bounds);
        final BoundingBox bounds = new BoundingBox(convertedBounds.bottomLeft.getLatitudeE6(), convertedBounds.bottomLeft.getLongitudeE6(),
                convertedBounds.topRight.getLatitudeE6(), convertedBounds.topRight.getLongitudeE6());
        point = bounds.getLatitudeSpan() == 0 && bounds.getLongitudeSpan() == 0;
        // Add some margin to avoid cutting off items at the edge, as Google Maps does implicitly.
        mapBounds = point ? bounds : bounds.extendMargin(1.1f);
        wgs84Center = converter.fromMap(new Geopoint(mapBounds.getCenterPoint().getLatitude(), mapBounds.getCenterPoint().getLongitude()));
    }

    boolean isPoint() {
        return point;
    }

    /** Pass directly to MapView; this center is already in the map source's coordinate system. */
    LatLong getMapCenter() {
        return new LatLong(mapBounds.getCenterPoint().getLatitude(), mapBounds.getCenterPoint().getLongitude());
    }

    /** Use for application state, including restoring the center after a map source change. */
    Geopoint getWgs84Center() {
        return wgs84Center;
    }

    byte getZoom(final Dimension dimension, final int tileSize) {
        return LatLongUtils.zoomForBounds(dimension,
                new org.mapsforge.core.model.BoundingBox(mapBounds.getMinLatitude(), mapBounds.getMinLongitude(), mapBounds.getMaxLatitude(), mapBounds.getMaxLongitude()), tileSize);
    }
}
