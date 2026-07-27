package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import static org.oscim.map.Viewport.MIN_ZOOM_LEVEL;

class AmapVTMSource extends AbstractMapsforgeVTMOnlineTileProvider {

    AmapVTMSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_amap), Uri.parse("https://webrd01.is.autonavi.com"),
                "/appmaptile?lang=zh_cn&size=1&scale=1&style=7&x={X}&y={Y}&z={Z}", MIN_ZOOM_LEVEL, 20,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_amap), false));
    }

    @Override
    @NonNull
    public MapCoordinateConverter getCoordinateConverter() {
        return Gcj02CoordinateConverter.INSTANCE;
    }
}
