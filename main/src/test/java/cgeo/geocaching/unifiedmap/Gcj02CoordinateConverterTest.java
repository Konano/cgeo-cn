package cgeo.geocaching.unifiedmap;

import cgeo.geocaching.location.Geopoint;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class Gcj02CoordinateConverterTest {

    private static final Gcj02CoordinateConverter CONVERTER = Gcj02CoordinateConverter.INSTANCE;

    @Test
    public void wgs84ToGcj02() {
        final Geopoint gcj02 = CONVERTER.toMap(new Geopoint(39.908823, 116.397470));

        assertThat(gcj02.getLatitude()).isEqualTo(39.91022649807321, offset(0.000002));
        assertThat(gcj02.getLongitude()).isEqualTo(116.4037135824225, offset(0.000002));
    }

    @Test
    public void gcj02ToWgs84() {
        final Geopoint wgs84 = CONVERTER.fromMap(new Geopoint(39.91022649807321, 116.4037135824225));

        assertThat(wgs84.getLatitude()).isEqualTo(39.908823, offset(0.000002));
        assertThat(wgs84.getLongitude()).isEqualTo(116.397470, offset(0.000002));
    }

    @Test
    public void roundTrip() {
        final Geopoint wgs84 = new Geopoint(31.2304, 121.4737);
        final Geopoint roundTrip = CONVERTER.fromMap(CONVERTER.toMap(wgs84));

        assertThat(roundTrip.getLatitude()).isEqualTo(wgs84.getLatitude(), offset(0.000002));
        assertThat(roundTrip.getLongitude()).isEqualTo(wgs84.getLongitude(), offset(0.000002));
    }

    @Test
    public void outsideChinaIsUnchanged() {
        final Geopoint london = new Geopoint(51.5074, -0.1278);

        assertThat(CONVERTER.toMap(london)).isSameAs(london);
        assertThat(CONVERTER.fromMap(london)).isSameAs(london);
    }
}
