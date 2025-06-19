package pl.Dayfit.Florae.Auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.Dayfit.Florae.Entities.FloraeUser;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of Spring Security's UserDetails interface, representing the principal
 * (currently authenticated user) in the security context. This class adapts a FloraeUsers
 * entity to be used with Spring Security.

 * This class holds the details of a specific authenticated user and provides the required
 * methods to retrieve user information like username, password, and authorities (roles).

 * Responsibilities include:
 * - Retrieving the username of the authenticated user.
 * - Retrieving the password of the authenticated user.
 * - Providing the authorities (roles) associated with the authenticated user.

 * The constructor requires a FloraeUsers instance, allowing this principal to wrap the
 * corresponding entity for use in authentication and authorization mechanisms.
 */
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {
    private final FloraeUser user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(user.getRoles()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }
}
