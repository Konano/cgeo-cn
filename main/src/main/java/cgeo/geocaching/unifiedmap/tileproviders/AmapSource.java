package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

class AmapSource extends AbstractMapsforgeOnlineTileProvider {

    AmapSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_amap), Uri.parse("https://wprd01.is.autonavi.com"),
                "/appmaptile?lang=zh_cn&size=1&scale=1&style=10&x={X}&y={Y}&z={Z}", 2, 20,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_amap), false),
                new String[]{"wprd01.is.autonavi.com", "wprd02.is.autonavi.com", "wprd03.is.autonavi.com", "wprd04.is.autonavi.com"});
    }

    @Override
    @NonNull
    public MapCoordinateConverter getCoordinateConverter() {
        return Gcj02CoordinateConverter.INSTANCE;
    }
}
