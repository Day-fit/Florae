package pl.Dayfit.Florae.Services.Auth.JWT;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;

/**
 * Service class responsible for caching and retrieving {@code FloraeUser} information.
 * This service provides methods to fetch user data from the database and caches the results
 * for improved performance and reduced database load.

 * Annotations:
 * - {@code @Service}: Marks this class as a Spring service for dependency injection.
 * - {@code @RequiredArgsConstructor}: Generates a constructor with required dependencies via Lombok.

 * Cache Configuration:
 * - Caches data under the cache name "florae-users".
 * - Uses method arguments (e.g., {@code username}, {@code id}, {@code email}) as cache keys.

 * Methods:
 * - {@code getFloraeUser(String username)}: Retrieves a {@code FloraeUser} by the provided username.
 *   The result is cached using the username as the key.
 * - {@code getFloraeUserById(int id)}: Retrieves a {@code FloraeUser} by the provided user ID.
 *   The result is cached using the user ID as the key.
 * - {@code getFloraeUserByEmail(String email)}: Retrieves a {@code FloraeUser} by the provided email.
 *   The result is cached using the email as the key.
 */
@Service
@RequiredArgsConstructor
public class FloraeUserCacheService {
    private final FloraeUserRepository userRepository;

    @Cacheable(value = "florae-users", key = "#username")
    public FloraeUser getFloraeUser(String username)
    {
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "florae-users", key = "#id")
    public FloraeUser getFloraeUserById(int id)
    {
        return userRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "florae-users", key = "#email")
    public FloraeUser getFloraeUserByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    @CachePut(value = "florae-users", key = "#user.id")
    public FloraeUser saveFloraeUser(FloraeUser user)
    {
        return userRepository.save(user);
    }

    @Cacheable(value = "florae-users", key = "{#email, #username}")
    public FloraeUser findByEmailOrUsername(String email, String username)
    {
        return userRepository.findByUsernameOrEmail(username, email);
    }
}
