package pl.Dayfit.Florae.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for defining a PasswordEncoder bean.
 * This class provides a PasswordEncoder bean for encoding and decoding passwords.
 * Spring Security uses the PasswordEncoder bean for password hashing and verification.
 *
 * <p>Key Responsibilities:</p>
 * <ul>
 *  <li>Defines a PasswordEncoder bean for encoding and decoding passwords.</li>
 */

@Configuration
public class PasswordEncoderConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder(12);
    }
}
