package pl.Dayfit.Florae.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.Dayfit.Florae.DTOs.FloraeUserLoginDTO;
import pl.Dayfit.Florae.DTOs.FloraeUserRegisterDTO;
import pl.Dayfit.Florae.Entities.FloraeUser;
import pl.Dayfit.Florae.Repositories.FloraeUserRepository;

import java.security.SecureRandom;
import java.util.Base64;


/**
 * Service class responsible for user-related operations in the Florae system.
 * Handles user registration, authentication, and token generation.

 * <p>Responsibilities:</p>
 * <ul>
 *  <li> Registering a new user with validation to ensure a unique email and username.</li>
 *  <li> Validating user credentials during login.</li>
 *  <li> Generating JWT tokens for authenticated users.</li>
 *  <li> Generating new refresh tokens for authenticated users.</li>
 * </ul>

 * <p>Dependencies:</p>
 * <ul>
 *  <li> {@code FloraeUserRepository}: Used for accessing and managing user data in the database.</li>
 *  <li> {@code BCryptPasswordEncoder}: Used for encrypting user passwords.</li>
 *  <li> {@code AuthenticationManager}: Used for authenticating user credentials.</li>
 *  <li> {@code JWTService}: Used for generating JSON Web Token (JWT) for user authentication.</li>
 *  <li> {@code SecureRandom}: Used for generating random salt values for user passwords.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FloraeUserService {
    private final FloraeUserRepository floraeUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final SecureRandom secureRandom;

    public static final int ACCESS_TOKEN_EXPIRATION_TIME = 30;
    public static final int REFRESH_TOKEN_EXPIRATION_TIME = 14;

    public void registerUser(FloraeUserRegisterDTO floraeUserRegisterDTO) throws DuplicateKeyException
    {
        if (floraeUserRepository.existsByEmailOrUsername(floraeUserRegisterDTO.getEmail(), floraeUserRegisterDTO.getUsername()))
        {
            throw new DuplicateKeyException("Username already exists or email already exists. Please try again with different username or email.");
        }

        FloraeUser floraeUser = new FloraeUser();
        floraeUser.setUsername(floraeUserRegisterDTO.getUsername().toLowerCase());
        floraeUser.setEmail(floraeUserRegisterDTO.getEmail().toLowerCase());
        floraeUser.setSalt(generateSalt());
        floraeUser.setPassword(bCryptPasswordEncoder.encode(floraeUserRegisterDTO.getPassword() + floraeUser.getSalt()));
        floraeUser.setRoles("USER");

        floraeUserRepository.save(floraeUser);
    }

    public boolean isValid(FloraeUserLoginDTO floraeUserLoginDTO) {

        if(floraeUserLoginDTO.getEmail() != null) {
            String email = floraeUserLoginDTO.getEmail().toLowerCase();
            FloraeUser floraeUser = floraeUserRepository.findByEmail(email);

            try {
                String salt = floraeUser.getSalt() == null ? "" : floraeUser.getSalt();
                return authManager.authenticate(new UsernamePasswordAuthenticationToken(floraeUserLoginDTO.getUsername(), floraeUserLoginDTO.getPassword() + salt)).isAuthenticated();
            } catch (AuthenticationException e) {
                return false;
            }
        }

        if (floraeUserLoginDTO.getUsername() != null) {
            String username = floraeUserLoginDTO.getUsername().toLowerCase();
            FloraeUser floraeUser = floraeUserRepository.findByUsername(username);

            try {
                String salt = floraeUser.getSalt() == null ? "" : floraeUser.getSalt();
                return authManager.authenticate(new UsernamePasswordAuthenticationToken(username, floraeUserLoginDTO.getPassword() + salt)).isAuthenticated();
            } catch (AuthenticationException e) {
                return false;
            }
        }

        return false;
    }

    public String getAccessToken(String username) {
        return jwtService.generateAccessToken(username, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public String getAccessTokenFromRefreshToken(String refreshToken)
    {
        String username = jwtService.extractUsername(refreshToken);

        if (jwtService.validateRefreshToken(refreshToken)){
            return getAccessToken(username);
        }

        return null;
    }

    public String getRefreshToken(String username)
    {
        return jwtService.generateRefreshToken(username, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    private String generateSalt()
    {
        byte[] salt = new byte[44];
        secureRandom.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }
}
