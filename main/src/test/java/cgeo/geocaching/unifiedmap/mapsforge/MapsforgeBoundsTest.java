package cgeo.geocaching.unifiedmap.mapsforge;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;

import org.junit.Test;
import org.mapsforge.core.model.Dimension;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.LatLongUtils;
import org.oscim.core.BoundingBox;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class MapsforgeBoundsTest {

    private static final MapCoordinateConverter GCJ02 = Gcj02CoordinateConverter.INSTANCE;
    private static final Viewport BEIJING = new Viewport(39.90, 116.39, 39.92, 116.41);
    private static final Dimension DIMENSION = new Dimension(600, 600);
    private static final int TILE_SIZE = 256;

    @Test
    public void gcj02BoundsKeepMapCenterSeparateFromWgs84State() {
        final MapsforgeBounds bounds = new MapsforgeBounds(BEIJING, GCJ02);
        final BoundingBox expected = expectedMapBounds(BEIJING, GCJ02).extendMargin(1.1f);
        final Geopoint mapCenter = new Geopoint(expected.getCenterPoint().getLatitude(), expected.getCenterPoint().getLongitude());

        assertThat(bounds.isPoint()).isFalse();
        assertMapCenter(bounds, mapCenter);
        assertThat(bounds.getWgs84Center()).isEqualTo(GCJ02.fromMap(mapCenter));
        assertThat(bounds.getWgs84Center()).isNotEqualTo(mapCenter);
        // The old path treated this GCJ-02 center as WGS84 and applied another toMap conversion.
        assertThat(Math.abs(bounds.getMapCenter().longitude - GCJ02.toMap(mapCenter).getLongitude())).isGreaterThan(0.001);
        assertZoom(bounds, expected);
    }

    @Test
    public void identityBoundsPreserveCenterAndMargin() {
        final Viewport viewport = new Viewport(-1.0, -2.0, 3.0, 4.0);
        final MapsforgeBounds bounds = new MapsforgeBounds(viewport, MapCoordinateConverter.IDENTITY);

        assertThat(bounds.isPoint()).isFalse();
        assertMapCenter(bounds, viewport.getCenter());
        assertThat(bounds.getWgs84Center()).isEqualTo(viewport.getCenter());
        assertZoom(bounds, new BoundingBox(-1.0, -2.0, 3.0, 4.0).extendMargin(1.1f));
        // At this view size the unpadded bounds would fit at level 7; the 10% margin requires level 6.
        assertThat(bounds.getZoom(DIMENSION, TILE_SIZE)).isEqualTo((byte) 6);
    }

    @Test
    public void gcj02SinglePointOnlyNeedsRecentering() {
        final Geopoint point = new Geopoint(39.908823, 116.397470);
        final MapsforgeBounds bounds = new MapsforgeBounds(new Viewport(point), GCJ02);

        // MapsforgeFragment uses isPoint to leave the zoom unchanged, even without a laid-out view.
        assertThat(bounds.isPoint()).isTrue();
        assertMapCenter(bounds, GCJ02.toMap(point));
        assertThat(bounds.getWgs84Center().getLatitude()).isEqualTo(point.getLatitude(), offset(0.000002));
        assertThat(bounds.getWgs84Center().getLongitude()).isEqualTo(point.getLongitude(), offset(0.000002));
    }

    @Test
    public void identitySinglePointOnlyNeedsRecentering() {
        final Geopoint point = new Geopoint(51.5074, -0.1278);
        final MapsforgeBounds bounds = new MapsforgeBounds(new Viewport(point), MapCoordinateConverter.IDENTITY);

        assertThat(bounds.isPoint()).isTrue();
        assertMapCenter(bounds, point);
        assertThat(bounds.getWgs84Center()).isEqualTo(point);
    }

    @Test
    public void savedCenterCanBeReusedWithAnotherMapSource() {
        final MapsforgeBounds gcjBounds = new MapsforgeBounds(BEIJING, GCJ02);
        final Geopoint savedPosition = gcjBounds.getWgs84Center();
        final MapsforgeBounds identityBounds = new MapsforgeBounds(new Viewport(savedPosition), MapCoordinateConverter.IDENTITY);
        final MapsforgeBounds restoredGcjBounds = new MapsforgeBounds(new Viewport(identityBounds.getWgs84Center()), GCJ02);

        assertMapCenter(identityBounds, savedPosition);
        assertThat(restoredGcjBounds.getMapCenter().latitude).isEqualTo(gcjBounds.getMapCenter().latitude, offset(0.000002));
        assertThat(restoredGcjBounds.getMapCenter().longitude).isEqualTo(gcjBounds.getMapCenter().longitude, offset(0.000002));
    }

    @Test
    public void wgs84BoundsCanBeRecalculatedForCurrentMapSource() {
        // A deferred update must retain the WGS84 viewport, not the previous source's map bounds.
        final MapsforgeBounds gcjBounds = new MapsforgeBounds(BEIJING, GCJ02);
        final MapsforgeBounds identityBounds = new MapsforgeBounds(BEIJING, MapCoordinateConverter.IDENTITY);

        assertMapCenter(identityBounds, BEIJING.getCenter());
        assertThat(identityBounds.getMapCenter()).isNotEqualTo(gcjBounds.getMapCenter());
        assertZoom(identityBounds, expectedMapBounds(BEIJING, MapCoordinateConverter.IDENTITY).extendMargin(1.1f));
    }

    private static BoundingBox expectedMapBounds(final Viewport viewport, final MapCoordinateConverter converter) {
        // Use the real converter and map library as the reference, not a copy of the GCJ-02 formula.
        final Viewport mapViewport = converter.toMap(viewport);
        return new BoundingBox(mapViewport.bottomLeft.getLatitudeE6(), mapViewport.bottomLeft.getLongitudeE6(),
                mapViewport.topRight.getLatitudeE6(), mapViewport.topRight.getLongitudeE6());
    }

    private static void assertMapCenter(final MapsforgeBounds bounds, final Geopoint expected) {
        final LatLong center = bounds.getMapCenter();
        assertThat(center.latitude).isEqualTo(expected.getLatitude(), offset(0.000002));
        assertThat(center.longitude).isEqualTo(expected.getLongitude(), offset(0.000002));
    }

    private static void assertZoom(final MapsforgeBounds bounds, final BoundingBox expected) {
        final org.mapsforge.core.model.BoundingBox mapBounds = new org.mapsforge.core.model.BoundingBox(
                expected.getMinLatitude(), expected.getMinLongitude(), expected.getMaxLatitude(), expected.getMaxLongitude());
        assertThat(bounds.getZoom(DIMENSION, TILE_SIZE)).isEqualTo(LatLongUtils.zoomForBounds(DIMENSION, mapBounds, TILE_SIZE));
    }
}
