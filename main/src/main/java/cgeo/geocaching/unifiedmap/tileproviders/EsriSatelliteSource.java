package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.core.util.Pair;

class EsriSatelliteSource extends AbstractMapsforgeOnlineTileProvider {

    EsriSatelliteSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_esri_satellite), Uri.parse("https://server.arcgisonline.com"),
                "/ArcGIS/rest/services/World_Imagery/MapServer/tile/{Z}/{Y}/{X}", 1, 19,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_esri), false));
    }
}
