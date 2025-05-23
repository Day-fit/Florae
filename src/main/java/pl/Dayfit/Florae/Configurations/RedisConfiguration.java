package pl.Dayfit.Florae.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableRedisRepositories(basePackages = "pl.Dayfit.Florae.Repositories.Redis")
public class RedisConfiguration {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory)
    {
        RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                    .fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig())
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new Jackson2JsonRedisSerializer<>(String.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.setHashKeySerializer(new Jackson2JsonRedisSerializer<>(String.class));
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        template.afterPropertiesSet();

        return template;
    }
}
