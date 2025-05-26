package pl.Dayfit.Florae.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.Dayfit.Florae.Entities.ApiKey;

import java.util.Collections;

/**
 * Represents an authentication token for API key-based authentication.
 * This class extends {@link AbstractAuthenticationToken} and encapsulates
 * authentication details related to an API key.
 * <p>
 * Responsibilities:
 * - Holds the authenticated {@link ApiKey}.
 * - Provides access to the credentials (API key) and the principal (user associated with the API key).
 * <p>
 * The class is used in the authentication process to indicate successful
 * authentication based on the provided API key.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final ApiKey apiKey;

    public ApiKeyAuthenticationToken(ApiKey apiKey) {
        super(Collections.singleton(new SimpleGrantedAuthority("FLORA_LINK")));
        this.apiKey = apiKey;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public Object getPrincipal() {
        return apiKey.getFloraeUser();
    }
}