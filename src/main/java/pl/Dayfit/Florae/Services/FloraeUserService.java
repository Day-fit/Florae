package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUserRequestDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;

/**
 * Service class for handling user-related operations in the Florae system.
 * This class manages the registration of new users and ensures unique constraints
 * on usernames and email addresses.

 * Dependencies:
 * - {@code FloraeUserRepository}: Provides access to database operations for {@code FloraeUser} entities.
 * - {@code BCryptPasswordEncoder}: Used for securely encoding user passwords.

 * Responsibilities:
 * - Registering new users in the system.
 * - Validating the uniqueness of email addresses and usernames during user registration.

 * Annotations:
 * - {@code @Service}: Marks this class as a Spring service component.
 * - {@code @RequiredArgsConstructor}: Generates a constructor with required dependencies through Lombok.

 * Methods:
 * - {@code registerUser(FloraeUserRequestDTO)}: Registers a new user and stores the user data in the database.
 *   Throws {@code DuplicateKeyException} if the username or email is already in use.
 */
@Service
@RequiredArgsConstructor
public class FloraeUserService {
    private final FloraeUserRepository floraeUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void registerUser(FloraeUserRequestDTO floraeUserRequestDTO) throws DuplicateKeyException
    {
        if (floraeUserRepository.existsByEmailOrUsername(floraeUserRequestDTO.getEmail(), floraeUserRequestDTO.getUsername()))
        {
            throw new DuplicateKeyException("Username already exists or email already exists. Please try again with different username or email.");
        }

        FloraeUser floraeUser = new FloraeUser();
        floraeUser.setUsername(floraeUserRequestDTO.getUsername());
        floraeUser.setEmail(floraeUserRequestDTO.getEmail());
        floraeUser.setPassword(bCryptPasswordEncoder.encode(floraeUserRequestDTO.getPassword()));
        floraeUser.setRoles("USER");

        floraeUserRepository.save(floraeUser);
    }
}
