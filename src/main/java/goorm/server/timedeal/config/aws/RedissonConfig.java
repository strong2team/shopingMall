package goorm.server.timedeal.config.aws;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Value("${data.redis.host}")
	private String redisHost;

	@Value("${data.redis.port}")
	private int redisPort;

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		//config.useSingleServer().setAddress("redis://localhost:6379"); // Redis 주소
		String redisAddress = String.format("redis://%s:%d", redisHost, redisPort);
		config.useSingleServer().setAddress(redisAddress); // Redis 주소 설정
		return Redisson.create(config);
	}
}
