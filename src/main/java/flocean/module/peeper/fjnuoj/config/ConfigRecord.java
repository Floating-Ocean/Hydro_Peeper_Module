package flocean.module.peeper.fjnuoj.config;

import java.util.List;

public record ConfigRecord (String workPath, CookieRecord cookie, List<Integer> excludeID, String ojUrl, String uptime_apikey) {
}
