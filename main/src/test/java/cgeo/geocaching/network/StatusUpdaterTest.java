package cgeo.geocaching.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusUpdaterTest {

    private static final int INSTALLED = 202610072;
    private static final String URL = "https://cache.nan.pub/geocaching/cgeo_cn/cgeo-cn-release.apk";

    private static ObjectNode response(final int versionCode) {
        return new ObjectMapper().createObjectNode()
                .put("version_code", versionCode)
                .put("message", "New c:geo CN release available.")
                .put("message_id", "status_new_release")
                .put("icon", "attribute_climbing")
                .put("url", URL);
    }

    @Test
    public void newerVersionKeepsTheExistingStatusDisplayAndDownloadLink() {
        final StatusUpdater.Status status = StatusUpdater.Status.fromCnResponse(response(INSTALLED + 1), INSTALLED);
        assertThat(status).isNotSameAs(StatusUpdater.Status.NO_STATUS);
        assertThat(status.messageId).isEqualTo("status_new_release");
        assertThat(status.icon).isEqualTo("attribute_climbing");
        assertThat(status.url).isEqualTo(URL);
    }

    @Test
    public void sameAndOlderVersionsDoNotShowAnUpdate() {
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED), INSTALLED)).isSameAs(StatusUpdater.Status.NO_STATUS);
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED - 1), INSTALLED)).isSameAs(StatusUpdater.Status.NO_STATUS);
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED).put("version_name", "2099.01.01"), INSTALLED))
                .isSameAs(StatusUpdater.Status.NO_STATUS);
    }

    @Test
    public void invalidVersionCodesDoNotShowAnUpdate() {
        final ObjectNode missing = response(INSTALLED + 1);
        missing.remove("version_code");
        for (ObjectNode json : new ObjectNode[]{missing, response(0), response(-1),
                response(INSTALLED).put("version_code", "209901012"), response(INSTALLED).put("version_code", true),
                response(INSTALLED).put("version_code", 209901012.5), response(INSTALLED).put("version_code", 2147483648L)}) {
            assertThat(StatusUpdater.Status.fromCnResponse(json, INSTALLED)).isSameAs(StatusUpdater.Status.NO_STATUS);
        }
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED + 1), 0)).isSameAs(StatusUpdater.Status.NO_STATUS);
    }

    @Test
    public void incompleteStatusDoesNotDisplayAnEmptyOrUnclickableUpdate() {
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED + 1).putNull("message"), INSTALLED))
                .isSameAs(StatusUpdater.Status.NO_STATUS);
        assertThat(StatusUpdater.Status.fromCnResponse(response(INSTALLED + 1).put("url", ""), INSTALLED))
                .isSameAs(StatusUpdater.Status.NO_STATUS);
    }
}
