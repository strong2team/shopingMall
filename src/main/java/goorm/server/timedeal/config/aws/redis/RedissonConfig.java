package goorm.server.timedeal.config.aws.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class RedissonConfig {

	@Bean
	@Profile("dev")
	public RedissonClient redissonClientLocal(@Value("${spring.data.redis.host}") String redisHost,
											  @Value("${spring.data.redis.port}") int redisPort) {
		Config config = new Config();
		String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);
		config.useSingleServer().setAddress(redisAddress);
		return Redisson.create(config);
	}


	@Bean
	@Profile("prod")
	public RedissonClient redissonClientProd(@Value("${spring.data.redis.host}") String redisHost,
											 @Value("${spring.data.redis.port}") int redisPort) {
		Config config = new Config();
		String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);
		config.useSingleServer().setAddress(redisAddress);
		return Redisson.create(config);
	}
}