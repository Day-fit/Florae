package pl.Dayfit.Florae.Services.Auth.JWT;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.JPA.FloraeUserRepository;

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
}
