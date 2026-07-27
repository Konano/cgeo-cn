package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.unifiedmap.mapsforge.MapsforgeFragment;

import android.net.Uri;

import androidx.core.util.Pair;

import java.net.MalformedURLException;
import java.net.URL;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.AbstractTileSource;
import org.mapsforge.map.view.MapView;

public class AbstractMapsforgeOnlineTileProvider extends AbstractMapsforgeTileProvider {

    private final AbstractTileSource mfTileSource;
    private String tilePath;

    AbstractMapsforgeOnlineTileProvider(final String name, final Uri uri, final String tilePath, final int zoomMin, final int zoomMax, final Pair<String, Boolean> mapAttribution) {
        this(name, uri, tilePath, zoomMin, zoomMax, mapAttribution, new String[]{uri.getHost()});
    }

    AbstractMapsforgeOnlineTileProvider(final String name, final Uri uri, final String tilePath, final int zoomMin, final int zoomMax,
                                        final Pair<String, Boolean> mapAttribution, final String[] hostNames) {
        super(name, uri, zoomMin, zoomMax, mapAttribution);
        this.tilePath = tilePath;
        mfTileSource = new AbstractTileSource(hostNames, 443) {
            @Override
            public int getParallelRequestsLimit() {
                return 8;
            }

            @Override
            public URL getTileUrl(final Tile tile) throws MalformedURLException {
                // tilePath: "/cyclosm/{Z}/{X}/{Y}.png"
                final String path = TilePathFormatter.format(AbstractMapsforgeOnlineTileProvider.this.tilePath, tile.tileX, tile.tileY, tile.zoomLevel);
                return new URL(AbstractMapsforgeOnlineTileProvider.this.mapUri.getScheme(), getHostName(), this.port, path);
            }

            @Override
            public byte getZoomLevelMax() {
                return (byte) zoomMax;
            }

            @Override
            public byte getZoomLevelMin() {
                return (byte) zoomMin;
            }

            @Override
            public boolean hasAlpha() {
                return false;
            }
        };
    }

    protected void setTilePath(final String tilePath) {
        this.tilePath = tilePath;
    }

    @Override
    public void addTileLayer(final MapsforgeFragment fragment, final MapView map) {
        mfTileSource.setUserAgent("cgeo");
        tileLayer = new TileDownloadLayer(fragment.getTileCache(), map.getModel().mapViewPosition, mfTileSource, AndroidGraphicFactory.INSTANCE);
        map.getLayerManager().getLayers().add(0, tileLayer); // insert at the bottom of the stack so overlays stay on top
        ((TileDownloadLayer) tileLayer).onResume();
    }

}
