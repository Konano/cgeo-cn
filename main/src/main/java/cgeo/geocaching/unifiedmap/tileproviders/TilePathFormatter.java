package cgeo.geocaching.unifiedmap.tileproviders;

final class TilePathFormatter {

    private TilePathFormatter() {
        // utility class
    }

    static String format(final String tilePath, final long tileX, final long tileY, final int zoomLevel) {
        final long invertedTileY = (1L << zoomLevel) - tileY - 1;
        return tilePath
                .replace("{Z}", String.valueOf(zoomLevel))
                .replace("{X}", String.valueOf(tileX))
                .replace("{-Y}", String.valueOf(invertedTileY))
                .replace("{Y}", String.valueOf(tileY));
    }
}
