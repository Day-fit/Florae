package pl.Dayfit.Florae.Configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for defining beans related to REST communication and
 * JSON serialization/deserialization.

 * This class provides the necessary components such as RestTemplate for
 * making REST API calls and ObjectMapper for object mapping and JSON processing.
 */
@Configuration
public class RestConfiguration {
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper mapper()
    {
        return new ObjectMapper();
    }
}
