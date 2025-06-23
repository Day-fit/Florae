package pl.Dayfit.Florae.Other;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@AllArgsConstructor
public class RedisSchemaReset implements ApplicationRunner {
    private final RedisConnectionFactory factory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        factory.getConnection().serverCommands().flushDb();
    }
}
