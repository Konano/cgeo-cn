package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import static org.oscim.map.Viewport.MIN_ZOOM_LEVEL;

class TencentVTMSource extends AbstractMapsforgeVTMOnlineTileProvider {

    TencentVTMSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_tencent), Uri.parse("https://rt0.map.gtimg.com"),
                "/realtimerender?z={Z}&x={X}&y={-Y}&type=vector&styleid=0", MIN_ZOOM_LEVEL, 18,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_tencent), false),
                new String[]{"https://rt0.map.gtimg.com", "https://rt1.map.gtimg.com",
                        "https://rt2.map.gtimg.com", "https://rt3.map.gtimg.com"});
    }

    @Override
    @NonNull
    public MapCoordinateConverter getCoordinateConverter() {
        return Gcj02CoordinateConverter.INSTANCE;
    }
}
