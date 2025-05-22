package pl.Dayfit.Florae.Auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.Dayfit.Florae.Entities.ApiKey;

import java.util.Collections;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final ApiKey apiKey;

    public ApiKeyAuthenticationToken(ApiKey apiKey) {
        super(Collections.singleton(new SimpleGrantedAuthority("FLORAE_LINK")));
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return apiKey.getKeyValue();
    }

    @Override
    public Object getPrincipal() {
        return apiKey.getFloraeUser();
    }
}