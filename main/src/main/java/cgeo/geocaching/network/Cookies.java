package cgeo.geocaching.network;

import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.utils.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import okhttp3.Cookie;
import okhttp3.Cookie.Builder;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringUtils;

public final class Cookies {

    public static final InMemoryCookieJar cookieJar = new InMemoryCookieJar();

    public static List<Cookie> extractCookies(@NonNull final String url, final String cookieString, final Predicate<Cookie> filter) {
        if (cookieString == null) {
            return Collections.emptyList();
        }
        final HttpUrl httpUrl = HttpUrl.get(url);
        final List<Cookie> cookies = new ArrayList<>();
        for (String cookie : cookieString.split("; ")) {
            final Cookie c = Cookie.parse(httpUrl, cookie);
            if (c != null && (filter == null || filter.test(c))) {
                cookies.add(c);
            }
        }
        return cookies;
    }

    public static class InMemoryCookieJar implements CookieJar {

        final HashMap<String, Cookie> allCookies = new HashMap<>();

        @Override
        public synchronized void saveFromResponse(@NonNull final HttpUrl url, final List<Cookie> cookies) {
            boolean needStoreUpdate = false;
            final boolean doLogging = Log.isEnabled(Log.LogLevel.DEBUG);
            final StringBuilder cookieLogString = doLogging ? new StringBuilder() : null;
            for (final Cookie cookie : cookies) {
                needStoreUpdate |= addCookie(cookie);
                if (doLogging) {
                    cookieLogString.append(";").append(cookie.name()).append("=").append(prepareCookieValueForLog(cookie.value()));
                    Log.d("HTTP-COOKIES: SAVE META " + getCookieMetadata(cookie));
                }
            }
            if (doLogging) {
                Log.d("HTTP-COOKIES: SAVE for " + url + ": " + cookieLogString);
            }
            if (needStoreUpdate) {
                dumpCookieStore();
            }
        }

        private static String prepareCookieValueForLog(final String value) {
            return StringUtils.isBlank(value) || value.length() < 50 ? value : value.substring(0, 10) + "#" + value.length() + "#" + value.substring(value.length() - 3);

        }

        private static String getCookieMetadata(final Cookie cookie) {
            return "name=" + cookie.name()
                    + ", domain=" + cookie.domain()
                    + ", path=" + cookie.path()
                    + ", persistent=" + cookie.persistent()
                    + ", expiresAt=" + cookie.expiresAt()
                    + ", secure=" + cookie.secure()
                    + ", httpOnly=" + cookie.httpOnly()
                    + ", hostOnly=" + cookie.hostOnly();
        }

        private boolean addCookie(final Cookie cookie) {
            final String key = cookie.domain() + ';' + cookie.name();
            final Cookie oldCookie = allCookies.get(key);
            if (oldCookie == null || !oldCookie.equals(cookie)) {
                allCookies.put(key, cookie);
                return true;
            }
            return false;
        }

        @Override
        @NonNull
        public List<Cookie> loadForRequest(@NonNull final HttpUrl url) {
            final List<Cookie> cookies = new LinkedList<>();
            final boolean doLogging = Log.isEnabled(Log.LogLevel.DEBUG);
            final StringBuilder cookieLogString = doLogging ? new StringBuilder() : null;
            synchronized (this) {
                for (final Cookie cookie : allCookies.values()) {
                    if (cookie.matches(url)) {
                        cookies.add(cookie);
                        if (doLogging) {
                            cookieLogString.append(";").append(cookie.name()).append("=").append(prepareCookieValueForLog(cookie.value()));
                        }
                    }
                }
            }
            if (doLogging) {
                Log.d("HTTP-COOKIES: SEND for " + url + ": " + cookieLogString);
            }
            return cookies;
        }

        public synchronized void clear() {
            allCookies.clear();
            dumpCookieStore();
        }

        private synchronized void restoreCookieStore() {
            final String oldCookies = Settings.getPersistentCookies();
            Log.d("HTTP-COOKIES: RESTORE storedLength=" + (oldCookies == null ? -1 : oldCookies.length()));
            int restoredCount = 0;
            if (oldCookies != null) {
                for (final String cookie : StringUtils.split(oldCookies, ';')) {
                    final String[] split = StringUtils.split(cookie, "=", 3);
                    if (split.length == 3) {
                        try {
                            final Cookie restoredCookie = new Builder().name(split[0]).value(split[1]).domain(split[2]).build();
                            addCookie(restoredCookie);
                            restoredCount++;
                            Log.d("HTTP-COOKIES: RESTORE META " + getCookieMetadata(restoredCookie));
                        } catch (final RuntimeException exception) {
                            Log.w("HTTP-COOKIES: RESTORE failed for name=" + split[0] + ", domain=" + split[2] + ", exception=" + exception.getClass().getSimpleName());
                        }
                    } else {
                        Log.w("HTTP-COOKIES: RESTORE ignored malformed entry with fieldCount=" + split.length);
                    }
                }
            }
            Log.d("HTTP-COOKIES: RESTORE completed restoredCount=" + restoredCount + ", inMemoryCount=" + allCookies.size());
        }

        private void dumpCookieStore() {
            final StringBuilder persistentCookies = new StringBuilder();
            final StringBuilder persistentCookieNames = new StringBuilder();
            int persistentCookieCount = 0;
            for (final Cookie cookie : allCookies.values()) {
                if (!cookie.persistent()) {
                    continue;
                }
                if (persistentCookieNames.length() > 0) {
                    persistentCookieNames.append(',');
                }
                persistentCookieNames.append(cookie.name());
                persistentCookieCount++;
                persistentCookies.append(cookie.name());
                persistentCookies.append('=');
                persistentCookies.append(cookie.value());
                persistentCookies.append('=');
                persistentCookies.append(cookie.domain());
                persistentCookies.append(';');
            }
            Settings.setPersistentCookies(persistentCookies.toString());
            Log.d("HTTP-COOKIES: DUMP inMemoryCount=" + allCookies.size()
                    + ", persistentCount=" + persistentCookieCount
                    + ", names=[" + persistentCookieNames + "]"
                    + ", storedLength=" + persistentCookies.length());
        }
    }

    private Cookies() {
        // Utility class
    }

    public static void clearCookies() {
        cookieJar.clear();
        cookieJar.dumpCookieStore();
    }

    // To be called once when starting the application
    public static void restoreCookies() {
        cookieJar.restoreCookieStore();
    }
}
