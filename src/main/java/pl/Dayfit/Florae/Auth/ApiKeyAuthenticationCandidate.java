package pl.Dayfit.Florae.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import pl.Dayfit.Florae.Entities.ApiKey;

/**
 * Represents an authentication candidate for API key-based authentication.
 * This class extends {@link AbstractAuthenticationToken}
 * and is utilized during the authentication process to encapsulate and represent an incoming API key
 * as a potential authentication token.
 * <p>
 * Responsibilities include:
 * - Encapsulation of an {@link ApiKey} object, which represents the provided API key.
 * - Providing methods to retrieve authentication credentials and the principal
 *   (associated user) based on the API key.
 * <p>
 * Key Behavior:
 * - Overrides {@code getCredentials()} to return the associated {@link ApiKey} instance.
 * - Overrides {@code getPrincipal()} to return the {@link pl.Dayfit.Florae.Entities.FloraeUser}
 *   linked to the provided API key.
 * <p>
 * Usage Context:
 * - This class is typically used before establishing full authentication. For example,
 *   an instance of this class may be used in initial authentication stages to hold the API key
 *   while verifying its validity to promote or deny authentication.
 * <p>
 * Notes:
 * - This token is created in an unauthenticated state initially (as specified in the constructor),
 *   and authentication can only proceed if the provided API key is validated.
 */
public class ApiKeyAuthenticationCandidate extends AbstractAuthenticationToken {
    private final ApiKey apiKey;

    public ApiKeyAuthenticationCandidate(ApiKey apiKey)
    {
        super(null);
        this.apiKey = apiKey;
        setAuthenticated(false);
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
