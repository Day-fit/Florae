package pl.Dayfit.Florae.Auth;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Locator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.Dayfit.Florae.Services.Auth.JWT.SecretKeysService;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class FloraeKeyLocator implements Locator<Key> {
    private final SecretKeysService secretKeysService;

    @Override
    public Key locate(Header header) {
        Object keyId = header.get("keyId");
        if (!(keyId instanceof Integer keyIndex)) {
            throw new JwtException("Invalid key ID header value: " + keyId);
        }

        return secretKeysService.getCurrentSecret(keyIndex);
    }
}
