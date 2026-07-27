package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

class TencentSource extends AbstractMapsforgeOnlineTileProvider {

    TencentSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_tencent), Uri.parse("https://rt0.map.gtimg.com"),
                "/realtimerender?z={Z}&x={X}&y={-Y}&type=vector&styleid=0", 2, 18,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_tencent), false),
                new String[]{"rt0.map.gtimg.com", "rt1.map.gtimg.com", "rt2.map.gtimg.com", "rt3.map.gtimg.com"});
    }

    @Override
    @NonNull
    public MapCoordinateConverter getCoordinateConverter() {
        return Gcj02CoordinateConverter.INSTANCE;
    }
}
