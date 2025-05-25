package pl.Dayfit.Florae.Services.Auth.JWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Auth.UserPrincipal;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;

/**
 * Service implementation of {@code UserDetailsService} for Spring Security.

 * This service is responsible for loading user-specific data by username. It interacts
 * with the {@code FloraeUserRepository} to retrieve user information and transform
 * it into a Spring Security {@code UserDetails} object.

 * Primary Responsibilities:
 * - Locating a {@code FloraeUser} entity by its username.
 * - Throwing an exception if the user cannot be found.
 * - Wrapping the found {@code FloraeUser} instance into a {@code UserPrincipal}
 *   for use by Spring Security.

 * Annotations:
 * - {@code @Slf4j}: Provides logging capabilities for diagnostic and tracing purposes.
 * - {@code @Service}: Marks this class as a Spring service component.
 * - {@code @RequiredArgsConstructor}: Generates a constructor with required final fields,
 *   ensuring dependency injection for the {@code FloraeUserRepository}.

 * Dependencies:
 * - {@code FloraeUserRepository}: Used to access and query user-related data from the database.
 * - {@code UserPrincipal}: Represents the authenticated user's details with integration
 *   for Spring Security.

 * Methods:
 * - {@code loadUserByUsername}: Retrieves a {@code FloraeUser} by username, wraps it
 *   into a {@code UserPrincipal}, or throws a {@code UsernameNotFoundException} if the
 *   user does not exist.

 * Exceptions:
 * - {@code UsernameNotFoundException}: Thrown when no user with the specified username
 *   is found in the system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FloraeUserDetailsService implements UserDetailsService {
    private final FloraeUserRepository floraeUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FloraeUser user = floraeUserRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Username not found");
        }

        return new UserPrincipal(user);
    }
}
