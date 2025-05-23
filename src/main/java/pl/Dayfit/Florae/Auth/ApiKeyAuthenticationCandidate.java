package pl.Dayfit.Florae.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import pl.Dayfit.Florae.Entities.ApiKey;

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
