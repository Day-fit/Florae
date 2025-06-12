package pl.Dayfit.Florae.Helpers.SpEL;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

/**
 * Helper class for generating short API keys.
 * <p>Can be used in SpEL</p>
 */

@Component("apiKeysHelper")
public class ApiKeysHelper {
    public String generateShortKey(String rawApiKey) {
        return DigestUtils.sha256Hex(rawApiKey).substring(0, 12); //Used to reduce the query results
    }

    public String generateShortFromHash(String hashedApiKey) {
        return hashedApiKey.substring(0, 12);
    }
}
