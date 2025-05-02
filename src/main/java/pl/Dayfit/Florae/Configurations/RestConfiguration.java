package pl.Dayfit.Florae.Configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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
