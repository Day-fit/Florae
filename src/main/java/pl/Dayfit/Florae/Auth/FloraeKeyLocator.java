package pl.Dayfit.Florae.Auth;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.Dayfit.Florae.Services.Auth.JWT.SecretKeysService;

import java.security.Key;

/**
 * Component responsible for localizing the correct SecretKey
 * based on the `keyId` in JWT token headers
 */
@Component
@RequiredArgsConstructor
public class FloraeKeyLocator implements Locator<Key> {
    private final SecretKeysService secretKeysService;

    /**
     * Localize the SecretKey based on the `keyId` in the header
     *
     * @param header the JWT header to inspect
     * @return Secret key based on `keyId`
     * @throws JwtException if the `keyId` is invalid
     */
    @Override
    public Key locate(Header header) {
        Object keyId = header.get("keyId");
        if (!(keyId instanceof Integer keyIndex)) {
            throw new JwtException("Invalid key ID header value: " + keyId);
        }

        return secretKeysService.getCurrentSecret(keyIndex);
    }
}
