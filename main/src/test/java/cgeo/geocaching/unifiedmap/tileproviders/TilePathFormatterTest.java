package cgeo.geocaching.unifiedmap.tileproviders;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TilePathFormatterTest {

    @Test
    public void xyz() {
        assertThat(TilePathFormatter.format("/{Z}/{X}/{Y}.png", 3, 5, 4)).isEqualTo("/4/3/5.png");
    }

    @Test
    public void invertedY() {
        assertThat(TilePathFormatter.format("/tile?z={Z}&x={X}&y={-Y}", 3, 5, 4)).isEqualTo("/tile?z=4&x=3&y=10");
    }
}
