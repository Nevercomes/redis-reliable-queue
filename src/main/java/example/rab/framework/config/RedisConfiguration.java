package example.rab.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfiguration implements CachingConfigurer {

    @Bean
    @ConditionalOnProperty(name = "spring.redis.mode", havingValue = "standalone")
    public RedisConnectionFactory standaloneConnectionFactory(@Value("${spring.redis.standalone.host}") String host,
                                                              @Value("${spring.redis.standalone.port}") int port) {
        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        return new LettuceConnectionFactory(standaloneConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        connectionFactory.setValidateConnection(true);
        template.setConnectionFactory(connectionFactory);

        return init(template);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplateWithTx(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        connectionFactory.setValidateConnection(true);
        template.setConnectionFactory(connectionFactory);
        template.setEnableTransactionSupport(true);

        return init(template);
    }

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<String, Object> init(RedisTemplate<String, Object> template) {
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);

        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash 的 key 也采用 StringRedisSerializer 的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}
